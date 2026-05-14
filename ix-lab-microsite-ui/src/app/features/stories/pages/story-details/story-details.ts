import { Component } from '@angular/core';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { UsecaseService } from '../../../../core/services/usecase';
import { ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormArray, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';


// interface StoryDetails {
//   usecaseId: number;
//   industryId: number;
//   subIndustryId: number;
//   valueChainId: number;
//   title: string;
//   thumbnailImageUrl: string;
//   bannerUrl: string
//   tag: string[];
//   description: string;
//   duration: number;
//   ownerId: number;

//   speakers: {
//     speakerEid: string;
//     speakerType: 'PRIMARY' | 'SECONDARY' | 'TERTIARY';
//   }[];

//   artifacts: {
//     artifactType: string;
//     url: string;
//     artifactName: string;
//   }[];

//   faqs: ({
//     question: string;
//     answer: string;
//     updatedBy: number;
//     lastUpdated: string | null;
//     showAnswer?: boolean;   // purely for UI toggle
//   })[];

//   businessProblem: string;
//   solutions: string;
//   valueDelivered: string;
//   toolsAndTechnologies: string;
//   keyResults: string;

//   status: string;
//   approverId: number;
//   approvedDate: string | null;
//   createdDate: string;
//   updatedDate: string | null;
//   isActive: boolean;
//   creatorId: number;
//   narrationGuide: string;
// }
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
  selector: 'app-story-details',
  imports: [CustomDropdownComponent, CommonModule, ReactiveFormsModule],
  templateUrl: './story-details.html',
  styleUrls: ['./story-details.scss'],
})
export class StoryDetailsComponent {
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
  leftArrowIconPath = 'assets/icons/arrow_left.png';
  pencilIconPath = 'assets/icons/pencil.png';
  saveIconPath = 'assets/icons/save_white.png';

  showNarrationGuideAndFAQ = false;
  storyDetails: StoryDetails = {} as StoryDetails;
  clientCredentials: any[] = [];
  demoVideos: any[] = [];
  clientTestimonials: any[] = [];
  fromPage: string = 'stories';

  faqs!: FormArray<FormGroup>;
  faqBackup: any[] = [];
  storyForm!: FormGroup;
  videoList: string[] = [];
  currentVideo: string = '';
  currentIndex = 0;
  showVideoPlayer = false;
  storyID: any;
  constructor(private route: ActivatedRoute, private usecaseService: UsecaseService, private cdr: ChangeDetectorRef,
    private router: Router, private fb: FormBuilder
  ) { this.faqs = this.fb.array<FormGroup>([]); }

  ngOnInit() {
    this.storyForm = this.fb.group({
      clientCredential: [''],
      demoVideo: [''],
      clientTestimonial: ['']
    });
    window.scrollTo({ top: 0 });
    this.videoList = this.demoVideos?.map(v => v.url) || [];
    this, this.fromPage = this.route.snapshot.queryParams['from'] || 'stories';
    this.storyID = this.route.snapshot.paramMap.get('id');
    // if (id) {
    //   this.usecaseService.getStoryDetailsById(id).subscribe(res => {
    //     this.storyDetails = res as StoryDetails;

    //     this.storyDetails.businessProblem = this.cleanContent(this.storyDetails.businessProblem);
    //     this.storyDetails.solutions = this.cleanContent(this.storyDetails.solutions);
    //     this.storyDetails.valueDelivered = this.cleanContent(this.storyDetails.valueDelivered);
    //     this.storyDetails.toolsAndTechnologies = this.cleanContent(this.storyDetails.toolsAndTechnologies);
    //     this.storyDetails.keyResults = this.cleanContent(this.storyDetails.keyResults);
    //     this.storyDetails.narrationGuide = this.cleanContent(this.storyDetails.narrationGuide);
    //     console.log('Fetched story details:', this.storyDetails);
    //     // Initialize FAQ toggle state
    //     // this.storyDetails.faqs = this.storyDetails.faqs.map(f => ({ ...f, showAnswer: false }));

    //     this.storyDetails.faqs = (this.storyDetails.faqs ?? []).map(f => ({ ...f, showAnswer: false }));

    //     this.faqs.clear();
    //     (this.storyDetails.faqs ?? []).forEach(f => {
    //       this.faqs.push(this.fb.group({
    //         question: [f.question, Validators.required],
    //         answer: [f.answer, Validators.required],
    //         editing: [false],
    //         showAnswer: [false],
    //         updated: [false],
    //         usecaseFaqId: [f.usecaseFaqId],
    //         updatedBy: [f.updatedBy]
    //       }));
    //     });

    //     // Split artifacts by type
    //     this.clientCredentials = this.storyDetails.artifacts.filter(a => a.artifactType === 'ELEVATOR_PITCH' || a.artifactType === 'USER_STORY');
    //     this.demoVideos = this.storyDetails.artifacts.filter(a => a.artifactType === 'DEMO_VIDEO');
    //     this.clientTestimonials = this.storyDetails.artifacts.filter(a => a.artifactType === 'CLIENT_TESTIMONIAL');

    //     this.cdr.detectChanges();
    //   });
    // }
    this.getStoryDetails(this.storyID);
  }


