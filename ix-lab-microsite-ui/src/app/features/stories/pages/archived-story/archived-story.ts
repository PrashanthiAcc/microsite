import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-archived-story',
  imports: [CommonModule, CustomDropdownComponent, RouterModule],
  templateUrl: './archived-story.html',
  styleUrl: './archived-story.scss',
})
export class ArchivedStoriesComponent {
showAdminControls = false;
  industries: any[] = [];
  subIndustries: any[] = [];
  valueChains: any[] = [];

  allSubIndustries: any[] = [];
  allValueChains: any[] = [];
  archivedStories: any[] = [];
  allUsers: any[] = [];

  constructor(private router: Router, private http: HttpClient,
    private industryService: IndustryService, private cdr: ChangeDetectorRef, private route: ActivatedRoute,
    private userService: UserService, private usecaseService: UsecaseService
  ) { }

   ngOnInit() {
    window.scrollTo({ top: 0 });
    this.route.queryParams.subscribe(params => {
      this.showAdminControls = params['from'] === 'config';
    });

    this.loadIndustries();
    this.loadAllStories();
    this.getAllUsers();

  }

  loadAllStories() {
    this.usecaseService.getAllStories().subscribe((res: any) => {
      const allStories = res.content || [];
    
      this.archivedStories = allStories
        .filter((story: any) => story.status === 'ARCHIVED')
        .map((archived: any) => ({
          ...archived,
          ownerName: this.getOwnerName(archived.ownerEId)
        }));
        console.log("archivedStories::", this.archivedStories);
      this.cdr.detectChanges();
    });
  }

  getAllUsers() {
    this.userService.getAllUsers().subscribe((res: any) => {
      this.allUsers = res;

      // re-map owner names if stories already loaded
      this.archivedStories = this.archivedStories.map(story => ({
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

  
  currentPage = 1;

  toggleMenu(story: any, event?: MouseEvent) {
    if (event) {
      event.stopPropagation();
    }
    // close others first
    this.archivedStories.forEach(s => {
      if (s !== story) {
        s.showMenu = false;
      }
    });
    story.showMenu = !story.showMenu;
  }

  @HostListener('document:click')
  closeMenus() {
    this.archivedStories.forEach(s => (s.showMenu = false));
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


}
