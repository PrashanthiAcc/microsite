import { Component, HostListener, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute, NavigationEnd } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { ToasterComponent } from '../../../../shared/components/toaster/toaster';
import { Spinner } from '../../../../shared/components/spinner/spinner';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';


@Component({
  selector: 'app-favourites-story',
  imports: [CommonModule, CustomDropdownComponent, RouterModule, ToasterComponent, FormsModule, Spinner],
  templateUrl: './favourites-story.html',
  styleUrl: './favourites-story.scss',
})
export class FavouritesStoryComponent {
showAdminControls = false;
  isLoading = true;
  industries: any[] = [];
  subIndustries: any[] = [];
  valueChains: any[] = [];

  allIndustries: any[] = [];
  allSubIndustries: any[] = [];
  allValueChains: any[] = [];
  stories: any[] = [];
  filteredStories: any[] = [];
  searchText: string = '';
  drafts: any[] = [];
  allDrafts: any[] = [];
  allUsers: any[] = [];
  showToast = false;
  toastMessage = '';
  toastTitle = '';
  currentPage = 1;
  totalPages = 0;
  pageSize = 10;
  totalElements = 0;
  pagination_right = 'assets/icons/pagination_right.png';
  pagination_left = 'assets/icons/pagination_left.png';
  reviewIcon = "assets/icons/review.png";
  durationIcon = "assets/icons/time.png";
  user_logo = "assets/icons/user_logo.png";
  archiveIcon = "assets/icons/archive.png";
  discardIcon = "assets/icons/discard.png";
  favoriteIcon = "assets/icons/favorite.png";
  editIcon = "assets/icons/edit.png";
  currentFrom: string = 'stories';
  selectedIndustryId: number | null = null;
  selectedSubIndustryId: number | null = null;
  selectedValueChainId: number | null = null;
  selectedIndustryObj: any = null;
  cardCategoryTitle: any;
  filtered: any;
  subIndustryName: any;
  industryName: any;
  valueChainName: any;
  showIndustryOverview: boolean = false;
  showValueChain: boolean = false;
  isDataLoading = signal(false);

  constructor(private router: Router, private http: HttpClient,
    private industryService: IndustryService, private cdr: ChangeDetectorRef, private route: ActivatedRoute,
    private userService: UserService, private usecaseService: UsecaseService
  ) { }

  ngOnInit() {
    this.loadIndustries();
    this.route.queryParams.subscribe(params => {
      this.showAdminControls = params['from'] === 'config';
      this.currentFrom = this.route.snapshot.queryParams['from'] || 'stories';
      this.currentPage = params['page'] ? +params['page'] : 1;
      const id = params['id'];
      if (id) {
        console.log("selected story Id:", id);
      }
      this.loadAllStories(this.currentPage);
    });
    this.getAllUsers();
    this.cdr.detectChanges();
    window.scrollTo({ top: 0 });

  }


  loadAllStories(page: number = 1, size: number = 10) {
    this.isDataLoading.set(true);
    this.isLoading = false;
    this.usecaseService.getAllStories(page, size).subscribe({
      next: (res: any) => {
        let allStories = res.content || [];
        console.log("all stories:: ", allStories)

        if (!this.showAdminControls) {
          allStories = allStories.filter((story: any) => story.status == 'IN_REVIEW');
          //console.log("filtered stories:: ", this.filteredStories)
          if (allStories.length > 0) {
            this.isLoading = true;
          } else {
            this.isLoading = false;
          }
        }

        this.isLoading = false;

        // Sort by updatedDate DESC (latest first)
        allStories = allStories.sort((a: any, b: any) => {
          const dateA = a.updatedDate ? new Date(a.updatedDate).getTime() : new Date(a.createdDate).getTime();
          const dateB = b.updatedDate ? new Date(b.updatedDate).getTime() : new Date(b.createdDate).getTime();
          return dateB - dateA; // latest first
        });

        this.stories = allStories.map((story: any) => ({
          ...story,
          tags: story.tag,
          ownerName: this.getOwnerName(story.ownerEId),
          industryName: this.getIndustryName(story.industryId),
          subIndustryName: this.getSubIndustryName(story.subIndustryId),
          valueChainName: this.getValueChainName(story.valueChainId)
        }));

        this.filteredStories = this.stories;
        console.log("filtered stories:: ", this.filteredStories)
        this.applyFilters();
        if (this.showAdminControls && this.currentFrom !== 'stories') {
          this.filteredStories = this.stories.filter((story: any) => story.status === 'IN_REVIEW');
          console.log("filtered stories:: ", this.filteredStories)

          this.drafts = allStories
            .filter((story: any) => story.status === 'DRAFT')
            .map((draft: any) => ({
              ...draft,
              tags: draft.tag,
              ownerName: this.getOwnerName(draft.ownerEId),
              industryName: this.getIndustryName(draft.industryId),
              subIndustryName: this.getSubIndustryName(draft.subIndustryId),
              valueChainName: this.getValueChainName(draft.valueChainId)
            }));
        }

        this.currentPage = page;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.pageSize = res.size;
        this.isDataLoading.set(false);
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.isDataLoading.set(false);
      }
      //console.log('Stories loaded:', this.stories);
    });
  }