  getStoryDetails(id: any) {
    if (id) {
      this.usecaseService.getStoryDetailsById(id).subscribe(res => {
        this.storyDetails = res as StoryDetails;

        this.storyDetails.businessProblem = this.cleanContent(this.storyDetails.businessProblem);
        this.storyDetails.solutions = this.cleanContent(this.storyDetails.solutions);
        this.storyDetails.valueDelivered = this.cleanContent(this.storyDetails.valueDelivered);
        this.storyDetails.toolsAndTechnologies = this.cleanContent(this.storyDetails.toolsAndTechnologies);
        this.storyDetails.keyResults = this.cleanContent(this.storyDetails.keyResults);
        this.storyDetails.narrationGuide = this.cleanContent(this.storyDetails.narrationGuide);
        console.log('Fetched story details:', this.storyDetails);
        // Initialize FAQ toggle state
        // this.storyDetails.faqs = this.storyDetails.faqs.map(f => ({ ...f, showAnswer: false }));

        this.storyDetails.faqs = (this.storyDetails.faqs ?? []).map(f => ({ ...f, showAnswer: false }));

        this.faqs.clear();
        (this.storyDetails.faqs ?? []).forEach(f => {
          this.faqs.push(this.fb.group({
            question: [f.question, Validators.required],
            answer: [f.answer, Validators.required],
            editing: [false],
            showAnswer: [false],
            updated: [false],
            usecaseFaqId: [f.usecaseFaqId],
            updatedBy: [f.updatedBy]
          }));
        });

        // Split artifacts by type
        this.clientCredentials = this.storyDetails.artifacts.filter(a => a.artifactType === 'ELEVATOR_PITCH' || a.artifactType === 'USER_STORY');
        //this.demoVideos = this.storyDetails.artifacts.filter(a => a.artifactType === 'DEMO_VIDEO');
        this.demoVideos = this.storyDetails.artifacts.filter(
          a => a.artifactType === 'DEMO_VIDEO' || a.artifactType === 'DEMO_VIDEO_LINK'
        );
        this.videoList = this.demoVideos.map(a => a.url);
        this.clientTestimonials = this.storyDetails.artifacts.filter(a => a.artifactType === 'CLIENT_TESTIMONIAL');

        this.cdr.detectChanges();
      });
    }
  }

  cleanContent(content: string): string {
    if (!content) return '';

    return content
      .replace(/&nbsp;/g, ' ')
      .replace(/\u00A0/g, ' ')
      .trim();
  }

  onArtifactSelected(artifact: any) {
    const fileName = artifact.artifactName?.toLowerCase() || '';
    const cleanUrl = artifact.url;

    const isPPT = fileName.endsWith('.ppt') || fileName.endsWith('.pptx');
    const isVideo = /\.(mp4|webm|ogg)$/i.test(fileName);

    if (isPPT) {
      const officeUrl = `ms-powerpoint:ofe|u|${artifact.url}`;
      window.location.href = officeUrl;
    } // ✅ VIDEO
    else if (isVideo) {

      const intentUrl = `intent://${cleanUrl.replace(/^https?:\/\//, '')}#Intent;scheme=https;type=video/mp4;end`;
      window.location.href = intentUrl;

      this.playVideoInPlayer(cleanUrl);

      // setTimeout(() => {
      //   this.playVideoInPlayer(cleanUrl);
      // }, 1500);
    } else {
      window.open(cleanUrl, '_blank');
    }
  }

  closeVideo() {
    const video = document.querySelector('video') as HTMLVideoElement;

    if (video) {
      video.pause();
      video.currentTime = 0;
    }

    this.showVideoPlayer = false;
    this.currentVideo = '';
  }

  // playVideoInPlayer(url: string) {
  //   this.showVideoPlayer = true;

  //   if (!this.videoList.includes(url)) {
  //     this.videoList.push(url);
  //   }

  //   this.currentIndex = this.videoList.indexOf(url);
  //   this.currentVideo = url;
  // }


  // playNext() {
  //   this.currentIndex++;

  //   if (this.currentIndex < this.videoList.length) {
  //     this.currentVideo = this.videoList[this.currentIndex];
  //   } else {
  //     this.currentIndex = 0; // loop
  //     this.currentVideo = this.videoList[0];
  //   }
  // }

  playVideoInPlayer(url: string) {
  this.showVideoPlayer = true;

  if (!this.videoList.includes(url)) {
    this.videoList.push(url);
  }
  console.log("video list::", this.videoList)
  this.currentIndex = this.videoList.indexOf(url);
  this.currentVideo = url;

  setTimeout(() => {
    const video = document.querySelector('video') as HTMLVideoElement;
    if (video) {
      video.addEventListener('ended', () => {
        if (this.videoList.length === 1) {
          // ✅ Only one video → loop same video
          video.currentTime = 0;
          video.play();
        } else {
          // ✅ Multiple videos → go to next
          this.playNext();
        }
      });
    }
  }, 0);
}

playNext() {
  this.currentIndex++;

  if (this.currentIndex < this.videoList.length) {
    this.currentVideo = this.videoList[this.currentIndex];
  } else {
    this.currentIndex = 0; // loop back to first
    this.currentVideo = this.videoList[0];
  }
}


