import { ApplicationConfig, provideBrowserGlobalErrorListeners, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';
import { MsalModule, MsalInterceptor, MsalGuard } from '@azure/msal-angular';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { PublicClientApplication, InteractionType, BrowserCacheLocation } from '@azure/msal-browser';
import { environment } from '../environments/environment';
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(),

    // ✅ Import MSAL NgModule (this makes <msal-redirect> available)
    importProvidersFrom(
      MsalModule.forRoot(
        new PublicClientApplication({
          auth: {
            clientId: environment.clientId,
            authority: `https://login.microsoftonline.com/${environment.tenantId}`,
            redirectUri: environment.redirectUri,
            postLogoutRedirectUri: environment.redirectUri,
          },
          cache: {
            cacheLocation: BrowserCacheLocation.LocalStorage
          }
        }),
        {
          interactionType: InteractionType.Redirect,
          authRequest: { scopes: ['User.Read'] } // ✅ must not be empty
        },
        {
          interactionType: InteractionType.Redirect,
          protectedResourceMap: new Map([
            ['https://graph.microsoft.com/v1.0/me', ['User.Read']] // ✅ test Graph first
          ])
        }

      )
    ),

    {
      provide: HTTP_INTERCEPTORS,
      useClass: MsalInterceptor,
      multi: true
    }
  ]
};
