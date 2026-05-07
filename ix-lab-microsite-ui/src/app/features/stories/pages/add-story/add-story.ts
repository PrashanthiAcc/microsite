import { ChangeDetectorRef, Component, signal } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators, FormsModule } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';
import { ToasterComponent } from '../../../../shared/components/toaster/toaster';
import { Spinner } from '../../../../shared/components/spinner/spinner';

@Component({
  selector: 'app-add-story',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, CustomDropdownComponent, ToasterComponent, Spinner],
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
  arrowLeftIcon = "assets/icons/left.png";
  isOpen: boolean = true;
  showApprovalModal: boolean = false;
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
  showToast = false;
  toastMessage = '';
  toastTitle = '';
  elevatorFileName: string = '';
  storyFileName: string = '';
  thumbnailFile: File | null = null;
  bannerFile: File | null = null;
  thumbnailPreview: string | ArrayBuffer | null = null;
  bannerPreview: string | ArrayBuffer | null = null;
  isDataLoading = signal(false);
  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient, private location: Location,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService, private cd: ChangeDetectorRef) {
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
      // clientCredentials: this.fb.array([this.createArtifact()]),
      demoVideos: this.fb.array([this.createDemoVideo()]),
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

    this.demoVideos.controls.forEach((ctrl, i) => {
      const videoGroup = ctrl as FormGroup;
      videoGroup.get('link')?.valueChanges.subscribe(val => {
        if (val) {
          videoGroup.get('file')?.reset(null);
          videoGroup.get('file')?.disable();
        } else {
          videoGroup.get('file')?.enable();
        }
      });
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

  createDemoVideo() {
    return this.fb.group({
      file: [null],
      link: ['']
    });
  }
  // Create artifact control (file or null)
  createArtifact() {
    return this.fb.control(null);
  }

  // GETTERS
  get tagsArray(): FormArray {
    return this.storyForm.get('tags') as FormArray;
  }
  // get clientCredentials(): FormArray {
  //   return this.storyForm.get('clientCredentials') as FormArray;
  // }
  // get demoVideos(): FormArray {
  //   return this.storyForm.get('demoVideos') as FormArray;
  // }
  get demoVideos(): FormArray<FormGroup> {
    return this.storyForm.get('demoVideos') as FormArray<FormGroup>;
  }
  get clientTestimonials(): FormArray {
    return this.storyForm.get('clientTestimonials') as FormArray;
  }
  get faqs(): FormArray<FormGroup> {
    return this.storyForm.get('faqs') as FormArray<FormGroup>;
  }


  // ADD FIELD
  // addField(array: FormArray) {
  //   array.push(this.createArtifact());
  // }

  addField(array: FormArray) {
    if (array === this.demoVideos) {
      const newGroup = this.createDemoVideo();
      array.push(newGroup);

      // attach exclusivity for new demoVideo row
      (newGroup as FormGroup).get('link')?.valueChanges.subscribe(val => {
        if (val) {
          (newGroup as FormGroup).get('file')?.reset(null);
          (newGroup as FormGroup).get('file')?.disable();
        } else {
          (newGroup as FormGroup).get('file')?.enable();
        }
      });
    } else {
      array.push(this.createArtifact());
    }
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



  // handleFileUpload(event: Event, field: string, index?: number) {
  //   const input = event.target as HTMLInputElement;
  //   const file = input.files?.[0];
  //   if (file) {
  //     // ✅ size check
  //     if (file.size > this.fileSizeLimits[field]) {
  //       alert(`File size exceeds ${this.fileSizeLimits[field] / (1024 * 1024)}MB. Please upload a smaller file.`);
  //       input.value = '';
  //       return;
  //     }

  //     // ✅ type check
  //     if (!this.allowedFileTypes[field].includes(file.type)) {
  //       alert(`Invalid file type. Allowed types: ${this.allowedFileTypes[field].join(', ')}`);
  //       input.value = '';
  //       return;
  //     }

  //     // ✅ save file into correct form control
  //     if (field === 'thumbnail') {
  //       this.storyForm.patchValue({ thumbnail: file });
  //       this.thumbnailFile = file;
  //     } else if (field === 'banner') {
  //       this.storyForm.patchValue({ banner: file });
  //       this.bannerFile = file;
  //     } else if (index !== undefined) {
  //       (this.storyForm.get(field) as FormArray).at(index).setValue(file);
  //     } else {
  //       this.storyForm.patchValue({ [field]: file });
  //     }

  //     const reader = new FileReader();
  //     reader.onload = (e: any) => {
  //       if (field === 'thumbnail') {
  //         this.thumbnailPreview = e.target.result;
  //       } else if (field === 'banner') {
  //         this.bannerPreview = e.target.result;
  //       }
  //       this.cd.detectChanges();
  //     };
  //     reader.readAsDataURL(file);
  //   }
  // }


  handleFileUpload(event: Event, field: string, index?: number) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      // size check
      if (file.size > this.fileSizeLimits[field]) {
        alert(`File size exceeds ${this.fileSizeLimits[field] / (1024 * 1024)}MB. Please upload a smaller file.`);
        input.value = '';
        return;
      }

      // type check
      if (!this.allowedFileTypes[field].includes(file.type)) {
        alert(`Invalid file type. Allowed types: ${this.allowedFileTypes[field].join(', ')}`);
        input.value = '';
        return;
      }

      // handle different fields
      if (field === 'thumbnail') {
        this.storyForm.patchValue({ thumbnail: file });
        this.thumbnailFile = file;

        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.thumbnailPreview = e.target.result;
          this.cd.detectChanges();
        };
        reader.readAsDataURL(file);

      } else if (field === 'banner') {
        this.storyForm.patchValue({ banner: file });
        this.bannerFile = file;

        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.bannerPreview = e.target.result;
          this.cd.detectChanges();
        };
        reader.readAsDataURL(file);

      } else if (field === 'clientTestimonials' && index !== undefined) {
        // plain control
        (this.storyForm.get(field) as FormArray).at(index).setValue(file);

      } else if (field === 'demoVideos' && index !== undefined) {
        const videoGroup = (this.storyForm.get(field) as FormArray).at(index) as FormGroup;
        videoGroup.get('file')?.setValue(file);

        // disable link if file chosen
        videoGroup.get('link')?.reset('');
        videoGroup.get('link')?.disable();

      } else {
        this.storyForm.patchValue({ [field]: file });
      }
    }
  }


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


  // addFAQ() {
  //   const faqGroup = this.fb.group({
  //     question: ['', Validators.required],
  //     answer: ['', Validators.required],
  //     editing: [true],
  //     showAnswer: [false]
  //   });
  //   //this.faqs.push(faqGroup);
  //   this.faqs.insert(0, faqGroup);
  // }

  // saveFAQ(index: number) {
  //   const faqGroup = this.faqs.at(index);
  //   if (faqGroup.valid) {
  //     faqGroup.patchValue({ editing: false }); // collapse view
  //   } else {
  //     alert('Please fill both Question and Answer before saving.');
  //   }
  // }

  // cancelFAQ(index: number) {
  //   if (index === 0) {
  //     this.faqs.at(index).reset({ question: '', answer: '', editing: true });
  //   } else {
  //     this.faqs.removeAt(index);
  //   }
  // }


  // deleteFAQ(index: number) {
  //   if (this.faqs.length > 1) {
  //     this.faqs.removeAt(index);
  //   } else {
  //     this.cancelFAQ(index);
  //   }
  // }

  addFAQ() {
    const faqGroup = this.fb.group({
      question: ['', Validators.required],
      answer: ['', Validators.required],
      editing: [true],
      showAnswer: [false],
      previousValue: this.fb.group({
        question: [''],
        answer: ['']
      })
    });
    this.faqs.insert(0, faqGroup);
  }

  saveFAQ(index: number) {
    const faqGroup = this.faqs.at(index);
    if (faqGroup.valid) {
      faqGroup.patchValue({ editing: false });
      (faqGroup.get('previousValue') as FormGroup).patchValue({
        question: faqGroup.get('question')?.value,
        answer: faqGroup.get('answer')?.value
      });
    } else {
      alert('Please fill both Question and Answer before saving.');
    }
  }

  editFAQ(index: number) {
    const faqGroup = this.faqs.at(index);
    faqGroup.patchValue({ editing: true });
  }

  cancelFAQ(index: number) {
    const faqGroup = this.faqs.at(index);
    const prev = faqGroup.get('previousValue') as FormGroup;

    const savedQuestion = prev.get('question')?.value;
    const savedAnswer = prev.get('answer')?.value;
    const hasSaved = !!(savedQuestion || savedAnswer);

    if (hasSaved) {
      faqGroup.patchValue({
        question: savedQuestion,
        answer: savedAnswer,
        editing: false,
        showAnswer: false
      });
    } else {
      this.faqs.removeAt(index);
    }
  }

  deleteFAQ(index: number) {
    this.faqs.removeAt(index);
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
    formData.append('useCaseRequest', JSON.stringify(payload));

    if (this.thumbnailFile) {
      formData.append('thumbnailUrl', this.thumbnailFile);
    }

    if (this.bannerFile) {
      formData.append('bannerUrl', this.bannerFile);
    }

    // this.demoVideos.controls.forEach((control: any) => {
    //   if (control.value instanceof File) {
    //     formData.append('demoVideos', control.value);
    //   }
    // });
    this.demoVideos.controls.forEach(ctrl => {
      const group = ctrl as FormGroup;
      const file = group.get('file')?.value;
      const link = group.get('link')?.value;

      if (file instanceof File) {
        formData.append('demoVideos', file);
      }
      if (link) {
        formData.append('demoVideoLinks', link);
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
      formData.append('userStory', clientStory);
    }

    // ✅ Debug logging before API call
    console.log('Payload JSON:', JSON.stringify(payload, null, 2));
    for (const [key, value] of formData.entries()) {
      if (value instanceof File) {
        console.log(`${key}: File -> name=${value.name}, size=${value.size}, type=${value.type}`);
      } else {
        console.log(`${key}:`, value);
      }
    }
    this.isDataLoading.set(true);
    this.usecaseService.submitForApproval(formData).subscribe({
      next: (res: any) => {
        this.isDataLoading.set(false);
        this.showToast = false;
        console.log('Response from API:', res);
        this.toastTitle = 'Story created and submitted for approval successfully';
        // this.toastMessage = 'Your story has been submitted'; // ✅ add message
        this.showToast = true;
        this.cd.detectChanges();
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
        this.cd.detectChanges();

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
    formData.append('useCaseRequest', JSON.stringify(payload));

    if (this.thumbnailFile) {
      formData.append('thumbnailUrl', this.thumbnailFile);
    }

    if (this.bannerFile) {
      formData.append('bannerUrl', this.bannerFile);
    }

    // this.demoVideos.controls.forEach((control: any) => {
    //   if (control.value instanceof File) {
    //     formData.append('demoVideos', control.value);
    //   }
    // });

    this.demoVideos.controls.forEach(ctrl => {
      const group = ctrl as FormGroup;
      const file = group.get('file')?.value;
      const link = group.get('link')?.value;

      if (file instanceof File) {
        formData.append('demoVideos', file);
      }
      if (link) {
        formData.append('demoVideoLinks', link);
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
      formData.append('userStory', clientStory);
    }
    this.isDataLoading.set(true);
    this.usecaseService.saveAsDraft(formData).subscribe({
      next: (res: any) => {
        this.isDataLoading.set(false);
        this.showToast = false;
        console.log('Response from API:', res);
        this.toastTitle = 'Story Saved As Draft';
        this.showToast = true;
        this.cd.detectChanges();
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
        this.cd.detectChanges();

      }

    });

  }
  cancelForm() {
    this.storyForm.reset();
  }

  onCancel() {
    this.location.back();
  }
}