  // if (fileName.endsWith('.ppt') || fileName.endsWith('.pptx')) {
  //   // const viewerUrl = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(artifact.url)}`;

  //   // const newTab = window.open('', '_blank');

  //   // if (newTab) {
  //   //   newTab.location.href = viewerUrl;
  //   // }
  //   const officeUrl = `ms-powerpoint:ofe|u|${artifact.url}`;
  //   window.location.href = officeUrl;

  // } else {
  //   window.open(cleanUrl, '_blank');
  // }
  //}

  toggleFaqGuide(event: Event): void {
    console.log('FAQ & Guide checkbox changed:', event);
    const input = event.target as HTMLInputElement;
    this.showNarrationGuideAndFAQ = input.checked;
  }

  // navigateBack() {
  //   const queryParams = this.route.snapshot.queryParams;

  //   this.router.navigate(['/stories'], {
  //     queryParams: {
  //       from: queryParams['from'] || 'stories',
  //       page: queryParams['page'] || 1,
  //       search: queryParams['search'] || null,
  //       industry: queryParams['industry'] || null,
  //       subIndustry: queryParams['subIndustry'] || null,
  //       valueChain: queryParams['valueChain'] || null
  //     }
  //   });
  // }

  navigateBack() {
  const queryParams = this.route.snapshot.queryParams;

  // Check if the user came from archived
  const from = queryParams['from'] || 'stories';

  if (from === 'archived') {
    this.router.navigate(['/archived'], {
      queryParams: {
        from: 'archived',
        page: queryParams['page'] || 1,
        search: queryParams['search'] || null,
        industry: queryParams['industry'] || null,
        subIndustry: queryParams['subIndustry'] || null,
        valueChain: queryParams['valueChain'] || null
      }
    });
  } else if (from === 'favourites') {
    this.router.navigate(['/favourites'], {
      queryParams: {
        from: 'favourites',
        page: queryParams['page'] || 1,
        search: queryParams['search'] || null,
        industry: queryParams['industry'] || null,
        subIndustry: queryParams['subIndustry'] || null,
        valueChain: queryParams['valueChain'] || null
      }
    });
  }else {
    this.router.navigate(['/stories'], {
      queryParams: {
        from: 'stories',
        page: queryParams['page'] || 1,
        search: queryParams['search'] || null,
        industry: queryParams['industry'] || null,
        subIndustry: queryParams['subIndustry'] || null,
        valueChain: queryParams['valueChain'] || null
      }
    });
  }
}

  get faqFormGroups(): FormGroup[] {
    return this.faqs.controls as FormGroup[];
  }

  addFAQ() {
    const faqGroup = this.fb.group({
      usecaseFaqId: [null],
      usecaseId: [this.storyDetails.usecaseId],
      question: ['', Validators.required],
      answer: ['', Validators.required],
      editing: [true],
      showAnswer: [false],
      updated: [false],
      updatedBy: [this.storyDetails.creatorId] // or current user ID
    });
    this.faqs.insert(0, faqGroup);
  }



  editFAQ(i: number) {
    const faqGroup = this.faqs.at(i);
    this.faqBackup[i] = { ...faqGroup.value };
    faqGroup.patchValue({ editing: true, showAnswer: true });
  }

  saveFAQ(i: number) {
    const faqGroup = this.faqs.at(i);
    if (faqGroup.valid) {
      faqGroup.patchValue({ editing: false, updated: true });
      // TODO: send faqGroup.value to backend
    }
  }

  updateFAQ(i: number) {
    const faqGroup = this.faqs.at(i);
    console.log('Updating FAQ:', faqGroup.value);
    // TODO: call backend update API here
  }

  cancelFAQ(i: number) {
    const faqGroup = this.faqs.at(i);
    const original = this.faqBackup[i];
    if (original) {
      faqGroup.setValue(original);
    }
    faqGroup.patchValue({ editing: false, showAnswer: false });
  }

  deleteFAQ(i: number) {
    this.faqs.removeAt(i);
    this.faqBackup.splice(i, 1);
  }

  get hasUpdatedFaq(): boolean {
    return this.faqs.controls.some(f => f.get('updated')?.value === true);
  }

  updateFAQPayload() {
    // Collect the entire FAQ array
    const payload = this.faqs.value.map((f: any) => ({
      usecaseFaqId: f.usecaseFaqId,
      question: f.question,
      answer: f.answer,
      updatedBy: f.updatedBy
    }));

    console.log('Sending FAQ payload:', payload);

    // TODO: call your backend service here
    this.usecaseService.updateFaqs(this.storyDetails.usecaseId, payload)
      .subscribe({
        next: res => {
          console.log('FAQ update response:', res)
          this.getStoryDetails(this.storyID);
        },
        error: err => {
          console.error('FAQ update failed:', err)
        }
      });
  }


}