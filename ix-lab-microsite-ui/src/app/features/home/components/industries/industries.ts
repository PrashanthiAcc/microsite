import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { IndustryService } from '../../../../core/services/industry';
import { HomePageService } from '../../../../core/services/home-page.service';

interface IndustryThumbnail {
  industryId: number;
  industryThumbnailId: number;
  industryThumbnailUrl: string;
  industryName: string;
  icon: string;
  usecaseCount: number;
}

@Component({
  selector: 'app-industries',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './industries.html',
  styleUrls: ['./industries.scss'],
})
export class Industries {
  @Input() industriesData!: any;
  industries: IndustryThumbnail[] = [];   // ✅ typed array
  allIndustries: any;
  usecaseCounts: Record<number, number> = {};

  private iconMap: Record<number, string> = {
    1: 'assets/icons/icon-cpg.png',
    2: 'assets/icons/icon-life.png',
    3: 'assets/icons/icon-industrials.png',
    4: 'assets/icons/icon-energy.png',
    5: 'assets/icons/icon-utilities.png',
    6: 'assets/icons/icon-chemicals.png',
    7: 'assets/icons/icon-hightech.png',
  };
  constructor(private router: Router, private industryService: IndustryService, private cdr: ChangeDetectorRef,
    private homePageService: HomePageService
  ) { }

  ngOnInit() {
    console.log("industriesData::", this.industriesData);
    this.loadIndustries();
    this.getUsecaseCountByIndustry();
  }

  getUsecaseCountByIndustry() {
    this.homePageService.getCountByIndustry().subscribe((res: any[]) => {
      // Example response: [{ usecaseCount: 4, industryId: 1 }]
      this.usecaseCounts = res.reduce((acc, item) => {
        acc[item.industryId] = item.usecaseCount;
        return acc;
      }, {} as Record<number, number>);

      this.industries = this.mergeIndustries();
      this.cdr.detectChanges();
    });
  }


  loadIndustries() {
    this.industryService.getIndustries().subscribe((res: any) => {
      this.allIndustries = res.industries;
      this.industries = this.mergeIndustries();
      console.log("Merged industries:", this.industries);
      this.cdr.detectChanges();
    });
  }


  mergeIndustries(): IndustryThumbnail[] {
    if (!this.industriesData?.industryThumbnails || !this.allIndustries) {
      return [];
    }

    const merged = this.industriesData.industryThumbnails.map((thumb: any) => {
      const match = this.allIndustries.find(
        (ind: any) => ind.industryId === thumb.industryId
      );

      return {
        ...thumb,
        industryName: match ? match.industryName : 'Unknown Industry',
        icon: this.iconMap[thumb.industryId] || 'assets/icons/default.png',
        usecaseCount: this.usecaseCounts[thumb.industryId] || 0
      };
    });

    // ✅ Add static industry object
    merged.push({
      industryId: 8,
      industryThumbnailId: 97,
      industryThumbnailUrl:
        'assets/nvidia.jpg',
      industryName: 'NVIDIA',
      icon: 'assets/icons/icon-hightech.png',
      usecaseCount: 0,
    } as IndustryThumbnail & { redirectUrl: string });

    return merged;
  }



  goToStories(industryTitle: string) {
    if (industryTitle == 'NVIDIA') {
      window.open('https://apps.powerapps.com/play/e/5379f250-d93e-ea4e-b5fd-2ed49b2e6505/a/03905ca3-d4da-4d43-b72b-18bef4657795?tenantId=e0793d39-0939-496d-b129-198edd916feb&hint=d533ac37-20c0-496d-a5a1-c0ade80760e1&sourcetime=1769492949529&source=portal&hidenavbar=true')
    } else
      this.router.navigate(['/stories'], { queryParams: { from: 'stories', title: industryTitle } });
  }
}