  applyFilters() {
    this.filtered = [...this.stories];

    if (this.selectedIndustryId !== -1) {
      this.filtered = this.filtered.filter((story: any) => story.industryId === this.selectedIndustryId);
    }

    if (this.selectedSubIndustryId !== -1) {
      this.filtered = this.filtered.filter((story: any) => story.subIndustryId === this.selectedSubIndustryId);
    }

    if (this.selectedValueChainId !== -1) {
      this.filtered = this.filtered.filter((story: any) => story.valueChainId === this.selectedValueChainId);
    }

    this.filteredStories = this.filtered;

    if (this.showAdminControls && this.currentFrom !== 'stories') {

      this.drafts = this.filtered
        .filter((story: any) => story.status === 'DRAFT')
        .map((draft: any) => ({
          ...draft,
          tags: draft.tags,
          ownerName: this.getOwnerName(draft.ownerId),
          industryName: this.getIndustryName(draft.industryId),
          subIndustryName: this.getSubIndustryName(draft.subIndustryId)
        }));

      this.filteredStories = this.filtered.filter((story: any) => story.status === 'IN_REVIEW');
      console.log("filtered stories:: ", this.filteredStories)
      // this.filtered = this.filteredStories;
    } else {
      this.filteredStories = [...this.filtered];
    }
  }

  getAllUsers() {
    this.userService.getAllUsers().subscribe((res: any) => {
      this.allUsers = res;

      // re-map owner names if stories already loaded
      this.stories = this.stories.map(story => ({
        ...story,
        ownerName: this.getOwnerName(story.ownerEId)
      }));
    });
  }

  getOwnerName(ownerEId: string): string {
    if (!this.allUsers) return '';
    const user = this.allUsers.find((u: any) => u.userEid === ownerEId);
    return user ? user.userEid : 'Unknown';
  }

  toggleMenu(story: any, event?: MouseEvent) {
    if (event) {
      event.stopPropagation();
    }
    // close others first
    this.stories.forEach(s => {
      if (s !== story) {
        s.showMenu = false;
      }
    });
    story.showMenu = !story.showMenu;
  }

  @HostListener('document:click')
  closeMenus() {
    this.stories.forEach(s => (s.showMenu = false));
  }


  loadIndustries() {
    this.industryService.getIndustries().subscribe((res: any) => {
      this.allIndustries = res.industries;
      this.allSubIndustries = res.subIndustries;
      this.allValueChains = res.valueChains;

      this.industries = [
        { industryId: -1, industryName: 'All' },
        ...this.allIndustries
      ];

      this.subIndustries = [
        { subIndustryId: -1, subIndustryName: 'All' }
      ];

      this.valueChains = [{ valueChainId: -1, valueChainName: 'All' }];
      this.valueChainName = 'All';


      this.selectedSubIndustryId = -1;
      this.selectedValueChainId = -1;

      this.cardCategoryTitle = this.route.snapshot.queryParams['title'];
      const title = this.route.snapshot.queryParams['title'];
      if (title) {
        const selectedIndustryObj = this.industries.find(
          ind => ind.industryName === title
        );
        if (selectedIndustryObj) {
          this.selectedIndustryId = selectedIndustryObj.industryId;
          this.industryName = selectedIndustryObj.industryName;   // ✅ hydrate name
          this.subIndustryName = null;                           // reset
          this.valueChainName = null;
          this.applyFilters();
          this.cdr.detectChanges();
        }
        this.loadAllStories(this.currentPage, this.pageSize);
      } else {
        // Default Industry also to "All"
        this.selectedIndustryId = -1;
      }
    });
  }


