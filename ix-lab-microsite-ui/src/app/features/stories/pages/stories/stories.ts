import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';
import { ChangeDetectorRef } from '@angular/core';
import { ToasterComponent } from '../../../../shared/components/toaster/toaster';


@Component({
  selector: 'app-stories',
  standalone: true,
  imports: [CommonModule, CustomDropdownComponent, RouterModule, ToasterComponent],
  templateUrl: './stories.html',
  styleUrls: ['./stories.scss']
})
export class StoriesComponent implements OnInit {
  showAdminControls = false;
  industries: any[] = [];
  subIndustries: any[] = [];
  valueChains: any[] = [];

  allSubIndustries: any[] = [];
  allValueChains: any[] = [];
  stories: any[] = [];
  drafts: any[] = [];
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

  constructor(private router: Router, private http: HttpClient,
    private industryService: IndustryService, private cdr: ChangeDetectorRef, private route: ActivatedRoute,
    private userService: UserService, private usecaseService: UsecaseService
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.showAdminControls = params['from'] === 'config';

    this.route.queryParams.subscribe(params => {
      const id = params['id'];
      if(id) {
        console.log("selected story Id:", id);
      }
    })
    });

    this.loadIndustries();
    this.loadAllStories();
    this.getAllUsers();
    window.scrollTo({ top: 0 });
  }

  // loadAllStories() {
  //   this.usecaseService.getAllStories().subscribe((res: any) => {
  //     const allStories = res.content || [];
  //     // this.stories = res.content.map((story: any) => ({
  //     //   ...story,
  //     //   ownerName: this.getOwnerName(story.ownerEId)
  //     // }));
  //     this.stories = allStories.map((story: any) => ({
  //       ...story,
  //       ownerName: this.getOwnerName(story.ownerEId)
  //     }));

  //     this.drafts = allStories
  //       .filter((story: any) => story.status === 'DRAFT')
  //       .map((draft: any) => ({
  //         ...draft,
  //         ownerName: this.getOwnerName(draft.ownerEId)
  //       }));
  //     console.log("drafts::", this.drafts);
  //     this.cdr.detectChanges();
  //   });
  // }


loadAllStories(page: number = 1, size: number = 10) {
  this.usecaseService.getAllStories(page, size).subscribe((res: any) => {
    const allStories = res.content || [];

    this.stories = allStories.map((story: any) => ({
      ...story,
      tags: story.tag,
      ownerName: this.getOwnerName(story.ownerEId)
    }));

    this.drafts = allStories
      .filter((story: any) => story.status === 'DRAFT')
      .map((draft: any) => ({
        ...draft,
        tags: draft.tag,
        ownerName: this.getOwnerName(draft.ownerEId)
      }));

    this.currentPage = res.number + 1;
    this.totalPages = res.totalPages;
    this.totalElements = res.totalElements;
    this.pageSize = res.size;

    this.cdr.detectChanges();
    console.log('Stories loaded:', this.stories);
  });
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

  loadIndustries() {
    this.industryService.getIndustries().subscribe((res: any) => {

      this.industries = res.industries;

      this.allSubIndustries = res.subIndustries;
      this.allValueChains = res.valueChains;

    });
  }

  onIndustrySelected(industry: any) {

    if (!industry) {
      this.subIndustries = [];
      this.valueChains = [];
      return;
    }

    this.subIndustries = this.allSubIndustries.filter(
      sub => sub.industryId === industry.industryId
    );

    this.valueChains = [];
  }

  onSubIndustrySelected(sub: any) {

    if (!sub) {
      this.valueChains = [];
      return;
    }

    this.valueChains = this.allValueChains.filter(
      vc => vc.subIndustryId === sub.subIndustryId
    );

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
    this.usecaseService.discardDraft(usecaseId).subscribe({
      next: (res: string) => {
        console.log('API success, response:', res);

        this.toastTitle = 'Draft Discarded Successfully';
        this.toastMessage = res;
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
        console.error('API error:', err);
      }
    });
  }
}
