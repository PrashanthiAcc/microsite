import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-industries',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './industries.html',
  styleUrls: ['./industries.scss'],
})
export class Industries {
  constructor(private router: Router){}
  industries = [
    { title: 'Consumer Package Goods', count: 15, image: 'CPG.png', icon: 'icons/icon-cpg.png' },
    { title: 'Life Sciences', count: 35, image: 'Life Sciences.png', icon: 'icons/icon-life.png' },
    { title: 'Industrials', count: 15, image: 'Industrials.png', icon: 'icons/icon-industrials.png' },
    { title: 'Energy', count: 18, image: 'Energy.png', icon: 'icons/icon-energy.png' },
    { title: 'Utilities', count: 28, image: 'Utilities.jpg', icon: 'icons/icon-utilities.png' },
    { title: 'Chemical and Natural Resources', count: 10, image: 'Chemical and Natural Resources.png', icon: 'icons/icon-chemicals.png' },
    { title: 'High Tech', count: 20, image: 'High Tech Industry.png', icon: 'icons/icon-hightech.png' }
  ];

   goToStories(industryTitle: string) {
    this.router.navigate(['/stories'], { queryParams: { from: 'stories', title: industryTitle } });
  }
}
