import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute, NavigationEnd } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';
import { ChangeDetectorRef } from '@angular/core';
import { ToasterComponent } from '../../../../shared/components/toaster/toaster';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-stories',
  standalone: true,
  imports: [CommonModule, CustomDropdownComponent, RouterModule, ToasterComponent, FormsModule],
  templateUrl: './stories.html',
  styleUrls: ['./stories.scss']
})
export class StoriesComponent implements OnInit {
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
  viewIndustryOverview = [
      { name: 'Life Sciences', defaultImage: 'assets/LifeScience Industry Overview.png' },
    ];
  viewValueChain = [
      { name: 'Pharmaceuticals', defaultImage: 'assets/Pharmaceuticals Value Chain.png' },
      //{ name: 'Med Tech', defaultImage: 'assets/Life Sciences.png' },
    ];
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
    this.isLoading = false;
    this.usecaseService.getAllStories(page, size).subscribe((res: any) => {
      let allStories = res.content || [];

      if (!this.showAdminControls) {
        allStories = allStories.filter((story: any) => story.isActive == true);
        if (allStories.length > 0) {
          this.isLoading = true;
        } else {
          this.isLoading = false;
        }
      }

      this.isLoading = false;

      this.stories = allStories.map((story: any) => ({
        ...story,
        tags: story.tag,
        ownerName: this.getOwnerName(story.ownerEId),
        industryName: this.getIndustryName(story.industryId),
        subIndustryName: this.getSubIndustryName(story.subIndustryId)
      }));

      this.filteredStories = this.stories;
      this.applyFilters();
      if (this.showAdminControls && this.currentFrom !== 'stories') {
        this.filteredStories = this.stories.filter((story: any) => story.status === 'IN_REVIEW');

        this.drafts = allStories
          .filter((story: any) => story.status === 'DRAFT')
          .map((draft: any) => ({
            ...draft,
            tags: draft.tag,
            ownerName: this.getOwnerName(draft.ownerEId),
            industryName: this.getIndustryName(draft.industryId),
            subIndustryName: this.getSubIndustryName(draft.subIndustryId)
          }));
      }

      this.currentPage = page;
      this.totalPages = res.totalPages;
      this.totalElements = res.totalElements;
      this.pageSize = res.size;
      this.cdr.detectChanges();
      console.log('Stories loaded:', this.stories);
    });
  }


  applyFilters() {
    if (this.showAdminControls && this.currentFrom !== 'stories') {
      this.filtered = this.stories.filter((story: any) => story.status === 'IN_REVIEW');
      // this.filtered = this.filteredStories;
    } else {
      this.filtered = [...this.stories];
    }

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


  archiveStories() {
    // Handle archive selected stories
    console.log('Archive stories clicked');
    // TODO: Implement archiving logic
  }

  // loadIndustries() {
  //   this.industryService.getIndustries().subscribe((res: any) => {

  //     this.allIndustries = res.industries;

  //     this.allSubIndustries = res.subIndustries;
  //     this.allValueChains = res.valueChains;

  //     this.industries = [
  //       { industryId: null, industryName: 'All' },
  //       ...this.allIndustries
  //     ];

  //     this.subIndustries = [
  //       { subIndustryId: null, subIndustryName: 'All' },
  //       ...this.allSubIndustries
  //     ];

  //     this.valueChains = [
  //       { valueChainId: null, valueChainName: 'All' },
  //       ...this.allValueChains
  //     ];

  //     this.cardCategoryTitle = this.route.snapshot.queryParams['title'];
  //     const title = this.route.snapshot.queryParams['title'];
  //     if (title) {
  //       const selectedIndustryObj = this.industries.find(
  //         ind => ind.industryName === title
  //       );
  //       if (selectedIndustryObj) {
  //         this.selectedIndustryId = selectedIndustryObj.industryId;
  //         this.applyFilters();
  //         this.cdr.detectChanges();
  //       }
  //     }

  //   });
  // }

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

  archiveStory(usecaseId: string) {
    this.usecaseService.archiveUsecase(usecaseId).subscribe({
      next: (res: string) => {
        console.log('API success, response:', res);

        this.toastTitle = 'Story Archived Successfully';
        this.toastMessage = res;
        this.showToast = true;

        // ✅ Close the card menu for the archived story
        this.stories.forEach(s => {
          if (s.usecaseId === usecaseId) {
            s.showMenu = false;
          }
        });
        this.drafts.forEach(d => {
          if (d.usecaseId === usecaseId) {
            d.showMenu = false;
          }
        });

        this.cdr.detectChanges();
        window.scrollTo({ top: 0 });
        this.loadAllStories(); // Refresh the list 
      },
      error: (err) => {
        console.error('API error:', err);
      }
    });
  }

  archivedStories() {
    this.router.navigate(['/archived']);
  }

  discardDraft(usecaseId: string) {
    this.usecaseService.discardDraft(usecaseId).subscribe({
      next: (res: string) => {
        console.log('API success, response:', res);
        this.showToast = false;
        this.toastTitle = 'Draft Discarded Successfully';
        //this.toastMessage = res;
        this.showToast = true;

        // ✅ Close the card menu for the discarded draft
        this.stories.forEach(s => {
          if (s.usecaseId === usecaseId) {
            s.showMenu = false;
          }
        });
        this.drafts.forEach(d => {
          if (d.usecaseId === usecaseId) {
            d.showMenu = false;
          }
        });

        this.cdr.detectChanges();
        window.scrollTo({ top: 0 });
        this.loadAllStories(); // Refresh the list 
      },
      error: (err) => {
        console.error('API error:', err);
      }
    });
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

  getIndustryImage(){
    const name = this.industryName;
    const industry = this.viewIndustryOverview.find(item => item.name === name);
    return industry ? industry.defaultImage : 'assets/cards.png';
  }

  getValueChainImage(){
    const name = this.subIndustryName;
    const chain = this.viewValueChain.find(item => item.name === name);
    return chain ? chain.defaultImage : 'assets/cards.png';
  }

  getBreadcrumbTitle(): string {
    // Case 1: all three are "All"
    if (this.selectedIndustryId === -1 && this.selectedSubIndustryId === -1 && this.selectedValueChainId === -1) {
      return 'All Stories';
    }

    let parts: string[] = [];

    // Case 2: Industry = All, but others chosen
    if (this.selectedIndustryId === -1) {
      parts.push('All');
    } else if (this.industryName && this.industryName !== 'All') {
      parts.push(this.industryName);
    }

    if (this.selectedSubIndustryId !== -1 && this.subIndustryName && this.subIndustryName !== 'All') {
      parts.push(this.subIndustryName);
    }

    if (this.selectedValueChainId !== -1 && this.valueChainName && this.valueChainName !== 'All') {
      parts.push(this.valueChainName);
    }

    return parts.join(' > ') || 'All Stories';
  }

  openIndustryModal() {
    console.log("clicked chchchc")
    this.showIndustryOverview = true;
  }

  openValueChainModal() {
    this.showValueChain = true;
  }
}
