import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Story {
  title: string;
  category: string;
  description: string;
  image: string;
}

@Component({
  selector: 'app-featured-stories',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './featured-stories.html',
  styleUrl: './featured-stories.scss'
})
export class FeaturedStoriesComponent {

  currentIndex = 0;

  stories: Story[] = [
    {
      title: 'Lyophilization Process Modeling for a Leading Global Biotechnology Company',
      category: 'Life sciences • Pharma',
      description: `A global biopharmaceutical leader faced significant variability and scale-up risks in its lyophilization process, impacting operational predictability and efficiency. To address this, a physics-informed digital twin capability was implemented.`,
      image: 'assets/featured1.png'
    },
    {
      title: 'Smart Factory Optimization for Industrial Manufacturing',
      category: 'Industrials • Smart Operations',
      description: `A leading manufacturer modernized operations using AI-driven predictive analytics and IoT integration, reducing downtime and increasing production throughput.`,
      image: 'assets/featured2.jpg'
    },
    {
      title: 'Energy Grid Digitization and Analytics Transformation',
      category: 'Energy • Utilities',
      description: `Digital twin modeling and real-time monitoring improved grid stability and asset reliability across multiple energy distribution sites.`,
      image: 'assets/featured3.png'
    },
    {
      title: 'High-Tech Semiconductor Production Enhancement',
      category: 'High Tech • Manufacturing',
      description: `Advanced process simulation and analytics improved yield rates and reduced cycle time across semiconductor fabrication plants.`,
      image: 'assets/featured4.png'
    }
  ];

  next() {
    if (this.currentIndex < this.stories.length - 1) {
      this.currentIndex++;
    }
  }

  prev() {
    if (this.currentIndex > 0) {
      this.currentIndex--;
    }
  }

  get currentStory() {
    return this.stories[this.currentIndex];
  }
}

