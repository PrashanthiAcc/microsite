import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterModule } from '@angular/router';

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

  constructor(private elRef: ElementRef, private router: Router) {}

  ngOnInit() {
    this.setRoleFromStorage();
    this.router.events.subscribe(() => {
      this.setRoleFromStorage();
    });
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
