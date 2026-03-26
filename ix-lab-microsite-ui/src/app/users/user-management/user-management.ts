import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { QuillModule } from 'ngx-quill';

@Component({
  selector: 'app-user-management',
  standalone: true, 
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule],
  templateUrl: './user-management.html',
  styleUrls: ['./user-management.scss'],
})
export class UserManagementComponent {
 activeTab: string = 'all-users';
 isModalOpen: boolean = false;
 showModal: boolean = false;

  switchTab(tab: string): void {
    this.activeTab = tab;
  }

  isActive(tab: string): boolean {
    return this.activeTab === tab;
  }

  openModal(): void {
    this.isModalOpen = true;
    this.showModal = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.showModal = false;
  }

}
