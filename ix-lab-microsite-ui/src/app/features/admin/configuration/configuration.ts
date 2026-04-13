import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './configuration.html',
  styleUrl: './configuration.scss',
})
export class ConfigurationComponent {

  constructor(private router: Router) {}

  cards = [
    {
      title: 'Home Page Configurations',
      icon: 'home',
      route: '/homepage-configuration'
    },
    {
      title: 'User Management',
      icon: 'user',
      route: '/user-management'
    },
    {
      title: 'Stories Configuration',
      icon: 'stories',
      route: '/stories',
      active: true
    }
  ];

  // navigateTo(route: string) {
  //   this.router.navigate([route]);
  // }
  navigateTo(route: string) {
  if (route === '/stories') {
    this.router.navigate([route], { queryParams: { from: 'config' } });
  } else {
    this.router.navigate([route]);
  }
}

}
