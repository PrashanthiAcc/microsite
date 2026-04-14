import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, FormsModule, ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { Router, RouterModule } from '@angular/router';
import { CustomDropdownComponent } from '../../../../shared/components/custom-dropdown/custom-dropdown';
import { ToasterComponent } from '../../../../shared/components/toaster/toaster';
import { IndustryService } from '../../../../core/services/industry';
import { UsecaseService } from '../../../../core/services/usecase';
import { UserService } from '../../../../core/services/users';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-homepage-configurations',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, CustomDropdownComponent, ToasterComponent, RouterModule],
  templateUrl: './homepage-configurations.html',
  styleUrls: ['./homepage-configurations.scss'],
})
export class HomepageConfigurationsComponent {
  downArrowIconPath = 'assets/icons/down_arrow.png';
  upArrowIconPath = 'assets/icons/arrow-up.png';
  exchangeIcon = 'assets/icons/exchange.png';



  homePageForm: FormGroup;
  industries: any[] = [];
  subIndustries: any[] = [];
  valueChains: any[] = [];
  showStoryModal = false;

  constructor(private fb: FormBuilder, private router: Router, private http: HttpClient,
    private industryService: IndustryService, private userService: UserService, private usecaseService: UsecaseService, private cdr: ChangeDetectorRef) {

    this.homePageForm = this.fb.group({

    })
  }
  // Track which accordion is open
  isAccordionOpen = {
    hero: true,
    overview: false,
    industries: false,
    featured: false,
    capabilities: false
  };

  // Toggle function with "only one open at a time" behavior
  toggleAccordion(section: 'hero' | 'overview' | 'industries' | 'featured' | 'capabilities') {
    const currentlyOpen = this.isAccordionOpen[section];

    // Close all
    // this.isAccordionOpen.hero = false;
    // this.isAccordionOpen.overview = false;
    // this.isAccordionOpen.industries = false;
    // this.isAccordionOpen.featured = false;
    // this.isAccordionOpen.capabilities = false;

    // Reopen only if it wasn’t already open
    this.isAccordionOpen[section] = !currentlyOpen;
  }

  openStoryModal() {
  this.showStoryModal = true;
}
}