  onIndustrySelected(industry: any) {
    this.industryName = industry.industryName;
    console.log("industry name::", industry)
    if (!industry || industry.industryId === -1) {
      this.selectedIndustryId = -1;
      this.selectedSubIndustryId = -1;
      this.selectedValueChainId = -1;

      this.industryName = null;
      this.subIndustryName = null;
      this.valueChainName = null;

      this.subIndustries = [
        { subIndustryId: -1, subIndustryName: 'All' }
      ];

      this.valueChains = [{ valueChainId: -1, valueChainName: 'All' }];
      this.valueChainName = 'All';

      this.applyFilters();
      return;
    }

    this.selectedIndustryId = industry.industryId;
    this.selectedSubIndustryId = -1;
    this.selectedValueChainId = -1;

    this.subIndustryName = null;
    this.valueChainName = null;
    this.subIndustries = [
      { subIndustryId: -1, subIndustryName: 'All' },
      ...this.allSubIndustries.filter(sub => sub.industryId === industry.industryId)
    ];

    this.valueChains = [{ valueChainId: -1, valueChainName: 'All' }];
    this.valueChainName = 'All';

    this.applyFilters();
  }

  onSubIndustrySelected(sub: any) {
    this.subIndustryName = sub.subIndustryName;
    console.log("sub industry name::", sub)
    if (!sub || sub.subIndustryId === -1) {
      this.selectedSubIndustryId = -1;
      this.selectedValueChainId = -1;
      this.valueChainName = null;
      this.valueChains = [{ valueChainId: -1, valueChainName: 'All' }];
      this.valueChainName = 'All';

      this.applyFilters();
      return;
    }

    this.selectedSubIndustryId = sub.subIndustryId;
    this.selectedValueChainId = -1;
    this.valueChainName = null;
    this.valueChains = [
      { valueChainId: -1, valueChainName: 'All' },
      ...this.allValueChains.filter(vc => Number(vc.subIndustryId) === Number(sub.subIndustryId))
    ];
    this.valueChainName = 'All';

    this.applyFilters();
  }

  onValueChainSelected(vc: any) {
    this.valueChainName = vc.valueChainName;
    console.log("value chain name::", vc)
    if (!vc || vc.valueChainId === -1) {
      this.selectedValueChainId = -1;
      this.valueChainName = null;
      this.applyFilters();
      return;
    }

    this.selectedValueChainId = vc.valueChainId;
    this.applyFilters();
  }


  onSearch() {
    const value = this.searchText.trim().toLowerCase();

    if (this.showAdminControls && this.currentFrom !== 'stories') {
      this.filteredStories = this.stories.filter((story: any) => story.status === 'IN_REVIEW');
      this.filtered = this.filteredStories;
    } else {
      this.filtered = [...this.stories];
    }
    if (!value) {
      this.filteredStories = this.filtered;
      return;
    }

    this.filteredStories = this.filtered.filter((story: any) => {

      const title = (story.title || '').toLowerCase();
      const description = (story.description || '').toLowerCase();
      const ownerEId = (story.ownerEId || '').toLowerCase();
      const ownerName = (story.ownerName || '').toLowerCase();

      const tags = Array.isArray(story.tags)
        ? story.tags.join(' ').toLowerCase()
        : (story.tags || '').toLowerCase();

      return (
        title.includes(value) ||
        description.includes(value) || ownerEId.includes(value) || ownerName.includes(value) ||
        tags.includes(value)
      );
    });
  }

  getIndustryName(id: number): string {
    const industry = this.allIndustries.find((i: any) => i.industryId === id);
    return industry ? industry.industryName : '';
  }

  getSubIndustryName(id: number): string {
    const subIndustry = this.allSubIndustries.find((s: any) => s.subIndustryId === id);
    return subIndustry ? subIndustry.subIndustryName : '';
  }

  getValueChainName(id: number): string {
    const valueChain = this.allValueChains.find((s: any) => s.valueChainId === id);
    return valueChain ? valueChain.valueChainName : '';
  }

  isFilterApplied(): boolean {
    return !!(
      (this.industryName && this.industryName !== 'All') ||
      (this.subIndustryName && this.subIndustryName !== 'All') ||
      (this.valueChainName && this.valueChainName !== 'All')
    );
  }

  isNewStory(story: any): boolean {
    if (!story.updatedDate) return true;
    return false;
  }

}
