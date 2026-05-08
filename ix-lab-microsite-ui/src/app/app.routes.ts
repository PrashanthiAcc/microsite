import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout';
import { authGuard } from './core/auth/auth-guard';

export const routes: Routes = [
  {
  path: '',
  redirectTo: 'login',
  pathMatch: 'full'
},
  {
    path: 'login',
    loadComponent: () =>
      import('./core/auth/login/login')
        .then(m => m.LoginComponent)
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'home',
        loadComponent: () =>
          import('./features/home/pages/home/home')
            .then(m => m.HomeComponent)
      },
      {
        path: 'stories',
        loadComponent: () =>
          import('./features/stories/pages/stories/stories')
            .then(m => m.StoriesComponent)
      },
      {
        path: 'admin/configuration',
        loadComponent: () =>
          import('./features/admin/configuration/configuration')
            .then(m => m.ConfigurationComponent)
      },
      {
        path: 'add-story',
        loadComponent: () =>
          import('./features/stories/pages/add-story/add-story')
            .then(m => m.AddStoryComponent)
      },
      {
        path: 'stories/:id',
        loadComponent: () =>
          import('./features/stories/pages/story-details/story-details')
            .then(m => m.StoryDetailsComponent)
      },
      {
        path: 'stories/:id/edit',
        loadComponent: () =>
          import('./features/stories/pages/edit-story/edit-story')
            .then(m => m.EditStoryComponent)
      },
      {
        path: 'archived',
        loadComponent: () =>
          import('./features/stories/pages/archived-story/archived-story')
            .then(m => m.ArchivedStoriesComponent)
      },
      {
        path: 'favourites',
        loadComponent: () =>
          import('./features/stories/pages/favourites-story/favourites-story')
            .then(m => m.FavouritesStoryComponent)
      },
      {
        path: 'user-management',
        loadComponent: () =>
          import('./users/user-management/user-management')
            .then(m => m.UserManagementComponent)
      },
      {
        path: 'homepage-configuration',
        loadComponent: () =>
          import('./features/home/pages/homepage-configurations/homepage-configurations')
            .then(m => m.HomepageConfigurationsComponent)
      },
      {
        path: 'request',
        loadComponent: () =>
          import('./features/super-admin/requests/requests')
            .then(m => m.RequestsComponent)
      },
      {
        path: 'view-request/:id/edit',
        loadComponent: () =>
          import('./features/super-admin/view-requests/view-requests')
            .then(m => m.ViewRequestsComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
