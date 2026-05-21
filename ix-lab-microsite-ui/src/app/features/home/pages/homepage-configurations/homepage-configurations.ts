import { ChangeDetectorRef, Component, signal, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, FormsModule, ReactiveFormsModule, FormBuilder, FormArray } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { ToasterComponent } from '../../../../shared/components/toaster/toaster';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';
import { HttpClient } from '@angular/common/http';
import { NumericPlusDirective } from '../../../../shared/directives/numeric-plus';
import { HomePageService } from '../../../../core/services/home-page.service';
import { forkJoin, Observable, of } from 'rxjs';
import { Spinner } from '../../../../shared/components/spinner/spinner';

export interface Story {
  usecaseId: number;
  industryId: number;
  subIndustryId: number;
  valueChainId: number;
  title: string;
  thumbnailImageUrl: string;
  description: string;
  tags: string[];
  industryName?: string;
  subIndustryName?: string;
}

interface FeaturedStory {
  usecaseId: number;
}

interface IndustryThumbnail {
  industryId: number;
}

@Component({
  selector: 'app-homepage-configurations',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, CustomDropdownComponent, ToasterComponent, RouterModule,
    NumericPlusDirective, Spinner
  ],
  templateUrl: './homepage-configurations.html',
  styleUrls: ['./homepage-configurations.scss'],
})
export class HomepageConfigurationsComponent {
  downArrowIconPath = 'assets/icons/down_arrow.png';
  upArrowIconPath = 'assets/icons/arrow-up.png';
  exchangeIcon = 'assets/icons/exchange.png';



  homePageForm: FormGroup;
  industries: any[] = [];
  subIndustries: any[] = [];
  valueChains: any[] = [];
  showStoryModal = false;

  // Single image fields
  imagePreviews: { heroImage: string | null; keyCapabilitiesImage: string | null } = {
    heroImage: null,
    keyCapabilitiesImage: null
  };
  imageBlobs: { heroImage: Blob | null; keyCapabilitiesImage: Blob | null } = {
    heroImage: null,
    keyCapabilitiesImage: null
  };

  // Industries: 7 slots
  industryPreviews: string[] = Array(7).fill(null);
  industryBlobs: Blob[] = Array(7).fill(null);

