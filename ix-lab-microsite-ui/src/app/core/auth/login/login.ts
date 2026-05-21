import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/users';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  lockPath = 'assets/icons/lock.png';
  login_background = 'assets/login_background.png';

  enterpriseId = '';
  password = '';
  enterpriseValid = false;
  error = '';
  buttonLabel = 'Access';
  showRequestAccess = false;

  // Dummy user list
  users = [
    { enterpriseId: 'gudluru.yashwanth@accenture.com', password: 'test123' },
    { enterpriseId: 'mainak@accenture.com', password: 'secure456' }
  ];
  showForgotPassword = false;
  newPassword = '';
  confirmPassword = '';
  resetError = '';
  showRequestAccessModal = false;
  requestName = '';
  requestEnterpriseId = '';
  requestPassword = '';
  requestReason = '';
  allUsers: any[] = [];

  constructor(private router: Router, private http: HttpClient,
    private cdr: ChangeDetectorRef, private route: ActivatedRoute,
    private userService: UserService
  ) { }

  ngOnInit() {
    this.getAllUsers();
  }

  getAllUsers() {
    this.userService.getAllUsers().subscribe((res: any) => {
      this.allUsers = res;


    });
  }

  checkEnterpriseId() {
    // normalize input for comparison
    const enteredId = this.enterpriseId.trim().toLowerCase();

    // find a user whose userEid matches entered ID and is active
    const found = this.allUsers.find(
      u => u.userEid?.toLowerCase() === enteredId && u.isActive === true
    );

    if (found) {
      this.enterpriseValid = true;
      this.error = '';
      this.buttonLabel = 'Login';
      this.showRequestAccess = false;
    } else {
      this.enterpriseValid = false;
      this.error = 'Looks like the Enterprise ID you entered do not have access to the application.';
      this.showRequestAccess = true;
    }
  }



 login() {
  this.getAllUsers();
  this.userService.checkPassword(this.enterpriseId, this.password).subscribe({
    next: (res: { passwordMatch: boolean }) => {
      if (res.passwordMatch) {
        this.error = '';

        // find the logged-in user details from allUsers
        const loggedUser = this.allUsers.find(
          u => u.userEid?.toLowerCase() === this.enterpriseId.trim().toLowerCase()
        );

        // store token + user details in localStorage
        localStorage.setItem('authToken', 'dummy-token-123');
        if (loggedUser) {
          localStorage.setItem('loggedUser', JSON.stringify(loggedUser));
        }

        alert(`Login successful! Welcome ${loggedUser?.name ?? ''}`);
        this.router.navigate(['/home']); // redirect to home/landing
      } else {
        this.error = 'Invalid password. Please try again.';
      }
      this.cdr.detectChanges();
    },
    error: () => {
      this.error = 'Login failed due to a server error.';
      this.cdr.detectChanges();
    }
  });
}


  onSubmit() {
    if (!this.enterpriseValid) {
      this.checkEnterpriseId();
    } else {
      this.login();
    }
  }


  openForgotPassword(event: Event) {
    event.preventDefault();
    this.showForgotPassword = true;
  }

  closeForgotPassword() {
    this.showForgotPassword = false;
    this.newPassword = '';
    this.confirmPassword = '';
    this.resetError = '';
  }

 resetPassword() {
  if (this.newPassword !== this.confirmPassword) {
    this.resetError = 'Passwords do not match';
    return;
  }

  // dynamically resolve role from allUsers
  const matchedUser = this.allUsers.find(
    u => u.userEid?.toLowerCase() === this.enterpriseId.trim().toLowerCase()
  );

  const payload = {
    role: matchedUser?.role ?? 'PRESENTER', // dynamic role
    updaterEid: 'mukunda.ram.bhuyan', // could be current logged-in approver
    userPassword: this.newPassword
  };

  this.userService.resetPassword(this.enterpriseId, payload).subscribe({
    next: () => {
      alert('Password reset successful!');
      this.closeForgotPassword();
      this.cdr.detectChanges();
    },
    error: () => {
      this.resetError = 'Failed to reset password. Please try again.';
    }
  });
}



  openRequestAccess() {
    this.showRequestAccessModal = true;
  }

  closeRequestAccess() {
    this.showRequestAccessModal = false;
    this.requestName = '';
    this.requestEnterpriseId = '';
    this.requestPassword = '';
    this.requestReason = '';
  }

  // submitRequestAccess() {
  //   // For now just show alert, later connect to API
  //   alert(`Access request submitted:\nName: ${this.requestName}\nEnterprise ID: ${this.requestEnterpriseId}\nReason: ${this.requestReason}`);
  //   this.closeRequestAccess();
  // }

  submitRequestAccess() {
  const requestBody = {
    actionType: 'REQUEST_ACCESS',   // different action
    userEid: this.requestEnterpriseId,
    name: this.requestName,
    creatorEId: 'shashi.veeramalla', // or current approver/admin
    userPassword: this.requestPassword,
    reason: this.requestReason,
    
  };

  this.userService.addUser(requestBody).subscribe({
    next: (res) => {
      alert('Access request submitted successfully!');
      this.closeRequestAccess();
      this.error = '';
      this.cdr.detectChanges();
      
    },
    error: (err) => {
      console.error(err);
      alert('Failed to submit access request. Please try again.');
    }
  });
}

}
