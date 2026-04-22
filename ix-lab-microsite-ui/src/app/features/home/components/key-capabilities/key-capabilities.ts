import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-key-capabilities',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './key-capabilities.html',
  styleUrl: './key-capabilities.scss',
})
export class KeyCapabilities {
 @Input() keyCapabilitiesData!:any

 ngOnInit() {
    console.log("keyCapabilitiesData::", this.keyCapabilitiesData);
    console.log('image URL::', this.keyCapabilitiesData?.keyCapabilityConfigurationImage);
  }
}
