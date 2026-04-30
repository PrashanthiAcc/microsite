import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AddStoryComponent } from './features/stories/pages/add-story/add-story';
import { MsalService } from '@azure/msal-angular';
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, FormsModule, AddStoryComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {

  constructor(private msalService: MsalService) {}
  protected readonly title = signal('manufacturing-lab');

  login() {
    this.msalService.loginRedirect();
  }

  logout() {
    this.msalService.logoutRedirect();
  }
}
