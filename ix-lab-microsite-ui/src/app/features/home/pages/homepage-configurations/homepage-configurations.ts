import { ChangeDetectorRef, Component } from '@angular/core';
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
@Component({
  selector: 'app-homepage-configurations',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, CustomDropdownComponent, ToasterComponent, RouterModule],
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
  featuredStories: any[] = [];
  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService, private cdr: ChangeDetectorRef,
    private route: ActivatedRoute) {

    this.homePageForm = this.fb.group({
      applicationName: [''],
      title: [''],
      subtitle: [''],
      heroImage: [''],

      clientStories: [''],
      mesMomSolutions: [''],
      productionSupport: [''],
      sapEwmPrograms: [''],
      sapEwmProgramsTbd: [''],

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
    capabilities: false
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
    const industriesArray = this.homePageForm.get('industries') as any;
    const industryNames = [
      { name: 'Consumer Package Goods', defaultImage: 'assets/CPG.png' },
      { name: 'Life Sciences', defaultImage: 'assets/Life Sciences.png' },
      { name: 'Industrials', defaultImage: 'assets/Industrials.png' },
      { name: 'Energy', defaultImage: 'assets/Energy.png' },
      { name: 'Utilities', defaultImage: 'assets/Utilities.jpg' },
      { name: 'Chemicals & Natural Services', defaultImage: 'assets/Chemical and Natural Resources.png' },
      { name: 'High Tech', defaultImage: 'assets/High Tech Industry.png' }
    ];


    industryNames.forEach(ind => {
      industriesArray.push(this.fb.group({
        name: [ind.name],
        fileName: [''],
        defaultImage: [ind.defaultImage]
      }));
    });
    this.loadIndustries();
  }


  get industriesArray(): FormArray {
    return this.homePageForm.get('industries') as FormArray;
  }

  // Toggle function with "only one open at a time" behavior
  toggleAccordion(section: 'hero' | 'overview' | 'industries' | 'featured' | 'capabilities') {
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


  get featuredSlots() {
    const slots = [...this.featuredStories];
    while (slots.length < 4) {
      slots.push(null);
    }
    return slots;
  }

  openStoryModal() {
    this.showStoryModal = true;
    this.loadAllStories(this.currentPage);
  }

  selectStory(story: any) {
    const alreadySelected = this.featuredStories.some(
      (s) => s.usecaseId === story.usecaseId
    );

    if (alreadySelected) {
      alert("This story is already selected!");
      return;
    }

    if (this.featuredStories.length >= 4) {
      console.warn("You can only select up to 4 stories.");
      return;
    }

    this.featuredStories.push(story);
    this.showStoryModal = false;
  }

  removeStory(index: number) {
    this.featuredStories.splice(index, 1);
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


  onSaveChanges() {
    const payload = new FormData();

    // Add simple text fields
    Object.keys(this.homePageForm.value).forEach(key => {
      if (key !== 'industries' && key !== 'featuredStories') {
        payload.append(key, this.homePageForm.value[key]);
      }
    });

    // Add hero + keyCapabilities images
    if (this.imageBlobs.heroImage) {
      payload.append('heroImage', this.imageBlobs.heroImage, this.homePageForm.value.heroImage);
    }
    if (this.imageBlobs.keyCapabilitiesImage) {
      payload.append('keyCapabilitiesImage', this.imageBlobs.keyCapabilitiesImage, this.homePageForm.value.keyCapabilitiesImage);
    }

    // Add industries images
    this.industriesArray.controls.forEach((control, i) => {
      if (this.industryBlobs[i]) {
        payload.append(`industries[${i}].file`, this.industryBlobs[i], control.value.fileName);
      }
      payload.append(`industries[${i}].name`, control.value.name);
      payload.append(`industries[${i}].defaultImage`, control.value.defaultImage);
    });

    // ✅ Make FormData readable in console
    for (const [key, value] of payload.entries()) {
      if (value instanceof File) {
        console.log(`${key}: File -> name=${value.name}, size=${value.size} bytes, type=${value.type}`);
      } else {
        console.log(`${key}: ${value}`);
      }
    }

    // Example POST
    this.http.post('/api/homepage-config', payload).subscribe(res => {
      console.log('Saved successfully', res);
    });
  }



}