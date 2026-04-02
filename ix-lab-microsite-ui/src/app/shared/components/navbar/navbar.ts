import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener } from '@angular/core';
import { EventManager } from '@angular/platform-browser';
import { Router, RouterLink, RouterLinkActive, RouterModule, NavigationEnd, ActivatedRoute } from '@angular/router';

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
  isPresenter = false;
  role: string = 'admin';

  constructor(private elRef: ElementRef, private router: Router, private route: ActivatedRoute) {
   
  }

  ngOnInit() {
    // this.updateRole();
    // this.router.events.subscribe(() => {
    //   this.updateRole();
    // })
    this.route.data.subscribe(data => {
      this.role = data['role'] || 'admin'
    });
  }

  // updateRole() {
  //   this.isPresenter = this.router.url.startsWith('/presenter');
  // }

  toggleAdminMenu(event: Event) {
    event.stopPropagation();
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
