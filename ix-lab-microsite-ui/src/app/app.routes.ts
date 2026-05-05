import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout';

import { MsalGuard } from '@azure/msal-angular';
// export const routes: Routes = [
//   {
//     path: '',
//     //canActivate: [MsalGuard],
//     component: LayoutComponent,
//     data: { role: 'admin' },
//     children: [
//       {
//         path: '',
//         loadComponent: () =>
//           import('./features/home/pages/home/home')
//             .then(m => m.HomeComponent)
//       },
//       {
//         path: 'stories',
//         loadComponent: () =>
//           import('./features/stories/pages/stories/stories')
//             .then(m => m.StoriesComponent)
//       },
//       {
//         path: 'admin/configuration',
//         loadComponent: () =>
//           import('./features/admin/configuration/configuration')
//             .then(m => m.ConfigurationComponent)
//       },
//       {
//         path: 'add-story',
//         loadComponent: () =>
//           import('./features/stories/pages/add-story/add-story')
//             .then(m => m.AddStoryComponent)
//       },
//       {
//         path: 'stories/:id',
//         loadComponent: () =>
//           import('./features/stories/pages/story-details/story-details')
//             .then(m => m.StoryDetailsComponent)
//       },
//       {
//         path: 'stories/:id/edit',
//         loadComponent: () =>
//           import('./features/stories/pages/edit-story/edit-story')
//             .then(m => m.EditStoryComponent)
//       },
//       {
//         path: 'archived',
//         loadComponent: () =>
//           import('./features/stories/pages/archived-story/archived-story')
//             .then(m => m.ArchivedStoriesComponent)
//       },
//       {
//         path: 'user-management',
//         loadComponent: () =>
//           import('./users/user-management/user-management')
//             .then(m => m.UserManagementComponent)
//       },
//       {
//         path: 'homepage-configuration',
//         loadComponent: () =>
//           import('./features/home/pages/homepage-configurations/homepage-configurations')
//             .then(m => m.HomepageConfigurationsComponent)
//       }

//     ]
//   },
//   {
//     path: 'presenter',
//     //canActivate: [MsalGuard],
//     component: LayoutComponent,
//     data: { role: 'presenter' },
//     children: [
//       {
//         path: '',
//         loadComponent: () =>
//           import('./features/home/pages/home/home')
//             .then(m => m.HomeComponent)
//       },
//       {
//         path: 'stories',
//         loadComponent: () =>
//           import('./features/stories/pages/stories/stories')
//             .then(m => m.StoriesComponent)
//       },
//       {
//         path: 'admin/configuration',
//         loadComponent: () =>
//           import('./features/admin/configuration/configuration')
//             .then(m => m.ConfigurationComponent)
//       },
//       {
//         path: 'add-story',
//         loadComponent: () =>
//           import('./features/stories/pages/add-story/add-story')
//             .then(m => m.AddStoryComponent)
//       },
//       {
//         path: 'stories/:id',
//         loadComponent: () =>
//           import('./features/stories/pages/story-details/story-details')
//             .then(m => m.StoryDetailsComponent)
//       },
//       {
//         path: 'stories/:id/edit',
//         loadComponent: () =>
//           import('./features/stories/pages/edit-story/edit-story')
//             .then(m => m.EditStoryComponent)
//       },
//       {
//         path: 'archived',
//         loadComponent: () =>
//           import('./features/stories/pages/archived-story/archived-story')
//             .then(m => m.ArchivedStoriesComponent)
//       },
//       {
//         path: 'user-management',
//         loadComponent: () =>
//           import('./users/user-management/user-management')
//             .then(m => m.UserManagementComponent)
//       },
//       {
//         path: 'homepage-configuation',
//         loadComponent: () =>
//           import('./features/home/pages/homepage-configurations/homepage-configurations')
//             .then(m => m.HomepageConfigurationsComponent)
//       }
//     ]
//   },
//   //  {
//   //   path: 'user-management',
//   //   component: UserManagement,
//   //  },
//   {
//     path: '**',
//     redirectTo: ''
//   }
// ];


const sharedChildren = [
  {
    path: '',
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
  }
];

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    data: { role: 'admin' },
    children: sharedChildren
  },
  {
    path: 'presenter',
    component: LayoutComponent,
    data: { role: 'presenter' },
    children: sharedChildren
  },
  {
    path: 'superadmin',
    component: LayoutComponent,
    data: { role: 'superadmin' },
    children: [
      ...sharedChildren,
      {
        path: 'request',
        loadComponent: () =>
          import('./features/super-admin/requests/requests')
            .then(m => m.RequestsComponent)
      }
    ]
  },
  {
    path: 'request', // 👈 if you want root-level /request
    component: LayoutComponent,
    data: { role: 'superadmin' },
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/super-admin/requests/requests')
            .then(m => m.RequestsComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];


