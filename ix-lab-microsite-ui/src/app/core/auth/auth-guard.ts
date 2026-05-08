import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const userStr = localStorage.getItem('loggedUser');

  // If no user in storage → force login
  if (!userStr) {
    return router.parseUrl('/login');
  }

  const user = JSON.parse(userStr);
  const role = user.role?.toLowerCase();
  const attemptedUrl = state.url.split('?')[0];

  // SUPERADMIN → full access
  if (role === 'superadmin') {
    return true;
  }

  // ADMIN → block superadmin-only routes
  if (role === 'admin') {
    const restrictedForAdmin = ['/request', '/view-request'];
    if (restrictedForAdmin.some(r => attemptedUrl.includes(r))) {
      return router.parseUrl('/home');
    }
    return true;
  }

  // PRESENTER → block admin + superadmin routes
  if (role === 'presenter') {
    const restrictedForPresenter = [
      '/add-story',
      '/stories',                // covers /stories, /stories/:id, /stories/:id/edit
      '/archived',
      '/user-management',
      '/homepage-configuration',
      '/request',
      '/view-request',
      '/admin/configuration'
    ];
    if (restrictedForPresenter.some(r => attemptedUrl.includes(r))) {
      return router.parseUrl('/home');
    }
    return true;
  }

  // Unknown role → force login
  return router.parseUrl('/login');
};
