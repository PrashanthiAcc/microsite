import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { QuillModule } from 'ngx-quill';
import { UserService } from '../../core/services/users';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, QuillModule, RouterLink],
  providers: [DatePipe],
  templateUrl: './user-management.html',
  styleUrls: ['./user-management.scss'],
})
export class UserManagementComponent implements OnInit {

  activeTab: 'allUsers' | 'newRequests' = 'allUsers';
  isModalOpen: boolean = false;
  showModal: boolean = false;
  userForm: FormGroup | any;
  users: any[] = [];
  requests: any[] = [];
  message = '';
  searchText: string = '';
  filteredUsers: any[] = [];
  isEditMode = false;
  selectedUser: any = null;
  userModel = {
    userEid: '',
    role: '',
    name: '',
    creatorEId: '',
  };

  constructor(private userService: UserService,
    private fb: FormBuilder, private cdr: ChangeDetectorRef, private datePipe: DatePipe
  ) {
    this.userForm = this.fb.group(
      {
        userEid: [''],
        name: [''],
        role: [''],
        reason: [''],
      });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  switchTab(tab: 'allUsers' | 'newRequests'): void {
    this.activeTab = tab;
    this.loadUsers();
  }

  configureForm() {
    if (this.activeTab === 'allUsers') {
      this.userForm.get('userEid')?.setValidators([Validators.required]);
      this.userForm.get('name')?.setValidators([Validators.required]);
      this.userForm.get('role')?.setValidators([Validators.required]);
    }

    if (this.activeTab === 'newRequests') {

      this.userForm.get('UserEid')?.clearValidators();
      this.userForm.get('name')?.clearValidators();
      this.userForm.get('role')?.clearValidators();
      this.userForm.get('reason')?.setValidators([Validators.required]);
    }

    this.userForm.updateValueAndValidity();
  }

  openModal(): void {
    this.isEditMode = false;
    this.isModalOpen = true;
    this.showModal = true;
    this.userForm.reset();
    this.configureForm();
  }

  closeModal(): void {
    this.showModal = false;
    this.isModalOpen = false;
    this.userForm.reset();
  }

  loadUsers() {
    this.searchText = '';
    this.userService.getAllUsers().subscribe({
      next: (res: any) => {
        this.users = res;
        this.filteredUsers = [...this.users];
        this.requests = this.users.filter(user => user.isActive === false && user.accessStartDate == null);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error:', err);
      }
    });
  }

  saveUser() {
    if (this.userForm.valid && this.activeTab === 'allUsers' && !this.isEditMode) {
      this.createUser();
    } else if (this.userForm.valid && this.activeTab === 'allUsers' && this.isEditMode) {
      this.updateUser();
    } else if (this.userForm.valid && this.activeTab === 'newRequests') {
      const requestBody = {
        actionType: 'REQUEST_ACCESS',
        userEid: this.userForm.value.userEid,
        name: this.userForm.value.name,
        reason: this.userForm.value.reason
      };
      this.userService.addUser(requestBody).subscribe({
        next: (res) => {
          this.showModal = false;
          this.loadUsers();
          this.closeModal();
        },
      });
      this.showModal = false;
    } else {
      this.userForm.markAllAsTouched();
    }
  }

  createUser() {
    const requestBody = {
      actionType: 'ADD_USER',
      userEid: this.userForm.value.userEid,
      role: this.userForm.value.role?.toUpperCase(),
      name: this.userForm.value.name,
      creatorEId: 'shashi.veeramalla'
    };
    this.userService.addUser(requestBody).subscribe({
      next: (res) => {
        this.message = 'User Created successfully';
        this.loadUsers();
        this.closeModal();
        setTimeout(() => {
          this.message = '';
        }, 2000);
      },
    });
    this.showModal = false;
  }

  updateUser() {
    const selectedRole = this.userForm.get('role')?.value;
    const userEid = this.selectedUser?.userEid;
    const payload = {
      role: this.mapRole(selectedRole),
      updaterEid: 'mukunda.ram.bhuyan'
    };

    this.userService.updateUser(userEid, payload).subscribe({
      next: () => {
        this.message = 'User Updated successfully';
        this.loadUsers(); // refresh table
        this.closeModal();
        setTimeout(() => {
          this.message = '';
        }, 2000);
      }
    });
  }

  mapRole(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'Admin';
      case 'SUPERADMIN':
        return 'Super Admin';
      case 'PRESENTER':
        return 'Presenter';
      default:
        return role;
    }
  }

  acceptUser(user: any) {
    const payload = {
      role: user.role?.toUpperCase(),     // ensure uppercase
      approverEid: 'shashi.veeramalla'    // or logged-in user
    };

    const userEid = user.userEid;

    this.userService.acceptUser(userEid, payload).subscribe({
      next: (res) => {
        console.log('User accepted', res);

        // refresh table
        this.loadUsers();   // 🔥 reload data

      },
      error: (err) => {
        console.error('Error:', err);
      }
    });
  }

  deleteUser(user: any) {
    const userId = user.userId;
    this.userService.deleteUser(userId).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== userId);
        this.message = 'User Removed successfully.'
        this.loadUsers();
        setTimeout(() => {
          this.message = ''
        }, 2000);
      },
    });
  }

  openAddUser() {
    this.isEditMode = false;
    this.showModal = true;
    this.userForm.get('userEid')?.enable();
    this.userForm.get('name')?.enable();
    this.userForm.reset();
  }

  openEditUser(user: any) {
    this.isEditMode = true;
    this.showModal = true;
    this.selectedUser = user;

    this.userForm.patchValue({
      userEid: user.userEid,
      name: user.name,
      role: user.role
    });

    this.userForm.get('userEid')?.disable();
    this.userForm.get('name')?.disable();
  }

  onSearch() {
    const search = this.searchText.toLowerCase();

    this.filteredUsers = this.users.filter(user => {

      const formattedDate = this.datePipe.transform(
        user.accessStartDate,
        'dd MMM yyyy'
      )?.toLowerCase() || '';

      return (
        user.name?.toLowerCase().includes(search) ||
        user.userEid?.toLowerCase().includes(search) ||
        user.role?.toLowerCase().includes(search) ||
        user.approverEid?.toLowerCase().includes(search) ||
        formattedDate.includes(search)
      );
    });
  }

}
