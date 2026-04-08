import { Component } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators, FormsModule } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';

@Component({
  selector: 'app-add-story',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, CustomDropdownComponent],
  templateUrl: './add-story.html',
  styleUrls: ['./add-story.scss']
})
export class AddStoryComponent {
  basicDetailsOpen: boolean = true;
  pocsOpen: boolean = true;
  artifactsOpen: boolean = true;
  storyForm: FormGroup;

  industries: any[] = [];
  subIndustries: any[] = [];
  valueChains: any[] = [];

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
  showToast = false;
  toastMessage = '';
  toastTitle = '';
  elevatorFileName: string = '';
  storyFileName: string = '';
  thumbnailFile : File | null = null;
  bannerFile: File | null = null;

  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient, private location: Location,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService) {
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

      thumbnailImageUrl: [null],
      banner: [null],

      ownerId: [''],
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
        showAnswer: [false]
      })])
    });
  }

  ngOnInit() {
    window.scrollTo({ top: 0 });
    this.loadIndustries();
    this.getAllUsers();

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
  // removeField(array: FormArray, index: number) {
  //     array.removeAt(index);
  // }

  // removeField(array: FormArray, index: number) {
  //   array.at(index).setValue(null);
  // }

  removeField(array: FormArray, index: number) {
    if (array.length > 1) {
      array.removeAt(index);
    }
  }

  // FILE UPLOAD (renamed to avoid duplicate)
  // Allowed file types for each section

  // For artifacts (Client Credential, Demo Videos, Client Testimonial)
  // handleFileUpload(event: Event, field: string, index?: number) {
  //   const input = event.target as HTMLInputElement;
  //   const file = input.files?.[0];
  //   if (file) {
  //     // Check size
  //     if (file.size > this.fileSizeLimits[field]) {
  //       alert(`File size exceeds ${this.fileSizeLimits[field] / (1024 * 1024)}MB. Please upload a smaller file.`);
  //       input.value = '';
  //       return;
  //     }
  //     // Check type
  //     if (!this.allowedFileTypes[field].includes(file.type)) {
  //       alert(`Invalid file type. Allowed types: ${this.allowedFileTypes[field].join(', ')}`);
  //       input.value = '';
  //       return;
  //     }
  //     // Save file
  //     if (index !== undefined) {
  //       (this.storyForm.get(field) as FormArray).at(index).setValue(file);
  //     } else {
  //       this.storyForm.patchValue({ [field]: file });
  //     }
  //   }
  // }

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
        this.storyForm.patchValue({ thumbnailImageUrl: file });
        this.thumbnailFile = file;
      } else if (field === 'banner') {
        this.storyForm.patchValue({ banner: file });
        this.bannerFile = file;
      } else if (index !== undefined) {
        (this.storyForm.get(field) as FormArray).at(index).setValue(file);
      } else {
        this.storyForm.patchValue({ [field]: file });
      }
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

  cancelFAQ(index: number) {
    if (index === 0) {
      this.faqs.at(index).reset({ question: '', answer: '', editing: true });
    } else {
      this.faqs.removeAt(index);
    }
  }

  editFAQ(index: number) {
    this.faqs.at(index).patchValue({ editing: true });
  }

  // toggleAccordion() {
  //   this.isOpen = !this.isOpen;
  // }

  // get approverName(): string {
  //   const ownerId = this.storyForm.get('ownerId')?.value;
  //   if (!ownerId) return 'Select Owner';
  //   const user = this.allUsers.find(u => u.userId === +ownerId); // cast string → number
  //   return user ? user.userEid : '';
  // }

  openModal(action: 'draft' | 'approval') {
    this.modalAction = action;
    this.showApprovalModal = true;
  }
  closeApprovalModal() {
    this.showApprovalModal = false;
  }
  confirmModalAction() {
    this.showApprovalModal = false;
    if (this.modalAction === 'approval') {
      this.submitStory(); // existing approval payload
    } else if (this.modalAction === 'draft') {
      this.saveDraft();   // new draft handler
    }
  }
  submitStory() {
    console.log('Form Value:', this.storyForm.value); // log entire form value for debugging
    const formValue = this.storyForm.value;
    const selectedUser = this.allUsers.find(u => u.userEid === formValue.ownerId);
    const numericId = selectedUser ? selectedUser.userId : null;
    const payload = {
      industryId: formValue.industryId,
      subIndustryId: formValue.subIndustryId,
      valueChainId: formValue.valueChainId,
      title: formValue.title,
      thumbnailImageUrl: this.extractValue(this.storyForm.get('thumbnailImageUrl')?.value),
      bannerUrl: this.extractValue(this.storyForm.get('bannerUrl')?.value),
      description: formValue.description,
      duration: formValue.duration ? Number(formValue.duration) : null,
      ownerEId: formValue.ownerId,
      businessProblem: formValue.businessProblem,
      solutions: formValue.solutions,
      valueDelivered: formValue.valueDelivered,
      toolsAndTechnologies: formValue.toolsAndTechnologies,
      keyResults: formValue.keyResults,
      narrationGuide: formValue.narrationGuide,

      // static values
      approverId: numericId,
      isActive: true,
      creatorId: numericId,

      // ✅ speakers mapped into API format
      speakers: [
        { speakerEid: formValue.primarySpeaker, speakerType: 'PRIMARY' },
        { speakerEid: formValue.secondarySpeaker, speakerType: 'SECONDARY' },
        { speakerEid: formValue.tertiarySpeaker, speakerType: 'TERTIARY' }
      ].filter(s => s.speakerEid),

      // ✅ tags as plain array of strings
      tags: (formValue.tags || []).map((t: any) => t),
      // tags: [
      //   "Digital Twin",
      //   "Real Time Monitoring",
      //   "Pharma Innovation",
      //   "Plant Simulation"
      // ],
      faq: (formValue.faqs || []).map((f: any) => ({
        question: f.question,
        answer: f.answer
      })),
      // artifacts unchanged
      artifacts: [],
    };

    const formData = new FormData();
    formData.append('data', JSON.stringify(payload));

    if(this.thumbnailFile) {
      formData.append('thumbnailImage', this.thumbnailFile);
    }

    if(this.bannerFile) {
      formData.append('bannerImage', this.bannerFile);
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

    this.usecaseService.submitForApproval(formData).subscribe((res: any) => {

      console.log('Response from API:', res);
      this.toastTitle = 'Story created and submitted for approval successfully';
      this.showToast = true;
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

  saveDraft() {
    console.log('Save Draft Form Value:', this.storyForm.value); // log entire form value for debugging
    const formValue = this.storyForm.value;

    const selectedUser = this.allUsers.find(u => u.userEid === formValue.ownerId);
    const numericId = selectedUser ? selectedUser.userId : null;
    const payload = {
      industryId: formValue.industryId,
      subIndustryId: formValue.subIndustryId,
      valueChainId: formValue.valueChainId,
      title: formValue.title,
      thumbnailImageUrl: this.extractValue(this.storyForm.get('thumbnailImageUrl')?.value),
      bannerUrl: this.extractValue(this.storyForm.get('bannerUrl')?.value),
      description: formValue.description,
      duration: formValue.duration ? Number(formValue.duration) : null,
      ownerEId: formValue.ownerId,
      businessProblem: formValue.businessProblem,
      solutions: formValue.solutions,
      valueDelivered: formValue.valueDelivered,
      toolsAndTechnologies: formValue.toolsAndTechnologies,
      keyResults: formValue.keyResults,
      narrationGuide: formValue.narrationGuide,

      // static values
      approverId: numericId,
      isActive: true,
      creatorId: numericId,

      // ✅ speakers mapped into API format
      speakers: [
        { speakerEid: formValue.primarySpeaker, speakerType: 'PRIMARY' },
        { speakerEid: formValue.secondarySpeaker, speakerType: 'SECONDARY' },
        { speakerEid: formValue.tertiarySpeaker, speakerType: 'TERTIARY' }
      ].filter(s => s.speakerEid),

      // ✅ tags as plain array of strings
      tags: (formValue.tags || []).map((t: any) => t),
      // tags: [
      //   "Digital Twin",
      //   "Real Time Monitoring",
      //   "Pharma Innovation",
      //   "Plant Simulation"
      // ],
      faq: (formValue.faqs || []).map((f: any) => ({
        question: f.question,
        answer: f.answer
      })),
      // artifacts unchanged
      artifacts: [],
    };

    const formData = new FormData();
    formData.append('data', JSON.stringify(payload));

    if(this.thumbnailFile) {
      formData.append('thumbnailImage', this.thumbnailFile);
    }

    if(this.bannerFile) {
      formData.append('bannerImage', this.bannerFile);
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

    this.usecaseService.saveAsDraft(formData).subscribe((res: any) => {

      console.log('Response from API:', res);
      this.toastTitle = 'Story Saved As Draft';
      //this.toastMessage = res;
      this.showToast = true;
      this.location.back();

    });
  }
  cancelForm() {
    this.storyForm.reset();
  }

  onCancel() {
    this.location.back();
  }
}
