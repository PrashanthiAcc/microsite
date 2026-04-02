import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../../core/services/users';
import { UsecaseService } from '../../../../core/services/usecase';
import { RouterModule } from '@angular/router';

interface Story {
  title: string;
  category: string;
  description: string;
  image: string;
}

@Component({
  selector: 'app-featured-stories',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './featured-stories.html',
  styleUrl: './featured-stories.scss'
})
export class FeaturedStoriesComponent {

  stories: any[] = [];
  currentIndex = 0;
  currentStory: any;

  constructor(private userCaseService: UsecaseService, private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.loadStories();
  }

  loadStories() {
    this.userCaseService.getFeaturedStories().subscribe((res: any) => {

      if (res?.content) {

        // ✅ 1. Filter active
        const activeStories = res.content.filter(
          (story: any) => story.isActive === true
        );

        // ✅ 2. Sort latest first
        const sortedStories = activeStories.sort((a: any, b: any) => {
          return new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime();
        });

        // ✅ 3. Take max 4
        this.stories = sortedStories.slice(0, 4);

        // ✅ 4. Set first story
        this.currentStory = this.stories[0];

        this.cdr.detectChanges();
      }
    });
  }

  next() {
    if (this.currentIndex < this.stories.length - 1) {
      this.currentIndex++;
      this.currentStory = this.stories[this.currentIndex];
    }
  }

  prev() {
    if (this.currentIndex > 0) {
      this.currentIndex--;
      this.currentStory = this.stories[this.currentIndex];
    }
  }

  // get currentStory() {
  //   return this.stories[this.currentIndex];
  // }
}

