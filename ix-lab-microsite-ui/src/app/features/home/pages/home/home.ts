import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Hero } from '../../components/hero/hero';
import { Industries} from '../../components/industries/industries';
import { FeaturedStoriesComponent } from '../../components/featured-stories/featured-stories';
import { KeyCapabilities } from '../../components/key-capabilities/key-capabilities';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    Hero,
    Industries,
    FeaturedStoriesComponent,
    KeyCapabilities
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent {
  isOpen = false;

  statistics = [
    { number: '150+', label: 'Client Stories' },
    { number: '50+', label: 'MES/MOM solutions delivered' },
    { number: '124+', label: 'Production Site support for Critical apps' },
    { number: '30+', label: 'SAP EWM programs delivered' },
    { number: '30+', label: 'SAP EWM programs delivered' }
  ];

  toggle() {
    this.isOpen = !this.isOpen;
  }
}
