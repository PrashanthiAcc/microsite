import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterModule, NavigationEnd } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class NavbarComponent {

  adminOpen = false;
  userPhotoUrl = 'assets/images/profile.png'; // existing value

  constructor(private elRef: ElementRef, private router: Router) {
   
  }

  toggleAdminMenu() {
    this.adminOpen = !this.adminOpen;
  }

//  isAdminActive(): boolean {
//     return this.router.url.startsWith('/admin');
//   }
  @HostListener('document:click', ['$event'])
  handleDocumentClick(event: Event) {
    if (!this.elRef.nativeElement.contains(event.target)) {
      this.adminOpen = false;
    }
  }
}
