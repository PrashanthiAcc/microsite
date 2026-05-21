import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterModule } from '@angular/router';
import { HomePageService } from '../../../core/services/home-page.service';
import { AppStateService } from '../../../core/services/app-state.service';


@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class NavbarComponent {
  adminOpen = false;
  userPhotoUrl = 'assets/images/profile.png';
  role: string = ''; // no default
  getHomePageDetails: any;
  applicationName: string ='';

  constructor(private elRef: ElementRef, private router: Router,private homePageService: HomePageService,
    private cdRef: ChangeDetectorRef, private appStateService: AppStateService
  ) {}

  ngOnInit() {
    this.setRoleFromStorage();
    this.router.events.subscribe(() => {
      this.setRoleFromStorage();
    });
    this.appStateService.appName$.subscribe(name => {
    this.applicationName = name;
  });
    //this.getHomePageConfigDetails();
  }

   getHomePageConfigDetails(): void {
    this.homePageService.getHomePageData().subscribe({
      next: (res: any) => {
        console.log('fetched successfully', res);
        this.getHomePageDetails = res;
        this.cdRef.detectChanges();
      },
      error: err => console.error('Fetch failed', err)
    });
  }

  getAppNameParts(): string[] {
  if (!this.getHomePageDetails?.applicationName) return [];
  const appName = this.getHomePageDetails.applicationName;
  const prefix = 'Digital Manufacturing';
  if (appName.startsWith(prefix)) {
    return [prefix, appName.substring(prefix.length).trim()];
  }
  return [appName]; // fallback if it doesn't match
}

  /** ✅ Read role from localStorage only */
  setRoleFromStorage() {
    const userStr = localStorage.getItem('loggedUser');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.role = user.role?.toLowerCase() ?? '';
    } else {
      this.role = '';
    }
  }

  /** ✅ Role-based route resolution */
  getRoute(path?: string) {
    if (this.role === 'presenter' && path !== 'favourites') {
      return path ? `/${path}` : '/home';
    } else if (this.role === 'presenter' && path === 'favourites') {
      return '/favourites';
    } else if (this.role === 'superadmin') {
      return path ? `/${path}` : '/home';
    }
    // default (admin or unknown)
    return path ? `/${path}` : '/home';
  }

  toggleAdminMenu(event: Event) {
    event.stopPropagation();
    this.adminOpen = !this.adminOpen;
  }

  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('loggedUser');
    this.router.navigate(['/login']);
  }
  
  @HostListener('document:click', ['$event'])
  handleDocumentClick(event: Event) {
    if (!this.elRef.nativeElement.contains(event.target)) {
      this.adminOpen = false;
    }
  }

  
}
