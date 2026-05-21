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
import { AppStateService } from '../../../../core/services/app-state.service';

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
  filtered: any;
  subIndustryName: any;
  industryName: any;
  valueChainName: any;
  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService, private cdr: ChangeDetectorRef,
    private route: ActivatedRoute, private homePageService: HomePageService, private ngZone: NgZone, private appStateService: AppStateService) {

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

    this.loadIndustries();
    this.homePageForm.get('clientStories')?.disable();
    this.getHomePageConfigDetails();
  }


  getHomePageConfigDetails(): void {
    this.isDataLoading.set(true);
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
            this.featuredStories = stories?.map(story => ({
              ...story,
              industryName: this.getIndustryName(story?.industryId),
              subIndustryName: this.getSubIndustryName(story?.subIndustryId),
              tags: story?.tag || []
            }));
            this.cdr.detectChanges();
          });
        }

        // --- Industries + thumbnails ---
        const industriesArray = this.homePageForm.get('industries') as FormArray;
        industriesArray.clear();
        this.industryPreviews = [];

        const industryGroups = this.allIndustries.map((ind, i) => {
          const backendThumb = res?.industryThumbnails?.find(
            (t: any) => t.industryId === ind?.industryId
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
        this.isDataLoading.set(false);
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Fetch failed', err)
        this.isDataLoading.set(false);
      }
    });
  }

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

  // removeStory(index: number) {
  //   const featuredStoriesArray = this.homePageForm.get('featuredStories') as FormArray;

  //   // remove from local array
  //   this.featuredStories.splice(index, 1);

  //   // remove from FormArray
  //   featuredStoriesArray.removeAt(index);

  //   console.log("featured stories after delete::", this.featuredStories);
  // }

  removeStory(index: number) {
  this.featuredStories.splice(index, 1);

  const featuredStoriesArray = this.homePageForm.get('featuredStories') as FormArray;
  featuredStoriesArray.clear();

  this.featuredStories.forEach(story => {
    featuredStoriesArray.push(this.fb.group({ usecaseId: story.usecaseId }));
  });

  console.log("featured stories after delete::", this.featuredStories);
}


  loadAllStories(page: number = 1, size: number = 10) {
    this.usecaseService.getUsecasesByStatus('APPROVED', page, size).subscribe((res: any) => {
      let allStories = res.content || [];

      this.stories = allStories.map((story: any) => ({
        ...story,
        tags: story.tag,
        //ownerName: this.getOwnerName(story.ownerEId),
        industryName: this.getIndustryName(story.industryId),
        subIndustryName: this.getSubIndustryName(story.subIndustryId)
      }));

      this.filteredStories = this.stories
        .filter((story: any) => story.status === 'APPROVED')
      // .map((archived: any) => ({
      //   ...archived,
      //   ownerName: this.getOwnerName(archived.ownerEId)
      // }));
      console.log("ApprovedStories::", this.filteredStories);
      this.applyFilters();
      this.currentPage = page;
      this.totalPages = res.totalPages;
      this.totalElements = res.totalElements;
      this.pageSize = res.size;
      this.filteredStories = [...this.filteredStories];
      this.cdr.detectChanges();
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

    this.filteredStories = [...this.filtered];
    console.log("filtered stories:::", this.filteredStories)
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
        this.appStateService.setApplicationName(this.homePageForm.value.applicationName);
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
        // homepage-conf.ts (inside success callback)
        this.appStateService.setApplicationName(this.homePageForm.value.applicationName);

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