import { ChangeDetectorRef, Component, Input } from '@angular/core';
import { HomePageService } from '../../../../core/services/home-page.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hero',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './hero.html',
  styleUrl: './hero.scss',
})
export class Hero {
  @Input() heroData!: any
  approvedCount: any;
  constructor(private homePageService: HomePageService, private cdr: ChangeDetectorRef) { }
  ngOnInit() {
    console.log("heroData::", this.heroData)
    this.getApprovedStoryCount();
  }

  getApprovedStoryCount(): void {
    this.homePageService.getApprovedUsecaseCount().subscribe({
      next: (res: any) => {
        console.log('fetched successfully', res);

        const approvedCount = Number(res["Total Approved usecases"] || 0);
        const nvidiaCount = Number(this.heroData?.nvidiaStorycount || 0);

        // ✅ Extract the number from the response object
        this.approvedCount = (approvedCount + nvidiaCount).toString();

        // ✅ Patch the clientStories field with just the number
        this.cdr.detectChanges();
      },
      error: err => console.error('Fetch failed', err)
    });
  }
}
