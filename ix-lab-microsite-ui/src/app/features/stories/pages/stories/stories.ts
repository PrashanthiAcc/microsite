import { Component, HostListener, OnInit, signal } from '@angular/core';
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
import { Spinner } from '../../../../shared/components/spinner/spinner';
import { forkJoin } from 'rxjs';


@Component({
  selector: 'app-stories',
  standalone: true,
  imports: [CommonModule, CustomDropdownComponent, RouterModule, ToasterComponent, FormsModule, Spinner],
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
  allDrafts: any[] = [];
  
inReviewStories: any[] = [];
approvedStories: any[] = [];

filteredDrafts: any[] = [];
filteredInReview: any[] = [];
filteredApproved: any[] = [];

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
  viewIndustryOverview = [
    { name: 'Life Sciences', defaultImage: 'assets/LifeScience Industry Overview.png' },
  ];
  viewValueChain = [
    { name: 'Pharmaceuticals', defaultImage: 'assets/Pharmaceuticals Value Chain.png' },
    //{ name: 'Med Tech', defaultImage: 'assets/Life Sciences.png' },
  ];
  totalDrafts: any;
  totalInReview: any;
  totalApproved: any;
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


  // loadAllStories(page: number = 1, size: number = 10) {
  //   this.isDataLoading.set(true);
  //   this.isLoading = false;
  //   this.usecaseService.getAllStories(page, size).subscribe({
  //     next: (res: any) => {
  //       let allStories = res.content || [];

  //       if (!this.showAdminControls) {
  //         allStories = allStories.filter((story: any) => story.isActive == true);
  //         if (allStories.length > 0) {
  //           this.isLoading = true;
  //         } else {
  //           this.isLoading = false;
  //         }
  //       }

  //       this.isLoading = false;

  //       // Sort by updatedDate DESC (latest first)
  //       allStories = allStories.sort((a: any, b: any) => {
  //         const dateA = a.updatedDate ? new Date(a.updatedDate).getTime() : new Date(a.createdDate).getTime();
  //         const dateB = b.updatedDate ? new Date(b.updatedDate).getTime() : new Date(b.createdDate).getTime();
  //         return dateB - dateA; // latest first
  //       });

  //       this.stories = allStories.map((story: any) => ({
  //         ...story,
  //         tags: story.tag,
  //         ownerName: this.getOwnerName(story.ownerEId),
  //         industryName: this.getIndustryName(story.industryId),
  //         subIndustryName: this.getSubIndustryName(story.subIndustryId),
  //         valueChainName: this.getValueChainName(story.valueChainId)
  //       }));

  //       this.filteredStories = this.stories;
  //       this.applyFilters();
  //       if (this.showAdminControls && this.currentFrom !== 'stories') {
  //         this.filteredStories = this.stories.filter((story: any) => story.status === 'IN_REVIEW');

  //         this.drafts = allStories
  //           .filter((story: any) => story.status === 'DRAFT')
  //           .map((draft: any) => ({
  //             ...draft,
  //             tags: draft.tag,
  //             ownerName: this.getOwnerName(draft.ownerEId),
  //             industryName: this.getIndustryName(draft.industryId),
  //             subIndustryName: this.getSubIndustryName(draft.subIndustryId),
  //             valueChainName: this.getValueChainName(draft.valueChainId)
  //           }));
  //       }

  //       this.currentPage = page;
  //       this.totalPages = res.totalPages;
  //       this.totalElements = res.totalElements;
  //       this.pageSize = res.size;
  //       this.isDataLoading.set(false);
  //       this.cdr.detectChanges();
  //     },
  //     error: (err: any) => {
  //        this.isDataLoading.set(false);
  //     }
  //     //console.log('Stories loaded:', this.stories);
  //   });
  // }



loadAllStories(page: number = 1, size: number = 10) {
  this.isDataLoading.set(true);

  forkJoin({
    drafts: this.usecaseService.getUsecasesByStatus('DRAFT', page, size),
    inReview: this.usecaseService.getUsecasesByStatus('IN_REVIEW', page, size),
    approved: this.usecaseService.getUsecasesByStatus('APPROVED', page, size)
  }).subscribe({
    next: ({ drafts, inReview, approved }) => {
      this.drafts = (drafts.content || []).map((d:any) => this.mapStory(d));
      this.inReviewStories = (inReview.content || []).map((s:any) => this.mapStory(s));
      this.approvedStories = (approved.content || []).map((a:any) => this.mapStory(a));

      // ✅ Hydrate filteredStories based on mode
      if (this.showAdminControls) {
        this.filteredStories = [...this.inReviewStories];
      } else {
        this.filteredStories = [...this.approvedStories];
      }

      this.applyFilters();

      // Shared pagination
      // Shared pagination
this.currentPage = page;
this.pageSize = size;
// this.totalDrafts = drafts.totalElements || this.totalDrafts;
// this.totalInReview = inReview.totalElements || this.totalInReview;
// this.totalApproved = approved.totalElements || this.totalApproved;

// this.totalElements = this.totalDrafts + this.totalInReview + this.totalApproved;
// this.totalPages = Math.ceil(this.totalElements / this.pageSize);
if (this.showAdminControls) {
  // Admin view → In Review
  this.totalElements = inReview.totalElements;
  this.totalPages = inReview.totalPages;
} else {
  // Normal view → Approved
  this.totalElements = approved.totalElements;
  this.totalPages = approved.totalPages;
}
// this.totalElements =
//   this.drafts.length +
//   this.inReviewStories.length +
//   this.approvedStories.length;

// this.totalPages = Math.ceil(this.totalElements / this.pageSize);

this.isLoading = false;
this.isDataLoading.set(false);
this.cdr.detectChanges();

      // Debug logs
      console.log('Drafts:', this.drafts.length);
      console.log('In Review:', this.inReviewStories.length);
      console.log('Approved:', this.approvedStories.length);
      console.log('FilteredStories:', this.filteredStories.length);
    },
    error: () => {
      this.isDataLoading.set(false);
      this.isLoading = false;
    }
  });
}



private mapStory(story: any) {
  return {
    ...story,
    tags: story.tag,
    ownerName: this.getOwnerName(story.ownerEId),
    industryName: this.getIndustryName(story.industryId),
    subIndustryName: this.getSubIndustryName(story.subIndustryId),
    valueChainName: this.getValueChainName(story.valueChainId)
  };
}

  // applyFilters() {
  //   this.filtered = [...this.stories];

  //   if (this.selectedIndustryId !== -1) {
  //     this.filtered = this.filtered.filter((story: any) => story.industryId === this.selectedIndustryId);
  //   }

  //   if (this.selectedSubIndustryId !== -1) {
  //     this.filtered = this.filtered.filter((story: any) => story.subIndustryId === this.selectedSubIndustryId);
  //   }

  //   if (this.selectedValueChainId !== -1) {
  //     this.filtered = this.filtered.filter((story: any) => story.valueChainId === this.selectedValueChainId);
  //   }

  //   this.filteredStories = this.filtered;

  //   if (this.showAdminControls && this.currentFrom !== 'stories') {

  //     this.drafts = this.filtered
  //       .filter((story: any) => story.status === 'DRAFT')
  //       .map((draft: any) => ({
  //         ...draft,
  //         tags: draft.tags,
  //         ownerName: this.getOwnerName(draft.ownerId),
  //         industryName: this.getIndustryName(draft.industryId),
  //         subIndustryName: this.getSubIndustryName(draft.subIndustryId)
  //       }));

  //     this.filteredStories = this.filtered.filter((story: any) => story.status === 'IN_REVIEW');
  //     // this.filtered = this.filteredStories;
  //   } else {
  //     this.filteredStories = [...this.filtered];
  //   }
  // }

applyFilters() {
  const filterFn = (story: any) =>
    (this.selectedIndustryId === -1 || story.industryId === this.selectedIndustryId) &&
    (this.selectedSubIndustryId === -1 || story.subIndustryId === this.selectedSubIndustryId) &&
    (this.selectedValueChainId === -1 || story.valueChainId === this.selectedValueChainId);

  this.filteredDrafts = this.drafts.filter(filterFn);
  this.filteredInReview = this.inReviewStories.filter(filterFn);
  this.filteredApproved = this.approvedStories.filter(filterFn);

  if (this.showAdminControls) {
    // ✅ Admin view: only In Review
    this.filteredStories = this.filteredInReview;
  } else {
    // ✅ Normal view: only Approved
    this.filteredStories = this.filteredApproved;
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
    this.isDataLoading.set(true);
    this.usecaseService.discardDraft(usecaseId).subscribe({
      next: (res: string) => {
        this.isDataLoading.set(false);
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
        this.isDataLoading.set(false);
        console.error('API error:', err);
      }
    });
  }

  // onSearch() {
  //   const value = this.searchText.trim().toLowerCase();

  //   if (this.showAdminControls && this.currentFrom !== 'stories') {
  //     this.filteredStories = this.stories.filter((story: any) => story.status === 'IN_REVIEW');
  //     this.filtered = this.filteredStories;
  //   } else {
  //     this.filtered = [...this.stories];
  //   }
  //   if (!value) {
  //     this.filteredStories = this.filtered;
  //     return;
  //   }

  //   this.filteredStories = this.filtered.filter((story: any) => {

  //     const title = (story.title || '').toLowerCase();
  //     const description = (story.description || '').toLowerCase();
  //     const ownerEId = (story.ownerEId || '').toLowerCase();
  //     const ownerName = (story.ownerName || '').toLowerCase();

  //     const tags = Array.isArray(story.tags)
  //       ? story.tags.join(' ').toLowerCase()
  //       : (story.tags || '').toLowerCase();

  //     return (
  //       title.includes(value) ||
  //       description.includes(value) || ownerEId.includes(value) || ownerName.includes(value) ||
  //       tags.includes(value)
  //     );
  //   });
  // }
onSearch() {
  const value = this.searchText.trim().toLowerCase();

  const searchFn = (story: any) => {
    const title = (story.title || '').toLowerCase();
    const description = (story.description || '').toLowerCase();
    const ownerEId = (story.ownerEId || '').toLowerCase();
    const ownerName = (story.ownerName || '').toLowerCase();
    const tags = Array.isArray(story.tags)
      ? story.tags.join(' ').toLowerCase()
      : (story.tags || '').toLowerCase();

    return (
      title.includes(value) ||
      description.includes(value) ||
      ownerEId.includes(value) ||
      ownerName.includes(value) ||
      tags.includes(value)
    );
  };

  if (!value) {
    this.applyFilters();
    return;
  }

  this.filteredDrafts = this.drafts.filter(searchFn);
  this.filteredInReview = this.inReviewStories.filter(searchFn);
  this.filteredApproved = this.approvedStories.filter(searchFn);

  if (this.showAdminControls) {
    // ✅ Admin view: only In Review
    this.filteredStories = this.filteredInReview;
  } else {
    // ✅ Normal view: only Approved
    this.filteredStories = this.filteredApproved;
  }
  //this.filteredStories = this.filteredInReview; // keep template binding intact
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

  getIndustryImage() {
    const name = this.industryName;
    const industry = this.viewIndustryOverview.find(item => item.name === name);
    return industry ? industry.defaultImage : 'assets/cards.png';
  }

  getValueChainImage() {
    const name = this.subIndustryName;
    const chain = this.viewValueChain.find(item => item.name === name);
    return chain ? chain.defaultImage : 'assets/cards.png';
  }

  isIndustryOverviewEnabled(): boolean {
    return !!this.industryName &&
      this.industryName !== 'All' &&
      this.viewIndustryOverview.some(item => item.name === this.industryName);
  }

  isValueChainEnabled(): boolean {
    return !!this.subIndustryName &&
      this.subIndustryName !== 'All' &&
      this.viewValueChain.some(item => item.name === this.subIndustryName);
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

  getDynamicTitle(): string {
    const parts: string[] = [];

    if (this.industryName && this.industryName !== 'All') {
      parts.push(this.industryName);
    }

    if (this.subIndustryName && this.subIndustryName !== 'All') {
      parts.push(this.subIndustryName);
    }

    if (this.valueChainName && this.valueChainName !== 'All') {
      parts.push(this.valueChainName);
    }

    if (parts.length === 0 && (!this.showAdminControls && this.currentFrom === 'stories')) {
      return 'All Stories';
    } else if (parts.length === 0 && (this.showAdminControls && this.currentFrom !== 'stories')) {
      return '';
    }

    return parts.join(' > ');
  }

  isFilterApplied(): boolean {
    return !!(
      (this.industryName && this.industryName !== 'All') ||
      (this.subIndustryName && this.subIndustryName !== 'All') ||
      (this.valueChainName && this.valueChainName !== 'All')
    );
  }

  favouriteStory(usecaseId: string) {
  this.usecaseService.favouriteUsecase(JSON.parse(localStorage.getItem('loggedUser') || '{}').userId, usecaseId).subscribe({
    next: (res) => {
      console.log('API success, response:', res);

      this.toastTitle = 'Story Added To Favorite Tab.';
      this.toastMessage = res.message; // ✅ now valid
      this.showToast = true;

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
      this.loadAllStories();
    },
    error: (err) => {
      console.error('API error:', err);
    }
  });
}

}
