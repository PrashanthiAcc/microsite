import { Component } from '@angular/core';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { UsecaseService } from '../../../../core/services/usecase';
import { ChangeDetectorRef } from '@angular/core';

interface StoryDetails {
  usecaseId: number;
  industryId: number;
  subIndustryId: number;
  valueChainId: number;
  title: string;
  thumbnailImageUrl: string;
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

  faqs: ({
    question: string;
    answer: string;
    updatedBy: number;
    lastUpdated: string | null;
    showAnswer?: boolean;   // purely for UI toggle
  })[];

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
  imports: [CustomDropdownComponent, CommonModule],
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

  showNarrationGuideAndFAQ = false;
  storyDetails: StoryDetails = {} as StoryDetails;
  clientCredentials: any[] = [];
  demoVideos: any[] = [];
  clientTestimonials: any[] = [];
  fromPage: string = 'stories';

  constructor(private route: ActivatedRoute, private usecaseService: UsecaseService, private cdr: ChangeDetectorRef,
    private router: Router
  ) { }

  ngOnInit() {
    window.scrollTo({ top: 0 });
    this, this.fromPage = this.route.snapshot.queryParams['from'] || 'stories';
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.usecaseService.getStoryDetailsById(id).subscribe(res => {
        this.storyDetails = res as StoryDetails;
        console.log('Fetched story details:', this.storyDetails);
        // Initialize FAQ toggle state
        // this.storyDetails.faqs = this.storyDetails.faqs.map(f => ({ ...f, showAnswer: false }));

        this.storyDetails.faqs = (this.storyDetails.faqs ?? []).map(f => ({ ...f, showAnswer: false }));

        // Split artifacts by type
        this.clientCredentials = this.storyDetails.artifacts.filter(a => a.artifactType === 'ELEVATOR_PITCH' || a.artifactType === 'USER_STORY');
        this.demoVideos = this.storyDetails.artifacts.filter(a => a.artifactType === 'DEMO_VIDEO');
        this.clientTestimonials = this.storyDetails.artifacts.filter(a => a.artifactType === 'CLIENT_TESTIMONIAL');

        this.cdr.detectChanges();
      });
    }
  }

  onArtifactSelected(artifact: any) {
    const fileName = artifact.artifactName?.toLowerCase() || '';
    const cleanUrl = artifact.url;

    if (fileName.endsWith('.ppt') || fileName.endsWith('.pptx')) {
      const viewerUrl = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(artifact.url)}`;

      const newTab = window.open('', '_blank');

      if (newTab) {
        newTab.location.href = viewerUrl;
      }
    } else {
      window.open(cleanUrl, '_blank');
    }
  }

  toggleFaqGuide(event: Event): void {
    console.log('FAQ & Guide checkbox changed:', event);
    const input = event.target as HTMLInputElement;
    this.showNarrationGuideAndFAQ = input.checked;
  }

  navigateBack() {
  const queryParams = this.route.snapshot.queryParams;

  this.router.navigate(['/stories'], {
    queryParams: {
      from: queryParams['from'] || 'stories',
      page: queryParams['page'] || 1,
      search: queryParams['search'] || null,
      industry: queryParams['industry'] || null,
      subIndustry: queryParams['subIndustry'] || null,
      valueChain: queryParams['valueChain'] || null
    }
  });
}

}