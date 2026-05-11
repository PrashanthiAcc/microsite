import { ChangeDetectorRef, Component, signal } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators, FormsModule } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';

import { forkJoin, Observable } from 'rxjs';
import { CustomDropdownComponent } from '../../../shared/components/custom-dropdown/custom-dropdown';
import { ToasterComponent } from '../../../shared/components/toaster/toaster';
import { Spinner } from '../../../shared/components/spinner/spinner';
import { UserService } from '../../../core/services/users';
import { UsecaseService } from '../../../core/services/usecase';
import { IndustryService } from '../../../core/services/industry';

interface IndustryResponse {
  industries: any[];
  subIndustries: any[];
  valueChains: any[];
}

interface UsecaseFaq {
  usecaseFaqId: number | null;
  usecaseId: number | null;
  question: string;
  answer: string;
  updatedBy: number;
  lastUpdated: string | null;
  showAnswer?: boolean;   // purely for UI toggle
  editing?: boolean;      // UI state
  updated?: boolean;      // flag for saved state
}
interface StoryDetails {
  usecaseId: number;
  industryId: number;
  subIndustryId: number;
  valueChainId: number;
  title: string;
  thumbnailImageUrl: string;
  bannerUrl: string;
  tag: string[];
  description: string;
  duration: number;
  ownerId: number;

  speakers: {
    speakerEid: string;
    speakerType: 'PRIMARY' | 'SECONDARY' | 'TERTIARY';
  }[];

  artifacts: {
    artifactType: string;
    url: string;
    artifactName: string;
  }[];

  faqs: UsecaseFaq[];

  businessProblem: string;
  solutions: string;
  valueDelivered: string;
  toolsAndTechnologies: string;
  keyResults: string;

  status: string;
  approverId: number;
  approvedDate: string | null;
  createdDate: string;
  updatedDate: string | null;
  isActive: boolean;
  creatorId: number;
  narrationGuide: string;
}
@Component({
  selector: 'app-view-requests',
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, CustomDropdownComponent, ToasterComponent, Spinner],
  templateUrl: './view-requests.html',
  styleUrl: './view-requests.scss',
})
export class ViewRequestsComponent {
  basicDetailsOpen: boolean = true;
  pocsOpen: boolean = true;
  artifactsOpen: boolean = true;
  storyForm: FormGroup;

  industries: any[] = [];
  subIndustries: any[] = [];
  valueChains: any[] = [];

  originalFaqs: any[] = [];

  thumbnailPreview: string | ArrayBuffer | null = null;
  bannerPreview: string | ArrayBuffer | null = null;

  faqBackup: any = {};

