import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { IndustryService } from '../../../../core/services/industry';

interface IndustryThumbnail {
  industryId: number;
  industryThumbnailId: number;
  industryThumbnailUrl: string;
  industryName: string;
  icon: string;
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

  private iconMap: Record<number, string> = {
    1: 'assets/icons/icon-cpg.png',
    2: 'assets/icons/icon-life.png',
    3: 'assets/icons/icon-industrials.png',
    4: 'assets/icons/icon-energy.png',
    5: 'assets/icons/icon-utilities.png',
    6: 'assets/icons/icon-chemicals.png',
    7: 'assets/icons/icon-hightech.png',
  };
  constructor(private router: Router, private industryService: IndustryService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    console.log("industriesData::", this.industriesData);
    this.loadIndustries();
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

  return this.industriesData.industryThumbnails.map((thumb: any) => {
    const match = this.allIndustries.find(
      (ind: any) => ind.industryId === thumb.industryId
    );

    return {
      ...thumb,
      industryName: match ? match.industryName : 'Unknown Industry',
      icon: this.iconMap[thumb.industryId] || 'assets/icons/default.png'
    };
  });
}



  goToStories(industryTitle: string) {
    this.router.navigate(['/stories'], { queryParams: { from: 'stories', title: industryTitle } });
  }
}
  
