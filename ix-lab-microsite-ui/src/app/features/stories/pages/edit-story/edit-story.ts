import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators, FormsModule } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';
@Component({
  selector: 'app-edit-story',
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, CustomDropdownComponent],
  templateUrl: './edit-story.html',
  styleUrls: ['./edit-story.scss'],
})
export class EditStoryComponent {
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
  isOpen: boolean = true;
  showApprovalModal: boolean = false;
  tags: string[] = [];
  tagInput: string = '';
  showInput: boolean = false;
  editorConfig = {
    toolbar: [
      ['bold', 'italic', 'underline'],
      [{ align: [] }]
    ]
  };

  allowedFileTypes: { [key: string]: string[] } = {
    thumbnail: ['image/png', 'image/jpeg', 'image/jpg'],
    banner: ['image/png', 'image/jpeg', 'image/jpg'],
    clientCredentials: ['application/vnd.openxmlformats-officedocument.presentationml.presentation'], // .pptx
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
    clientCredentials: 100 * 1024 * 1024, // 100MB
    elevatorPitch: 100 * 1024 * 1024, // 100MB
    clientStory: 100 * 1024 * 1024, // 100MB
    demoVideos: 100 * 1024 * 1024,        // 100MB
    clientTestimonials: 100 * 1024 * 1024 // 100MB
  };
  allUsers: any[] = [];
  modalAction: 'draft' | 'approval' | null = null;
  usecaseID: string | null = '';
  elevatorFileName: string = '';
  storyFileName: string = '';
  thumbnailFile : File | null = null;
  bannerFile: File | null = null;

  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService,
    private route: ActivatedRoute, private location: Location, private cdr: ChangeDetectorRef) {
    this.storyForm = this.fb.group({
      elevatorPitch: [null],
      clientStory: [null],
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
      clientCredentials: this.fb.array([this.createArtifact()]),
      demoVideos: this.fb.array([this.createArtifact()]),
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

  ngOnInit() {
    window.scrollTo({ top: 0 });
    this.loadIndustries();
    this.getAllUsers();
    this.usecaseID = this.route.snapshot.paramMap.get('id');
    this.loadUsecaseById(this.usecaseID)
    this.cdr.detectChanges();
  }

  loadUsecaseById(id: string | null) {
    if (!id) return;

    this.usecaseService.getStoryDetailsById(id).subscribe((res: any) => {
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
        thumbnailImageUrl: thumbnailValue,
        bannerUrl: bannerValue
      });

      // ✅ Tags
      this.tagsArray.clear();
      (res.tag || []).forEach((t: string) => {
        this.tagsArray.push(this.fb.control(t));
      });

      // ✅ Artifacts
      this.clientCredentials.clear();
      this.demoVideos.clear();
      this.clientTestimonials.clear();
      (res.artifacts || []).forEach((a: any) => {
        if (a.artifactType === 'ELEVATOR_PITCH') {
          this.clientCredentials.push(this.fb.control({ name: a.artifactName, url: a.url }));
        } else if (a.artifactType === 'DEMO_VIDEO') {
          this.demoVideos.push(this.fb.control({ name: a.artifactName, url: a.url }));
        } else if (a.artifactType === 'CLIENT_TESTIMONIAL') {
          this.clientTestimonials.push(this.fb.control({ name: a.artifactName, url: a.url }));
        }
      });

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
    return this.fb.control(null);
  }

  // GETTERS
  get tagsArray(): FormArray {
    return this.storyForm.get('tags') as FormArray;
  }
  get clientCredentials(): FormArray {
    return this.storyForm.get('clientCredentials') as FormArray;
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
    if (array.length > 1) {
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
        (this.storyForm.get(field) as FormArray).at(index).setValue(file);
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

  // ✅ Update existing story

  updateStoryForApproval() {
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
      thumbnailImageUrl: this.extractValue(this.storyForm.get('thumbnailImageUrl')?.value),
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

    if(this.thumbnailFile) {
      formData.append('thumbnailUrl', this.thumbnailFile);
    }

    if(this.bannerFile) {
      formData.append('bannerUrl', this.bannerFile);
    }

    this.demoVideos.controls.forEach((control: any) => {
      if (control.value instanceof File) {
        formData.append('demoVideos', control.value);
      }
    });

    this.clientTestimonials.controls.forEach((control: any) => {
      if (control.value instanceof File) {
        formData.append('clientTestimonials', control.value);
      }
    });

    const elevatorPitch = this.storyForm.get('elevatorPitch')?.value;
    if (elevatorPitch instanceof File) {
      formData.append('elevatorPitch', elevatorPitch);
    }

    const clientStory = this.storyForm.get('clientStory')?.value;
    if (clientStory instanceof File) {
      formData.append('clientStory', clientStory);
    }

    console.log('Payload for Update:', payload);

    this.usecaseService.updateStorySendForApproval(this.usecaseID, formData).subscribe((res: any) => {
      console.log('Story updated successfully', res);
      this.location.back();
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
      thumbnailImageUrl: this.extractValue(this.storyForm.get('thumbnailImageUrl')?.value),
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

    if(this.thumbnailFile) {
      formData.append('thumbnailUrl', this.thumbnailFile);
    }

    if(this.bannerFile) {
      formData.append('bannerUrl', this.bannerFile);
    }

    this.demoVideos.controls.forEach((control: any) => {
      if (control.value instanceof File) {
        formData.append('demoVideos', control.value);
      }
    });

    this.clientTestimonials.controls.forEach((control: any) => {
      if (control.value instanceof File) {
        formData.append('clientTestimonials', control.value);
      }
    });

    const elevatorPitch = this.storyForm.get('elevatorPitch')?.value;
    if (elevatorPitch instanceof File) {
      formData.append('elevatorPitch', elevatorPitch);
    }

    const clientStory = this.storyForm.get('clientStory')?.value;
    if (clientStory instanceof File) {
      formData.append('clientStory', clientStory);
    }

    console.log('Payload for Update:', payload);

    this.usecaseService.updateStorySaveDraft(this.usecaseID, formData).subscribe((res: any) => {
      console.log('Story updated successfully', res);
      this.location.back();
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
}