  allSubIndustries: any[] = [];
  allValueChains: any[] = [];
  alertIconPath = 'assets/icons/alert.png';
  slideIconPath = 'assets/icons/slides.png';
  documentIconPath = 'assets/icons/document.png';
  users_inside_circleIconPath = 'assets/icons/users_inside_circle.png';
  solutionIconPath = 'assets/icons/solution.png';
  line_graphIconPath = 'assets/icons/line_graph.png';
  targetIconPath = 'assets/icons/target.png';
  caliperIconPath = 'assets/icons/caliper.png';
  dropdownIconPath = 'assets/icons/ui.png';
  locationIconPath = 'assets/icons/location.png';
  chequeIconPath = 'assets/icons/cheque.png';
  downArrowIconPath = 'assets/icons/down_arrow.png';
  upArrowIconPath = 'assets/icons/arrow-up.png';
  pencilIconPath = 'assets/icons/pencil.png';
  uploadIconPath = 'assets/icons/upload.png';
  saveIconPath = 'assets/icons/save_white.png';
  closeIconPath = 'assets/icons/close.png';
  arrowLeftIcon = "assets/icons/arrow_left.png";
  leftArrowIconPath = 'assets/icons/arrow_left.png';
  isOpen: boolean = true;
  showApprovalModal: boolean = false;
  showSuperAdminApprovalModal: boolean = false;
  tags: string[] = [];
  tagInput: string = '';
  showInput: boolean = false;
  editorConfig = {
    toolbar: [
      ['bold', 'italic', 'underline'],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ align: [] }]
    ]
  };

  allowedFileTypes: { [key: string]: string[] } = {
    thumbnail: ['image/png', 'image/jpeg', 'image/jpg'],
    banner: ['image/png', 'image/jpeg', 'image/jpg'],
    // clientCredentials: ['application/vnd.openxmlformats-officedocument.presentationml.presentation'], // .pptx
    elevatorPitch: ['application/vnd.openxmlformats-officedocument.presentationml.presentation'], // .pptx
    clientStory: ['application/vnd.openxmlformats-officedocument.presentationml.presentation'], // .pptx
    demoVideos: ['video/mp4'],
    clientTestimonials: [
      'application/pdf',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation', // .pptx
      'video/mp4',
      'image/png',
      'image/jpeg',
      'image/jpg'
    ]
  };

  // Size limits (bytes)
  fileSizeLimits: { [key: string]: number } = {
    thumbnail: 5 * 1024 * 1024, // 5MB
    banner: 5 * 1024 * 1024,    // 5MB
    // clientCredentials: 100 * 1024 * 1024, // 100MB
    elevatorPitch: 200 * 1024 * 1024, // 200MB
    clientStory: 200 * 1024 * 1024, // 200MB
    demoVideos: 250 * 1024 * 1024,        // 200MB
    clientTestimonials: 200 * 1024 * 1024 // 200MB
  };
  allUsers: any[] = [];
  modalAction: 'draft' | 'approval' | null = null;
  superAdminModalAction: 'send-to-draft' | 'accept' | null = null;
  usecaseID: string | null = '';
  elevatorFileName: string = '';
  storyFileName: string = '';
  thumbnailFile: File | null = null;
  bannerFile: File | null = null;

  showToast = false;
  toastMessage = '';
  toastTitle = '';
  isDataLoading = signal(false);
  storyDetails: StoryDetails = {} as StoryDetails;
  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService,
    private route: ActivatedRoute, private location: Location, private cdr: ChangeDetectorRef) {
    this.storyForm = this.fb.group({
      // elevatorPitch: [null],
      // clientStory: [null],
      industryId: [''],
      subIndustryId: [''],
      valueChainId: [''],
      title: ['', Validators.required],
      description: [''],
      tags: this.fb.array([]),
      duration: [''],

      thumbnail: [null],
      banner: [null],

      ownerId: 102,
      primarySpeaker: [''],
      secondarySpeaker: [''],
      tertiarySpeaker: [''],
      speakers: [
        {
          speakerEid: 'E1004',
          speakerType: 'PRIMARY'
        },
        {
          speakerEid: 'E1005',
          speakerType: 'SECONDARY'
        },
        {
          speakerEid: 'E1001',
          speakerType: 'TERTIARY'
        }
      ],
      demoVideos: this.fb.array([this.createArtifact()]),
      clientStory: this.fb.array([this.fb.control(null)]),
      elevatorPitch: this.fb.array([this.fb.control(null)]),
      clientTestimonials: this.fb.array([this.createArtifact()]),

      businessProblem: [''],
      solutions: [''],
      valueDelivered: [''],
      toolsAndTechnologies: [''],
      keyResults: [''],
      narrationGuide: [''],
      approverId: 3,
      isActive: true,
      creatorId: 4,
      faqs: this.fb.array([this.fb.group({
        question: ['', Validators.required],
        answer: ['', Validators.required],
        editing: [true],
        showAnswer: [false],
        originalData: [null]
      })])
    });
  }

  // ngOnInit() {
  //   window.scrollTo({ top: 0 });
  //   this.loadIndustries();
  //   this.getAllUsers();
  //   this.usecaseID = this.route.snapshot.paramMap.get('id');
  //   this.loadUsecaseById(this.usecaseID)
  //   this.cdr.detectChanges();
  // }

  ngOnInit() {
    window.scrollTo({ top: 0 });
    this.usecaseID = this.route.snapshot.paramMap.get('id');
    forkJoin({
      industries: this.industryService.getIndustries() as Observable<IndustryResponse>,
      users: this.userService.getAllUsers() as Observable<any[]>
    }).subscribe(({ industries, users }) => {
      this.industries = industries.industries;
      this.allSubIndustries = industries.subIndustries;
      this.allValueChains = industries.valueChains;
      this.allUsers = users;
      if (this.usecaseID) {
        this.loadUsecaseById(this.usecaseID);
      }
      this.cdr.detectChanges();
    });

  }

  loadUsecaseById(id: string | null) {
    if (!id) return;
    this.isDataLoading.set(true);
    this.usecaseService.getStoryDetailsById(id).subscribe((res: any) => {
      this.storyDetails = res as StoryDetails;
      // ✅ Find matching names for industry/subIndustry/valueChain
      const industryName = this.industries.find(i => i.industryId === res.industryId)?.industryName || '';
      const subIndustryName = this.allSubIndustries.find(si => si.subIndustryId === res.subIndustryId)?.subIndustryName || '';
      const valueChainName = this.allValueChains.find(vc => vc.valueChainId === res.valueChainId)?.valueChainName || '';

      // ✅ Owner patched with userEid string
      const ownerUser = this.allUsers.find(u => u.userEid === res.ownerEId);
      const ownerEid = ownerUser ? ownerUser.userEid : '';

      // ✅ Speakers patched with userEid strings
      const primarySpeakerEid = res.speakers.find((s: any) => s.speakerType === 'PRIMARY')?.speakerEid || '';
      const secondarySpeakerEid = res.speakers.find((s: any) => s.speakerType === 'SECONDARY')?.speakerEid || '';
      const tertiarySpeakerEid = res.speakers.find((s: any) => s.speakerType === 'TERTIARY')?.speakerEid || '';

      // ✅ Thumbnail file name extraction
      let thumbnailValue: any = null;
      if (res.thumbnailImageUrl) {
        const fileName = res.thumbnailImageUrl.split('/').pop();
        thumbnailValue = { name: fileName };
      }

      let bannerValue: any = null;
      if (res.bannerUrl) {
        const fileName = res.bannerUrl.split('/').pop();
        bannerValue = { name: fileName };
      }

      this.thumbnailPreview = res.thumbnailImageUrl || null;
      this.bannerPreview = res.bannerUrl || null;

      // ✅ Patch form values
      this.storyForm.patchValue({
        industryId: industryName,
        subIndustryId: subIndustryName,
        valueChainId: valueChainName,
        title: res.title,
        description: res.description,
        duration: res.duration,
        ownerId: ownerEid, // ✅ userEid string
        primarySpeaker: primarySpeakerEid,
        secondarySpeaker: secondarySpeakerEid,
        tertiarySpeaker: tertiarySpeakerEid,
        businessProblem: res.businessProblem,
        solutions: res.solutions,
        valueDelivered: res.valueDelivered,
        toolsAndTechnologies: res.toolsAndTechnologies,
        keyResults: res.keyResults,
        narrationGuide: res.narrationGuide,
        approverId: res.approverId,
        creatorId: res.creatorId,
        thumbnail: res.thumbnailImageUrl
          ? {
            file: null,
            name: res.thumbnailImageUrl.split('/').pop(),
            url: res.thumbnailImageUrl,
            isDeleted: false
          }
          : null,

        banner: res.bannerUrl
          ? {
            file: null,
            name: res.bannerUrl.split('/').pop(),
            url: res.bannerUrl,
            isDeleted: false
          }
          : null
      });

      // ✅ Tags
      this.tagsArray.clear();
      (res.tag || []).forEach((t: string) => {
        this.tagsArray.push(this.fb.control(t));
      });

      // ✅ Artifacts
      // this.clientCredentials.clear();
      this.elevatorPitch.clear();
      this.clientStory.clear();
      this.demoVideos.clear();
      this.clientTestimonials.clear();
      (res.artifacts || []).forEach((a: any) => {
        if (a.artifactType === 'ELEVATOR_PITCH') {
          this.elevatorPitch.push(this.fb.control({ name: a.artifactName, url: a.url }));
        } else if (a.artifactType === 'USER_STORY') {
          this.clientStory.push(this.fb.control({ name: a.artifactName, url: a.url }));
        } else if (a.artifactType === 'DEMO_VIDEO') {
          this.demoVideos.push(this.fb.control({ name: a.artifactName, url: a.url }));
        } else if (a.artifactType === 'CLIENT_TESTIMONIAL') {
          this.clientTestimonials.push(this.fb.control({ name: a.artifactName, url: a.url }));
        }
      });

      if (this.elevatorPitch.length === 0) {
        this.elevatorPitch.push(this.fb.control(null));
      }

      if (this.clientStory.length === 0) {
        this.clientStory.push(this.fb.control(null));
      }

      // ✅ FAQs
      this.faqs.clear();
      (res.faqs || []).forEach((f: any) => {
        this.faqs.push(this.fb.group({
          question: [f.question, Validators.required],
          answer: [f.answer, Validators.required],
          editing: [false],
          showAnswer: [false]
        }));
      });

      this.originalFaqs = this.faqs.value.map(faq => ({ ...faq }));
      this.isDataLoading.set(false);
    });
  }


  getAllUsers() {
    this.userService.getAllUsers().subscribe((res: any) => {
      this.allUsers = res;
    });
  }

  loadIndustries() {

    this.industryService.getIndustries().subscribe((res: any) => {

      this.industries = res.industries;

      this.allSubIndustries = res.subIndustries;
      this.allValueChains = res.valueChains;

    });
  }


  onIndustrySelected(industry: any) {
    this.storyForm.patchValue({ industryId: industry?.industryId || '' });
    this.subIndustries = industry
      ? this.allSubIndustries.filter(sub => sub.industryId === industry.industryId)
      : [];
    this.valueChains = [];
  }

  onSubIndustrySelected(sub: any) {
    this.storyForm.patchValue({ subIndustryId: sub?.subIndustryId || '' });
    this.valueChains = sub
      ? this.allValueChains.filter(vc => vc.subIndustryId === sub.subIndustryId)
      : [];
  }

  onValueChainSelected(vc: any) {
    this.storyForm.patchValue({ valueChainId: vc?.valueChainId || '' });
  }

  // Create artifact control (file or null)
  createArtifact() {
    return this.fb.control({
      file: null,
      name: null,
      url: null,
      isDeleted: false
    });
  }

  // GETTERS
  get tagsArray(): FormArray {
    return this.storyForm.get('tags') as FormArray;
  }
  // get clientCredentials(): FormArray {
  //   return this.storyForm.get('clientCredentials') as FormArray;
  // }
  get elevatorPitch(): FormArray {
    return this.storyForm.get('elevatorPitch') as FormArray;
  }
  get clientStory(): FormArray {
    return this.storyForm.get('clientStory') as FormArray;
  }
  get demoVideos(): FormArray {
    return this.storyForm.get('demoVideos') as FormArray;
  }
  get clientTestimonials(): FormArray {
    return this.storyForm.get('clientTestimonials') as FormArray;
  }
  get faqs(): FormArray<FormGroup> {
    return this.storyForm.get('faqs') as FormArray<FormGroup>;
  }


  // ADD FIELD
  addField(array: FormArray) {
    array.push(this.createArtifact());
  }

  // REMOVE FIELD
  removeField(array: FormArray, index: number) {
    const control = array.at(index);

    if (control.value?.url) {
      control.patchValue({ isDeleted: true });
    } else {
      array.removeAt(index);
    }
  }


  handleFileUpload(event: Event, field: string, index?: number) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      // ✅ size check
      if (file.size > this.fileSizeLimits[field]) {
        alert(`File size exceeds ${this.fileSizeLimits[field] / (1024 * 1024)}MB. Please upload a smaller file.`);
        input.value = '';
        return;
      }

      // ✅ type check
      if (!this.allowedFileTypes[field].includes(file.type)) {
        alert(`Invalid file type. Allowed types: ${this.allowedFileTypes[field].join(', ')}`);
        input.value = '';
        return;
      }

      // ✅ save file into correct form control
      if (field === 'thumbnail') {
        this.storyForm.patchValue({ thumbnail: file });
        this.thumbnailFile = file;
      } else if (field === 'banner') {
        this.storyForm.patchValue({ banner: file });
        this.bannerFile = file;
      } else if (index !== undefined) {
        // (this.storyForm.get(field) as FormArray).at(index).setValue(file);

        const control = (this.storyForm.get(field) as FormArray).at(index);

        const existingValue = control.value;

        control.setValue({
          file: file,
          name: file.name,
          url: existingValue?.url || null,
          isDeleted: false
        });
      } else {
        this.storyForm.patchValue({ [field]: file });
      }

      const reader = new FileReader();
      reader.onload = (e: any) => {
        if (field === 'thumbnail') {
          this.thumbnailPreview = e.target.result;
        } else if (field === 'banner') {
          this.bannerPreview = e.target.result;
        }
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    }
  }

  // --- Existing methods kept intact ---
  toggleInput(): void {
    this.showInput = !this.showInput;
  }

  addTag(input: HTMLInputElement): void {
    const value = input.value.trim();
    if (value && !this.tagsArray.value.includes(value)) {
      this.tagsArray.push(this.fb.control(value));
      input.value = '';
      this.showInput = false;
    }
  }

  removeTag(index: number): void {
    this.tagsArray.removeAt(index);
  }

  onFileUpload(event: any, type: string) {
    const file = event.target.files[0];
    console.log(type, file);
  }

  toggleBasicDetailsAccordion() {
    this.basicDetailsOpen = !this.basicDetailsOpen;
  }

  togglePOCsAccordion() {
    this.pocsOpen = !this.pocsOpen;
  }

  toggleArtifactsAccordion() {
    this.artifactsOpen = !this.artifactsOpen;
  }


  addFAQ() {
    const faqGroup = this.fb.group({
      question: ['', Validators.required],
      answer: ['', Validators.required],
      editing: [true],
      showAnswer: [false]
    });
    //this.faqs.push(faqGroup);
    this.faqs.insert(0, faqGroup);
  }

  saveFAQ(index: number) {
    const faqGroup = this.faqs.at(index);
    if (faqGroup.valid) {
      faqGroup.patchValue({ editing: false }); // collapse view
    } else {
      alert('Please fill both Question and Answer before saving.');
    }
  }

  cancelFAQ(i: number) {
    const faqGroup = this.faqs.at(i);
    const original = this.faqBackup[i];

    if (original) {
      faqGroup.setValue({
        question: original.question,
        answer: original.answer,
        editing: false,
        showAnswer: false
      });
    } else {
      faqGroup.get('editing')?.setValue(false);
      faqGroup.get('showAnswer')?.setValue(false);
    }
  }

  editFAQ(i: number) {
    const faqGroup = this.faqs.at(i);
    this.faqBackup[i] = JSON.parse(JSON.stringify(faqGroup.value));

    faqGroup.get('editing')?.setValue(true);
    faqGroup.get('showAnswer')?.setValue(true);
  }

  openModal(action: 'draft' | 'approval') {
    this.modalAction = action;
    this.showApprovalModal = true;
  }

  closeApprovalModal() {
    this.showApprovalModal = false;
  }
  get approverName(): string {
    const ownerId = this.storyForm.get('ownerId')?.value;
    if (!ownerId) return 'Select Owner';
    const user = this.allUsers.find(u => u.userId === +ownerId); // cast string → number
    return user ? user.userEid : '';
  }


  confirmModalAction() {
    this.showApprovalModal = false;
    if (this.modalAction === 'approval') {
      this.updateStoryForApproval(); // existing approval payload
    } else if (this.modalAction === 'draft') {
      this.updateStorySaveDraft();   // new draft handler
    }
  }

  // async urlToFile(url: string, fileName: string): Promise<File> {
  //   const response = await fetch(url);
  //   const blob = await response.blob();
  //   return new File([blob], fileName, { type: blob.type });
  // }

  // ✅ Update existing story

  async updateStoryForApproval() {
    if (!this.usecaseID) {
      console.error('No usecaseID found, cannot update story.');
      return;
    }

    const formValue = this.storyForm.value;

    // ✅ Map industry/subIndustry/valueChain names back to IDs
    const industryObj = this.industries.find(i => i.industryName === formValue.industryId);
    const subIndustryObj = this.allSubIndustries.find(si => si.subIndustryName === formValue.subIndustryId);
    const valueChainObj = this.allValueChains.find(vc => vc.valueChainName === formValue.valueChainId);

    // Owner and speakers already working fine (userEid strings)
    const ownerUser = this.allUsers.find(u => u.userEid === formValue.ownerId);

    const payload = {
      industryId: industryObj ? industryObj.industryId : null,
      subIndustryId: subIndustryObj ? subIndustryObj.subIndustryId : null,
      valueChainId: valueChainObj ? valueChainObj.valueChainId : null,
      title: formValue.title,
      thumbnailImageUrl: this.extractValue(this.storyForm.get('thumbnailUrl')?.value),
      bannerUrl: this.extractValue(this.storyForm.get('bannerUrl')?.value),
      description: formValue.description,
      duration: formValue.duration ? Number(formValue.duration) : null,

      ownerEId: formValue.ownerId, // already userEid string
      businessProblem: formValue.businessProblem,
      solutions: formValue.solutions,
      valueDelivered: formValue.valueDelivered,
      toolsAndTechnologies: formValue.toolsAndTechnologies,
      keyResults: formValue.keyResults,
      narrationGuide: formValue.narrationGuide,

      approverId: ownerUser ? ownerUser.userId : null,
      creatorId: ownerUser ? ownerUser.userId : null,
      isActive: true,

      speakers: [
        { speakerEid: formValue.primarySpeaker, speakerType: 'PRIMARY' },
        { speakerEid: formValue.secondarySpeaker, speakerType: 'SECONDARY' },
        { speakerEid: formValue.tertiarySpeaker, speakerType: 'TERTIARY' }
      ].filter(s => s.speakerEid),

      tags: (formValue.tags || []).map((t: any) => t),
      faq: (formValue.faqs || []).map((f: any) => ({
        question: f.question,
        answer: f.answer
      })),

      artifacts: []
    };

    const formData = new FormData();
    formData.append('useCaseRequest', JSON.stringify(payload));

    // Thumbnail section for Payload
    const thumbnail = this.storyForm.get('thumbnail')?.value;

    if (thumbnail instanceof File) {
      formData.append('thumbnailUrl', thumbnail);
    }
    else if (thumbnail?.file instanceof File) {
      formData.append('thumbnailUrls', thumbnail.file);
    }
    else if (typeof thumbnail === 'string') {
      formData.append('thumbnailUrl', thumbnail);
    }
    else if (thumbnail?.url) {
      formData.append('thumbnailUrls', thumbnail.url);
    }

    // Banner section for Payload
    const banner = this.storyForm.get('banner')?.value;

    if (banner instanceof File) {
      formData.append('bannerUrl', banner);
    }
    else if (banner?.file instanceof File) {
      formData.append('bannerUrls', banner.file);
    }
    else if (typeof banner === 'string') {
      formData.append('bannerUrl', banner);
    }
    else if (banner?.url) {
      formData.append('bannerUrls', banner.url);
    }

    // Elevator Pitch section for Payload
    this.elevatorPitch.controls.forEach((control: any) => {
      const value = control.value;

      if (value?.file instanceof File) {
        formData.append('elevatorPitch', value.file);
      } else if (value?.url) {
        formData.append('elevatorPitchUrls', value.url);
      }
    });

    // Detailed client story section for Payload
    this.clientStory.controls.forEach((control: any) => {
      const value = control.value;

      if (value?.file instanceof File) {
        formData.append('userStory', value.file);
      } else if (value?.url) {
        formData.append('userStoryUrls', value.url);
      }
    });

    // Demo Video section for Payload
    let hasDemoVideos = false;
    let hasDemoVideoControl = this.demoVideos.controls.length > 0;

    this.demoVideos.controls.forEach((control: any) => {
      const value = control.value;

      if (!value || (!value.file && !value.url)) return;

      if (value.isDeleted) return;

      hasDemoVideos = true;

      if (value.file instanceof File) {
        formData.append('demoVideos', value.file);
      } else if (value.url) {
        formData.append('demoVideosUrls', value.url);
      }
    });

    if (hasDemoVideoControl && !hasDemoVideos) {
      formData.append('demoVideosUrls', '');
    }

    // Client Testimonial section for Payload
    let hasTestimonials = false;
    let hasClientTestimonialControl = this.clientTestimonials.controls.length > 0;

    this.clientTestimonials.controls.forEach((control: any) => {
      const value = control.value;

      if (!value || (!value.file && !value.url)) return;

      if (value.isDeleted) return;

      hasTestimonials = true;

      if (value.file instanceof File) {
        formData.append('clientTestimonials', value.file);
      } else if (value.url) {
        formData.append('clientTestimonialsUrls', value.url);
      }
    });

    if (hasClientTestimonialControl && !hasTestimonials) {
      formData.append('clientTestimonialsUrls', '');
    }

    console.log('Payload for Update:', payload);
    this.isDataLoading.set(true);
    this.usecaseService.updateStorySendForApproval(this.usecaseID, formData).subscribe({
      next: (res: any) => {
        this.isDataLoading.set(false);
        this.showToast = false;
        console.log('Response from API:', res);
        this.toastTitle = 'Story updated successfully';
        // this.toastMessage = 'Your story has been submitted'; // ✅ add message
        this.showToast = true;
        this.cdr.detectChanges();
        this.location.back();
      },
      error: (err: any) => {
        this.isDataLoading.set(false);
        this.showToast = false;
        console.error('Error from API:', err);
        //console.log('Error payload:', err.error);
        let errorObj: any = {};
        try {
          errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
        } catch {
          errorObj = { errorDescription: err.message };
        }
        if (err.status === 400) {
          this.toastTitle = errorObj.errorDescription || errorObj.message || 'Bad Request';
          //this.toastMessage = err.error?.errorDescription;
        } else if (err.status === 500) {
          this.toastTitle = errorObj.errorDescription || errorObj.message || 'Bad Request';
        } else {
          this.toastTitle = 'An unexpected error occurred';
          //this.toastMessage = err.error?.errorDescription || err.message;
        }
        this.showToast = true;
        this.cdr.detectChanges();

      }

    });
  }

  updateStorySaveDraft() {
    if (!this.usecaseID) {
      console.error('No usecaseID found, cannot update story.');
      return;
    }

    const formValue = this.storyForm.value;

    // ✅ Map industry/subIndustry/valueChain names back to IDs
    const industryObj = this.industries.find(i => i.industryName === formValue.industryId);
    const subIndustryObj = this.allSubIndustries.find(si => si.subIndustryName === formValue.subIndustryId);
    const valueChainObj = this.allValueChains.find(vc => vc.valueChainName === formValue.valueChainId);

    // Owner and speakers already working fine (userEid strings)
    const ownerUser = this.allUsers.find(u => u.userEid === formValue.ownerId);

    const payload = {
      industryId: industryObj ? industryObj.industryId : null,
      subIndustryId: subIndustryObj ? subIndustryObj.subIndustryId : null,
      valueChainId: valueChainObj ? valueChainObj.valueChainId : null,
      title: formValue.title,
      thumbnailImageUrl: this.extractValue(this.storyForm.get('thumbnailUrl')?.value),
      bannerUrl: this.extractValue(this.storyForm.get('bannerUrl')?.value),
      description: formValue.description,
      duration: formValue.duration ? Number(formValue.duration) : null,

      ownerEId: formValue.ownerId, // already userEid string
      businessProblem: formValue.businessProblem,
      solutions: formValue.solutions,
      valueDelivered: formValue.valueDelivered,
      toolsAndTechnologies: formValue.toolsAndTechnologies,
      keyResults: formValue.keyResults,
      narrationGuide: formValue.narrationGuide,

      approverId: ownerUser ? ownerUser.userId : null,
      creatorId: ownerUser ? ownerUser.userId : null,
      isActive: true,

      speakers: [
        { speakerEid: formValue.primarySpeaker, speakerType: 'PRIMARY' },
        { speakerEid: formValue.secondarySpeaker, speakerType: 'SECONDARY' },
        { speakerEid: formValue.tertiarySpeaker, speakerType: 'TERTIARY' }
      ].filter(s => s.speakerEid),

      tags: (formValue.tags || []).map((t: any) => t),
      faq: (formValue.faqs || []).map((f: any) => ({
        question: f.question,
        answer: f.answer
      })),

      artifacts: []
    };

    const formData = new FormData();
    formData.append('useCaseRequest', JSON.stringify(payload));

    // Thumbnail section for Payload
    const thumbnail = this.storyForm.get('thumbnail')?.value;

    if (thumbnail instanceof File) {
      formData.append('thumbnailUrl', thumbnail);
    }
    else if (thumbnail?.file instanceof File) {
      formData.append('thumbnailUrls', thumbnail.file);
    }
    else if (typeof thumbnail === 'string') {
      formData.append('thumbnailUrl', thumbnail);
    }
    else if (thumbnail?.url) {
      formData.append('thumbnailUrls', thumbnail.url);
    }

    // Banner section for Payload
    const banner = this.storyForm.get('banner')?.value;

    if (banner instanceof File) {
      formData.append('bannerUrl', banner);
    }
    else if (banner?.file instanceof File) {
      formData.append('bannerUrls', banner.file);
    }
    else if (typeof banner === 'string') {
      formData.append('bannerUrl', banner);
    }
    else if (banner?.url) {
      formData.append('bannerUrls', banner.url);
    }

    // Elevator Pitch section for Payload
    this.elevatorPitch.controls.forEach((control: any) => {
      const value = control.value;

      if (value?.file instanceof File) {
        formData.append('elevatorPitch', value.file);
      } else if (value?.url) {
        formData.append('elevatorPitchUrls', value.url);
      }
    });

    // Detailed client story section for Payload
    this.clientStory.controls.forEach((control: any) => {
      const value = control.value;

      if (value?.file instanceof File) {
        formData.append('userStory', value.file);
      } else if (value?.url) {
        formData.append('userStoryUrls', value.url);
      }
    });

    // Demo Video section for Payload
    let hasDemoVideos = false;
    let hasDemoVideoControl = this.demoVideos.controls.length > 0;

    this.demoVideos.controls.forEach((control: any) => {
      const value = control.value;
      if (!value || (!value.file && !value.url)) return;

      if (value.isDeleted) return;

      hasDemoVideos = true;

      if (value.file instanceof File) {
        formData.append('demoVideos', value.file);
      } else if (value.url) {
        formData.append('demoVideosUrls', value.url);
      }
    });

    if (hasDemoVideoControl && !hasDemoVideos) {
      formData.append('demoVideosUrls', '');
    }

    // Client Testimonial section for Payload
    let hasTestimonials = false;
    let hasClientTestimonialControl = this.clientTestimonials.controls.length > 0;

    this.clientTestimonials.controls.forEach((control: any) => {
      const value = control.value;

      if (!value || (!value.file && !value.url)) return;

      if (value.isDeleted) return;

      hasTestimonials = true;

      if (value.file instanceof File) {
        formData.append('clientTestimonials', value.file);
      } else if (value.url) {
        formData.append('clientTestimonialsUrls', value.url);
      }
    });

    if (hasClientTestimonialControl && !hasTestimonials) {
      formData.append('clientTestimonialsUrls', '');
    }

    console.log('Payload for Update:', payload);
    this.isDataLoading.set(true);
    this.usecaseService.updateStorySaveDraft(this.usecaseID, formData).subscribe({
      next: (res: any) => {
        this.isDataLoading.set(false);
        this.showToast = false;
        console.log('Response from API:', res);
        this.toastTitle = 'Story updated for draft';
        // this.toastMessage = 'Your story has been submitted'; // ✅ add message
        this.showToast = true;
        this.cdr.detectChanges();
        this.location.back();
      },
      error: (err: any) => {
        this.isDataLoading.set(false);
        this.showToast = false;
        console.error('Error from API:', err);
        console.log('Error payload:', err.error);
        let errorObj: any = {};
        try {
          errorObj = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
        } catch {
          errorObj = { errorDescription: err.message };
        }
        if (err.status === 400) {
          this.toastTitle = errorObj.errorDescription || errorObj.message || 'Bad Request';
          //this.toastMessage = err.error?.errorDescription;
        } else if (err.status === 500) {
          this.toastTitle = errorObj.errorDescription || errorObj.message || 'Bad Request';
        } else {
          this.toastTitle = 'An unexpected error occurred';
          //this.toastMessage = err.error?.errorDescription || err.message;
        }
        this.showToast = true;
        this.cdr.detectChanges();

      }

    });
  }

  extractValue(value: any) {
    // Case 1: File (new upload)
    if (value instanceof File) {
      return value;
    }

    // Case 2: Object with name (your current issue)
    if (value && typeof value === 'object' && value.name) {
      return value.name; // ✅ FIX
    }

    // Case 3: Already string
    return value;
  }

  getCleanFileName(value: any, type: 'thumbnail' | 'banner'): string {
    if (!value) return '';

    if (value instanceof File) {
      return value.name;
    }

    try {
      let decoded = decodeURIComponent(value);
      decoded = decoded.split('?')[0];

      const parts = decoded.split('/');
      const fileName = parts[parts.length - 1];
      const folderName = parts[parts.length - 3];

      if (!fileName) return '';

      const cleanFolder = folderName.replace(/\s+/g, '');

      return `${cleanFolder}_${fileName}`;
    } catch {
      return type === 'thumbnail'
        ? 'ThumbnailURL_image.jpg'
        : 'BannerURL_image.jpg';
    }
  }

  deleteFAQ(index: number) {
    if (this.faqs.length > 1) {
      this.faqs.removeAt(index);
    } else {
      // If you want at least one FAQ always present, reset instead of removing
      this.faqs.at(0).patchValue({
        question: '',
        answer: '',
        editing: true,
        showAnswer: false
      });
    }

    this.originalFaqs = this.faqs.value.map(faq => ({ ...faq }));
  }

  onCancel() {
    this.location.back();
  }

  getApproverEid(storyDetails: any): string {
    if (!storyDetails?.approverId || !this.allUsers) return '';
    const approver = this.allUsers.find((u: any) => u.userId === storyDetails.approverId);
    return approver ? approver.userEid : storyDetails.approverId; // fallback to ID if not found
  }

  openSuperAdminModal(action: 'send-to-draft' | 'accept') {
    this.superAdminModalAction = action;
    console.log(this.superAdminModalAction)
    this.showSuperAdminApprovalModal = true;
  }

  closeSuperAdminApprovalModal() {
    this.showSuperAdminApprovalModal = false;
  }

  confirmSuperAdminModalAction() {
    this.showApprovalModal = false;
    if (this.superAdminModalAction === 'send-to-draft') {
      this.sendToDraftStory();
    } else if (this.superAdminModalAction === 'accept') {
      this.approveStory();
    }
  }

  sendToDraftStory() {
    this.usecaseService.sendToDraftUsecase(this.storyDetails.usecaseId).subscribe({
      next: (res: string) => {
        this.showToast = false;
        console.log('Story send to draft successfully:', res);
        this.toastTitle = 'Story updated for draft';
        this.showToast = true;
        this.router.navigate(['/request']);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Sending to draft failed:', err);
      }
    });
  }

  approveStory() {
    this.usecaseService.approveUsecase(this.storyDetails.usecaseId, this.storyDetails.approverId).subscribe({
      next: (res: string) => {
        this.showToast = false;
        console.log('Story approved successfully:', res);
        this.toastTitle = 'Story approved successfully';
        this.showToast = true;
        this.location.back();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Approval failed:', err);
      }
    });
  }

}
