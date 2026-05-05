// import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
// import { provideRouter } from '@angular/router';
// import { provideHttpClient } from '@angular/common/http';

// import { routes } from './app.routes';

// export const appConfig: ApplicationConfig = {
//   providers: [
//     provideBrowserGlobalErrorListeners(),
//     provideRouter(routes),
//     provideHttpClient()
//   ]
// };


import { ApplicationConfig, provideBrowserGlobalErrorListeners, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';

import { routes } from './app.routes';
import {
  MsalModule,
  MsalService,
  MsalGuard,
  MsalInterceptor
} from '@azure/msal-angular';
import { msalGuardConfig, msalInstance, msalInterceptorConfig } from './core/auth/auth-config';



export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(),

    importProvidersFrom(
      MsalModule.forRoot(
        msalInstance,
        msalGuardConfig,
        msalInterceptorConfig
      )
    ),

    MsalService,
    MsalGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: MsalInterceptor,
      multi: true
    }
  ]
};
