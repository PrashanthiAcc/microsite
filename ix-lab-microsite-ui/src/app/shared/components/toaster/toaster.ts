import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toaster',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toaster.html',
  styleUrls: ['./toaster.scss'],
})
export class ToasterComponent {
  @Input() title: string = '';
  @Input() message: string = '';
  @Input() show: boolean = false;

  closeToaster() {
    this.show = false;
  }
}
