import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Hero } from '../../components/hero/hero';
import { Industries } from '../../components/industries/industries';
import { FeaturedStoriesComponent } from '../../components/featured-stories/featured-stories';
import { KeyCapabilities } from '../../components/key-capabilities/key-capabilities';
import { HomePageService } from '../../../../core/services/home-page.service';

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
  getHomePageDetails: any;
  isOpen = false;

  statistics = [
    { number: '150+', label: 'Client Stories' },
    { number: '50+', label: 'MES/MOM solutions delivered' },
    { number: '124+', label: 'Production Site support for Critical apps' },
    { number: '30+', label: 'SAP EWM programs delivered' },
    { number: '30+', label: 'SAP EWM programs delivered' }
  ];
  heroData: any;
  industriesData: any
  keyCapabilitiesData: any;
  constructor(private homePageService: HomePageService, private cdr: ChangeDetectorRef){}

  toggle() {
    this.isOpen = !this.isOpen;
  }

  ngOnInit() {
    this.getHomePageConfigDetails();
    console.log("home")
  }

  getHomePageConfigDetails(): void {
    this.homePageService.getHomePageData().subscribe({
      next: (res: any) => {
        console.log('fetched successfully', res);
        this.getHomePageDetails = res;
        
        this.heroData = {
        applicationName: res.applicationName,
        title: res.title,
        subTitle: res.subTitle,
        heroImageUrl: res.heroImageUrl,
        mesMomSolDelivered: res.mesMomSolDelivered,
        prodSiteCriticalSupport: res.prodSiteCriticalSupport,
        sapEwmPrgDelivered: res.sapEwmPrgDelivered
      };

      this.industriesData = {
        industryThumbnails: res.industryThumbnails
      }

      this.keyCapabilitiesData = {
        keyCapabilityConfigurationImage: res.keyCapabilityConfigurationImage
      }
      
        this.cdr.detectChanges();
      },
      error: err => console.error('Fetch failed', err)
    });
  }
}
