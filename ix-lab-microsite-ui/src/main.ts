import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { msalInstance } from './app/core/auth/auth-config';

async function bootstrap() {
  // ✅ Step 1: Initialize MSAL FIRST
  await msalInstance.initialize();

  // ✅ Step 2: Handle redirect AFTER init
  await msalInstance.handleRedirectPromise().then((result) => {
    if (result?.account) {
    msalInstance.setActiveAccount(result.account);
    }
  });

  // ✅ Step 3: Bootstrap Angular
  await bootstrapApplication(App, appConfig);
}

bootstrap().catch(err => console.error(err));