  currentPage = 1;
  totalPages = 0;
  pageSize = 10;
  totalElements = 0;
  isLoading: boolean = true;
  showAdminControls = false;
  stories: any[] = [];
  filteredStories: any[] = [];
  allIndustries: any[] = [];
  allSubIndustries: any[] = [];
  allValueChains: any[] = [];
  cardCategoryTitle: any;
  selectedIndustryId: number | null = null;
  selectedSubIndustryId: number | null = null;
  selectedValueChainId: number | null = null;
  pagination_right = 'assets/icons/pagination_right.png';
  pagination_left = 'assets/icons/pagination_left.png';
  searchTerm: any;
  featuredStories: Story[] = [];
  getHomePageDetails: any;
  showToast = false;
  toastMessage = '';
  toastTitle = '';
  editorConfig = {
    toolbar: [
      ['bold', 'italic', 'underline'],
      // [{ list: 'ordered' }, { list: 'bullet' }],
      // [{ align: [] }]
    ]
  };
  isDataLoading = signal(false);
  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService, private cdr: ChangeDetectorRef,
    private route: ActivatedRoute, private homePageService: HomePageService, private ngZone: NgZone) {

    this.homePageForm = this.fb.group({
      applicationName: [''],
      title: [''],
      subtitle: [''],
      heroImage: [''],

      clientStories: ['150+'],
      mesMomSolutions: [''],
      productionSupport: [''],
      sapEwmPrograms: [''],
      sapEwmProgramsTbd: [''],
      nvidiaStorycount: [],

      industries: this.fb.array([]), // [{ name, fileName }]
      featuredStories: this.fb.array([]),
      keyCapabilitiesImage: ['']
    });



  }
  // Track which accordion is open
  isAccordionOpen = {
    hero: true,
    overview: false,
    industries: false,
    featured: false,
    capabilities: false,
    industryNamesConf: false
  };
  // industryNames = [
  //   { industryId: 1, name: 'Consumer Package Goods' },
  //   { industryId: 2, name: 'Life Sciences' },
  //   { industryId: 3, name: 'Energy' },
  //   { industryId: 4, name: 'Industrials' },
  //   { industryId: 5, name: 'Utilities' },
  //   { industryId: 6, name: 'Chemicals & Natural Services' },
  //   { industryId: 7, name: 'High Tech' }
  // ];

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      //this.showAdminControls = params['from'] === 'config';
      //this.currentFrom = this.route.snapshot.queryParams['from'] || 'stories';
      this.currentPage = params['page'] ? +params['page'] : 1;
      const id = params['id'];
      if (id) {
        console.log("selected story Id:", id);
      }
      this.loadAllStories(this.currentPage);
    });


    // const industriesArray = this.homePageForm.get('industries') as FormArray;
    // this.industryNames.forEach(ind => {
    //   industriesArray.push(this.fb.group({
    //     industryId: [ind.industryId],
    //     name: [ind.name],
    //     fileName: [''],
    //     defaultImage: ['']   // keep blank
    //   }));
    // });

    this.loadIndustries();
    this.homePageForm.get('clientStories')?.disable();
    this.getHomePageConfigDetails();

  }




  getHomePageConfigDetails(): void {
    this.homePageService.getHomePageData().subscribe({
      next: (res: any) => {
        console.log('fetched successfully', res);
        this.getHomePageDetails = res;

        // Prepare story requests
        const storyRequests: Observable<any>[] = res.featuredStories?.map((s: any) =>
          this.usecaseService.getStoryDetailsById(s.usecaseId)
        ) || [];

        if (storyRequests.length > 0) {
          forkJoin(storyRequests).subscribe((stories: any[]) => {
            this.featuredStories = stories.map(story => ({
              ...story,
              industryName: this.getIndustryName(story.industryId),
              subIndustryName: this.getSubIndustryName(story.subIndustryId),
              tags: story.tag || []
            }));
            this.cdr.detectChanges();
          });
        }

        // --- Industries + thumbnails ---
        const industriesArray = this.homePageForm.get('industries') as FormArray;
        industriesArray.clear();
        this.industryPreviews = [];

        const industryGroups = this.allIndustries.map((ind, i) => {
          const backendThumb = res.industryThumbnails?.find(
            (t: any) => t.industryId === ind.industryId
          );

          this.industryPreviews[i] = backendThumb ? backendThumb.industryThumbnailUrl : '';

          return this.fb.group({
            industryId: ind?.industryId,
            name: ind?.industryName,
            fileName: [''],
            defaultImage: [''],
            updatedName: ['']
          });
        });

        industryGroups.forEach(group => industriesArray.push(group));

        // --- Patch simple fields ---
        this.homePageForm.patchValue({
          applicationName: res.applicationName,
          title: res.title,
          subtitle: res.subTitle,
          mesMomSolutions: res.mesMomSolDelivered,
          productionSupport: res.prodSiteCriticalSupport,
          sapEwmPrograms: res.sapEwmPrgDelivered,
          sapEwmProgramsTbd: res.sapEwmProgramsTbd,
          nvidiaStorycount: res.nvidiaStorycount
        });

        this.imagePreviews['heroImage'] = res.heroImageUrl;
        this.imagePreviews['keyCapabilitiesImage'] = res.keyCapabilityConfigurationImage;

        this.getApprovedStoryCount();

        this.cdr.detectChanges();
      },
      error: err => console.error('Fetch failed', err)
    });
  }




  // getHomePageConfigDetails(): void {
  //   this.isDataLoading.set(true);
  //   this.homePageService.getHomePageData().subscribe({
  //     next: (res: any) => {
  //       console.log('fetched successfully', res);
  //       this.getHomePageDetails = res;

  //       const industriesArray = this.homePageForm.get('industries') as FormArray;
  //       industriesArray.clear();
  //       this.industryPreviews = [];

  //       // ✅ Wrap industry thumbnail resolution in forkJoin
  //       const industryRequests = this.allIndustries.map(ind =>
  //         of(res.industryThumbnails?.find((t: any) => t.industryId === ind.industryId))
  //       );

  //       forkJoin(industryRequests).subscribe((thumbs: any[]) => {
  //         this.allIndustries.forEach((ind, i) => {
  //           const backendThumb = thumbs[i];

  //           industriesArray.push(this.fb.group({
  //             industryId: ind.industryId,
  //             name: ind.industryName,
  //             fileName: [''],
  //             defaultImage: [''],
  //             updatedName: [ind.name]
  //           }));

  //           this.industryPreviews[i] = backendThumb ? backendThumb.industryThumbnailUrl : '';
  //         });

  //         // // ✅ Patch form only after industries + previews are ready
  //         // this.homePageForm.patchValue({
  //         //   applicationName: res.applicationName,
  //         //   title: res.title,
  //         //   subtitle: res.subTitle,
  //         //   mesMomSolutions: res.mesMomSolDelivered,
  //         //   productionSupport: res.prodSiteCriticalSupport,
  //         //   sapEwmPrograms: res.sapEwmPrgDelivered,
  //         //   sapEwmProgramsTbd: res.sapEwmProgramsTbd,
  //         //   nvidiaStorycount: res.nvidiaStorycount
  //         // });

  //         // this.imagePreviews['heroImage'] = res.heroImageUrl;
  //         // this.imagePreviews['keyCapabilitiesImage'] = res.keyCapabilityConfigurationImage;

  //         // this.cdr.detectChanges();

  //         this.ngZone.run(() => {
  //           this.homePageForm.patchValue({
  //             applicationName: res.applicationName,
  //             title: res.title,
  //             subtitle: res.subTitle,
  //             mesMomSolutions: res.mesMomSolDelivered,
  //             productionSupport: res.prodSiteCriticalSupport,
  //             sapEwmPrograms: res.sapEwmPrgDelivered,
  //             sapEwmProgramsTbd: res.sapEwmProgramsTbd,
  //             nvidiaStorycount: res.nvidiaStorycount
  //           }, { emitEvent: true });

  //           this.imagePreviews['heroImage'] = res.heroImageUrl;
  //           this.imagePreviews['keyCapabilitiesImage'] = res.keyCapabilityConfigurationImage;

  //           this.cdr.markForCheck();   // <-- use markForCheck instead of detectChanges
  //         });


  //         const featuredStoriesArray = this.homePageForm.get('featuredStories') as FormArray;
  //         featuredStoriesArray.clear();

  //         if (res.featuredStories?.length > 0) {
  //           res.featuredStories.forEach((story: any) => {
  //             featuredStoriesArray.push(this.fb.group({ usecaseId: story.usecaseId }));
  //           });

  //           // Example: hardcoded test with one ID
  //           // const storyRequests = [
  //           //   this.usecaseService.getStoryDetailsById('10163')
  //           // ];
  //           const storyRequests = res.featuredStories.map((s: any) =>
  //             this.usecaseService.getStoryDetailsById((s.usecaseId))
  //           );
  //           console.log("req::", storyRequests);

  //           forkJoin<any[]>(storyRequests).subscribe((stories: any[]) => {
  //             this.featuredStories = stories.map(story => ({
  //               ...story,
  //               industryName: this.getIndustryName(story.industryId),
  //               subIndustryName: this.getSubIndustryName(story.subIndustryId),
  //               tags: story.tag || []
  //             }));
  //             this.cdr.detectChanges();
  //           });
  //         }

  //         this.getApprovedStoryCount();
  //         this.cdr.detectChanges();
  //       });
  //       this.isDataLoading.set(false);
  //     },
  //     error: err => {
  //       console.error('Fetch failed', err) 
  //       this.isDataLoading.set(false);
  //     }
  //   });
  //   //this.isDataLoading.set(false);
  //    this.cdr.detectChanges();
  // }




  getApprovedStoryCount(): void {
    this.homePageService.getApprovedUsecaseCount().subscribe({
      next: (res: any) => {
        const approvedCount = Number(res["Total Approved usecases"] || 0);
        const nvidiaCount = Number(this.getHomePageDetails?.nvidiaStorycount || 0);

        const clientStoriesCtrl = this.homePageForm.get('clientStories');
        clientStoriesCtrl?.enable({ emitEvent: false });
        clientStoriesCtrl?.setValue((approvedCount + nvidiaCount).toString(), { emitEvent: true });
        clientStoriesCtrl?.disable({ emitEvent: false });

        this.cdr.markForCheck();
      },
      error: err => console.error('Fetch failed', err)
    });
  }



  // get industriesArray(): FormArray {
  //   return this.homePageForm.get('industries') as FormArray;
  // }

  get industriesArray(): FormArray<FormGroup> {
    return this.homePageForm.get('industries') as FormArray<FormGroup>;
  }

  // Toggle function with "only one open at a time" behavior
  toggleAccordion(section: 'hero' | 'overview' | 'industries' | 'featured' | 'capabilities' | 'industryNamesConf') {
    const currentlyOpen = this.isAccordionOpen[section];

    // Close all
    // this.isAccordionOpen.hero = false;
    // this.isAccordionOpen.overview = false;
    // this.isAccordionOpen.industries = false;
    // this.isAccordionOpen.featured = false;
    // this.isAccordionOpen.capabilities = false;

    // Reopen only if it wasn’t already open
    this.isAccordionOpen[section] = !currentlyOpen;
  }


  get featuredSlots(): (Story | null)[] {
    const slots: (Story | null)[] = [...this.featuredStories];
    while (slots.length < 4) {
      slots.push(null);
    }
    return slots;
  }

  openStoryModal() {
    this.showStoryModal = true;
    this.loadAllStories(this.currentPage);
  }

  selectStory(story: Story): void {
    const featuredStoriesArray = this.homePageForm.get('featuredStories') as FormArray;
    const alreadySelected = featuredStoriesArray.value.some(
      (s: any) => s.usecaseId === story.usecaseId
    );
    if (alreadySelected) {
      alert("This story is already selected!");
      return;
    }

    if (featuredStoriesArray.length >= 4) {
      console.warn("You can only select up to 4 stories.");
      return;
    }
    featuredStoriesArray.push(this.fb.group({ usecaseId: story.usecaseId }));
    this.featuredStories.push(story);
    console.log("selected featured stories::", featuredStoriesArray.value);
    this.showStoryModal = false;
  }

  removeStory(index: number) {
    const featuredStoriesArray = this.homePageForm.get('featuredStories') as FormArray;

    // remove from local array
    this.featuredStories.splice(index, 1);

    // remove from FormArray
    featuredStoriesArray.removeAt(index);

    console.log("featured stories after delete::", this.featuredStories);
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
        //ownerName: this.getOwnerName(story.ownerEId),
        industryName: this.getIndustryName(story.industryId),
        subIndustryName: this.getSubIndustryName(story.subIndustryId)
      }));

      this.filteredStories = this.stories;
      console.log("is active stories::", this.filteredStories)
      this.applyFilters();

      this.currentPage = page;
      this.totalPages = res.totalPages;
      this.totalElements = res.totalElements;
      this.pageSize = res.size;
      this.cdr.detectChanges();
      //console.log('Stories loaded:', this.stories);
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

  loadIndustries() {
    this.industryService.getIndustries().subscribe((res: any) => {

      this.allIndustries = res.industries;

      this.allSubIndustries = res.subIndustries;
      this.allValueChains = res.valueChains;

      // ✅ Populate industries FormArray dynamically
      const industriesArray = this.homePageForm.get('industries') as FormArray;
      industriesArray.clear();
      this.industryPreviews = [];

      this.allIndustries
        .filter(ind => ind.isActive) // only active industries
        .forEach((ind, i) => {
          industriesArray.push(this.fb.group({
            industryId: [ind.industryId],
            name: [ind.industryName],
            fileName: [''],
            defaultImage: ['']
          }));
          this.industryPreviews[i] = ''; // initialize preview slot
        });

      this.industries = [
        { industryId: null, industryName: 'All' },
        ...this.allIndustries
      ];

      this.subIndustries = [
        { subIndustryId: null, subIndustryName: 'All' },
        ...this.allSubIndustries
      ];

      this.valueChains = [
        { valueChainId: null, valueChainName: 'All' },
        ...this.allValueChains
      ];

      this.cardCategoryTitle = this.route.snapshot.queryParams['title'];
      const title = this.route.snapshot.queryParams['title'];
      if (title) {
        const selectedIndustryObj = this.industries.find(
          ind => ind.industryName === title
        );
        if (selectedIndustryObj) {
          this.selectedIndustryId = selectedIndustryObj.industryId;
          this.applyFilters();
          this.cdr.detectChanges();
        }
      }

    });
  }

  onIndustrySelected(industry: any) {

    if (!industry || !industry.industryId) {
      this.selectedIndustryId = null;
      this.selectedSubIndustryId = null;
      this.selectedValueChainId = null;
      this.subIndustries = [
        { subIndustryId: null, subIndustryName: 'All' },
        ...this.allSubIndustries
      ];

      this.valueChains = [
        { valueChainId: null, valueChainName: 'All' },
        ...this.allValueChains
      ];

      this.applyFilters();
      return;
    }

    this.selectedIndustryId = industry.industryId;
    this.selectedSubIndustryId = null;
    this.selectedValueChainId = null;
    this.subIndustries = [
      { subIndustryId: null, subIndustryName: 'All' },
      ...this.allSubIndustries.filter(
        sub => sub.industryId === industry.industryId
      )
    ];

    this.valueChains = [
      { valueChainId: null, valueChainName: 'All' }
    ];
    this.applyFilters();
  }

  onSubIndustrySelected(sub: any) {

    if (!sub || !sub.subIndustryId) {
      this.selectedSubIndustryId = null;
      this.selectedValueChainId = null;
      this.valueChains = [{ valueChainId: null, valueChainName: 'All' }, ...this.allValueChains];
      this.applyFilters();
      return;
    }
    this.selectedSubIndustryId = sub.subIndustryId;
    this.selectedValueChainId = null;
    this.valueChains = [
      { valueChainId: null, valueChainName: 'All' },
      ...this.allValueChains.filter(
        vc => vc.subIndustryId === sub.subIndustryId
      )
    ];
    this.applyFilters();
  }

  onValueChainSelected(vc: any) {
    if (!vc || !vc.valueChainId) {
      this.selectedValueChainId = null;
      this.applyFilters();
      return;
    }

    this.selectedValueChainId = vc.valueChainId;
    this.applyFilters();
  }

  applyFilters() {
    let filtered = [...this.stories];

    // Industry filter
    if (this.selectedIndustryId) {
      filtered = filtered.filter(story => story.industryId === this.selectedIndustryId);
    }

    // Sub-Industry filter
    if (this.selectedSubIndustryId) {
      filtered = filtered.filter(story => story.subIndustryId === this.selectedSubIndustryId);
    }

    // Value Chain filter
    if (this.selectedValueChainId) {
      filtered = filtered.filter(story => story.valueChainId === this.selectedValueChainId);
    }

    // Search filter (case-insensitive)
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(story =>
        (story.title && story.title.toLowerCase().includes(term)) ||
        (story.description && story.description.toLowerCase().includes(term)) ||
        (story.tag && story.tag.some((t: any) => t.toLowerCase().includes(term)))
      );
    }

    this.filteredStories = filtered;
    console.log("filtered stories::", this.filteredStories);
  }

  updateImage(fieldName: 'heroImage' | 'keyCapabilitiesImage' | 'industries', index?: number) {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.jpg,.jpeg,.png';

    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (!file) return;

      if (file.size > 1024 * 1024 * 10) {
        console.error('Invalid file. Must be .jpg/.jpeg/.png under 1MB.');
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        if (fieldName === 'industries' && index !== undefined) {
          // ✅ Use industry-specific arrays
          this.industryBlobs[index] = file;
          this.industryPreviews[index] = reader.result as string;

          const industriesArray = this.homePageForm.get('industries') as any;
          industriesArray.at(index).patchValue({ fileName: file.name });
        } else {
          // ✅ Use single-image fields
          this.imageBlobs[fieldName as 'heroImage' | 'keyCapabilitiesImage'] = file;
          this.imagePreviews[fieldName as 'heroImage' | 'keyCapabilitiesImage'] = reader.result as string;
          this.homePageForm.patchValue({ [fieldName]: file.name });
        }
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    };

    input.click();
  }


  private stripPlus(val: string): string {
    if (!val) return '';
    return val.toString().replace('+', '');
  }

  onSaveChanges(): void {
    const payload = new FormData();
    const homePageRequest = {
      applicationName: this.homePageForm.value.applicationName,
      title: this.homePageForm.value.title,
      subTitle: this.homePageForm.value.subtitle,
      mesMomSolDelivered: this.stripPlus(this.homePageForm.value.mesMomSolutions),
      prodSiteCriticalSupport: this.stripPlus(this.homePageForm.value.productionSupport),
      sapEwmPrgDelivered: this.stripPlus(this.homePageForm.value.sapEwmPrograms),
      nvidiaStoriesCount: this.stripPlus(this.homePageForm.value.nvidiaStorycount),
      //sapEwmProgramsTbd: this.stripPlus(this.homePageForm.value.sapEwmProgramsTbd),
      updatedById: JSON.parse(localStorage.getItem('loggedUser') || '{}').userId, // or from your context
      featuredStories: (this.homePageForm.value.featuredStories || []).map((s: { usecaseId: number }) => ({
        usecaseId: s.usecaseId
      })),

      industryThumbnails: (this.industriesArray.controls || [])
        .filter((control: any, i: number) => this.industryPreviews[i] || this.industryBlobs[i])
        .map((control: any) => ({
          industryId: control.value.industryId
        })),

      // ✅ Names payload
      industryNames: (this.industriesArray.controls || [])
        .map((control: any) => ({
          industryId: control.value.industryId,
          industryName: control.value.name,
          updatedIndustryName: control.value.updatedName
        }))

    };

    console.log("page request::", homePageRequest)
    payload.append('homePageRequest', JSON.stringify(homePageRequest));

    if (this.imageBlobs.heroImage) {
      payload.append('heroImageUrl', this.imageBlobs.heroImage, this.homePageForm.value.heroImage);
    }
    if (this.imageBlobs.keyCapabilitiesImage) {
      payload.append('keyCapConfigUrl', this.imageBlobs.keyCapabilitiesImage, this.homePageForm.value.keyCapabilitiesImage);
    }

    // this.industriesArray.controls.forEach((control: any, i: number) => {
    //   if (this.industryBlobs[i]) {
    //     payload.append(`industrythumbnailUrl[${i}]`, this.industryBlobs[i], control.value.fileName);
    //   }
    // });

    this.industriesArray.controls.forEach((control: any, i: number) => {
      if (this.industryBlobs[i]) {
        payload.append('industrythumbnailUrl', this.industryBlobs[i], control.value.fileName);
      }
    });
    for (const [key, value] of payload.entries()) {
      if (value instanceof File) {
        console.log(`${key}: File -> name=${value.name}, size=${value.size} bytes, type=${value.type}`);
      } else {
        console.log(`${key}: ${value}`);
      }
    }

    this.isDataLoading.set(true);
    this.homePageService.saveHomePageConfig(payload).subscribe({
      next: res => {

        console.log('Saved successfully', res);
        this.toastTitle = 'Saved successfully';
        this.showToast = true;
        this.isDataLoading.set(false);
        this.cdr.detectChanges();
        this.router.navigate(['/home']);   // ✅ redirect after success
      },
      error: err => {
        this.isDataLoading.set(false);
        console.error('Save failed', err)
        this.toastTitle = 'Save Failed';
        this.showToast = true;
        this.cdr.detectChanges();
      }
    });
    this.showToast = false;
  }


  onUpdate(): void {
    const payload = new FormData();

    const homePageConfig = {
      applicationName: this.homePageForm.value.applicationName,
      title: this.homePageForm.value.title,
      subTitle: this.homePageForm.value.subtitle,
      mesMomSolDelivered: this.stripPlus(this.homePageForm.value.mesMomSolutions),
      prodSiteCriticalSupport: this.stripPlus(this.homePageForm.value.productionSupport),
      sapEwmPrgDelivered: this.stripPlus(this.homePageForm.value.sapEwmPrograms),
      nvidiaStoriesCount: this.stripPlus(this.homePageForm.value.nvidiaStorycount),
      updatedById: JSON.parse(localStorage.getItem('loggedUser') || '{}').userId,
      featuredStories: (this.homePageForm.value.featuredStories || []).map((s: { usecaseId: number }) => ({
        usecaseId: s.usecaseId
      })),
      // ✅ Always send industry IDs
      industryThumbnails: (this.industriesArray.controls || [])
        .filter((control: any, i: number) => this.industryPreviews[i] || this.industryBlobs[i])
        .map((control: any) => ({
          industryId: control.value.industryId
        })),

      // ✅ Names payload
      industryNames: (this.industriesArray.controls || [])
        .map((control: any) => ({
          industryId: control.value.industryId,
          industryName: control.value.name,
          updatedIndustryName: control.value.updatedName
        }))
    };

    // ✅ Append JSON metadata
    payload.append('homePageConfig', JSON.stringify(homePageConfig));

    // ✅ Hero image: either binary OR url
    if (this.imageBlobs.heroImage) {
      payload.append('heroImageFile', this.imageBlobs.heroImage, this.homePageForm.value.heroImage);
      payload.append('heroImageFileUrls', ''); // blank since new file replaces old
    } else if (this.imagePreviews['heroImage']) {
      payload.append('heroImageFileUrls', this.imagePreviews['heroImage']);
    }

    // ✅ Key Capabilities image: either binary OR url
    if (this.imageBlobs.keyCapabilitiesImage) {
      payload.append('keyCapConfigFile', this.imageBlobs.keyCapabilitiesImage, this.homePageForm.value.keyCapabilitiesImage);
      payload.append('keyCapConfigFileUrls', '');
    } else if (this.imagePreviews['keyCapabilitiesImage']) {
      payload.append('keyCapConfigFileUrls', this.imagePreviews['keyCapabilitiesImage']);
    }

    // ✅ Industry thumbnails: each slot either binary OR url
    this.industriesArray.controls.forEach((control: any, i: number) => {
      if (this.industryBlobs[i]) {
        payload.append('industryThumbnailFiles', this.industryBlobs[i], control.value.fileName);
        //payload.append('industryThumbnailFilesUrls', ''); // blank since new file replaces old
      } else if (this.industryPreviews[i]) {
        payload.append('industryThumbnailFilesUrls', this.industryPreviews[i]);
      }
    });

    // Debug log
    for (const [key, value] of payload.entries()) {
      if (value instanceof File) {
        console.log(`${key}: File -> name=${value.name}, size=${value.size} bytes, type=${value.type}`);
      } else {
        console.log(`${key}: ${value}`);
      }
    }
    this.isDataLoading.set(true);
    this.homePageService.updateHomePageConfig(payload).subscribe({
      next: res => {
        console.log('Updated successfully', res);
        this.toastTitle = 'Updated successfully';
        this.showToast = true;
        this.isDataLoading.set(false);
        this.cdr.detectChanges();
        this.router.navigate(['/home']);   // ✅ redirect after success
      },
      error: err => {
        this.isDataLoading.set(false);
        console.error('update failed', err)
        this.toastTitle = 'Update Failed';
        this.showToast = true;
        this.cdr.detectChanges();
      }
    });
    this.showToast = false;
  }

  onSaveClick() {
    this.getHomePageDetails ? this.onUpdate() : this.onSaveChanges();
  }

}