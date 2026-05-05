import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener } from '@angular/core';
import { EventManager } from '@angular/platform-browser';
import { Router, RouterLink, RouterLinkActive, RouterModule, NavigationEnd, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink, RouterLinkActive, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class NavbarComponent {

  adminOpen = false;
  userPhotoUrl = 'assets/images/profile.png'; // existing value
  isPresenter = false;
  role: string = 'superadmin';

  constructor(private elRef: ElementRef, private router: Router, private route: ActivatedRoute) {
   
  }

  ngOnInit() {
    this.setRoleFromRoute();
    this.router.events.subscribe(() => {
      this.setRoleFromRoute();
    })
  }

  // updateRole() {
  //   const url = this.router.url;
  //   this.isPresenter = url.startsWith('/presenter') || url.startsWith('/stories');
  //   this.role = this.isPresenter ? 'presenter' : 'admin'
  // }

  setRoleFromRoute() {
  let route = this.router.routerState.snapshot.root;

  let role = 'superadmin'; // default

  while (route) {
    if (route.data && route.data['role']) {
      role = route.data['role']; // 👈 overwrite if found
    }
    route = route.firstChild!;
  }

  this.role = role;
}

// else if condition for favourites is added for now, once roles are in place then else if is not required and component routing will take care of it 
// getRoute(path?: string) {
//   if (this.role === 'presenter' && path !== 'favourites') {
//     return path ? `/presenter/${path}` : '/presenter';
//   } else if (this.role === 'presenter' && path === 'favourites') {
//     return '/presenter';
//   }
//   return path ? `/${path}` : '/';
// }

getRoute(path?: string) {
  if (this.role === 'presenter' && path !== 'favourites') {
    return path ? `/presenter/${path}` : '/presenter';
  } else if (this.role === 'presenter' && path === 'favourites') {
    return '/presenter';
  }
  // 👇 for superadmin, just return root-level paths
  else if (this.role === 'superadmin') {
    return path ? `/${path}` : '/';
  }
  // default (admin)
  return path ? `/${path}` : '/';
}



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
