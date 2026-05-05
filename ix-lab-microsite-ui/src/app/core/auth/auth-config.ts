import { PublicClientApplication, InteractionType } from '@azure/msal-browser';
import { MsalGuardConfiguration, MsalInterceptorConfiguration } from '@azure/msal-angular';

export const msalInstance = new PublicClientApplication({
  auth: {
    clientId: '2ffa26ea-7971-4b7f-90e7-7bc31ebc6945',
    authority: 'https://ixtsdev.b2clogin.com/ixtsdev.onmicrosoft.com/B2C_1_ixtsdev_signin',
    knownAuthorities: ['ixtsdev.b2clogin.com'],
    redirectUri: 'http://localhost:4200'
  },
  cache: {
    cacheLocation: 'localStorage'
  }
});

export const msalGuardConfig: MsalGuardConfiguration = {
  interactionType: InteractionType.Redirect,
  authRequest: {
    scopes: ['https://ixtsdev.onmicrosoft.com/api://2ffa26ea-7971-4b7f-90e7-7bc31ebc6945/access_as_user']
  }
};

export const msalInterceptorConfig: MsalInterceptorConfiguration = {
  interactionType: InteractionType.Redirect,
  protectedResourceMap: new Map([
    ['https://api-ix-mfg-azure-dev.azurewebsites.net', ['https://ixtsdev.onmicrosoft.com/api://2ffa26ea-7971-4b7f-90e7-7bc31ebc6945/access_as_user']]
  ])
};
