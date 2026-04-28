// export const environment = {
//   production: true,
//   apiUrl: 'https://api-ix-mfg-azure-prod.azurewebsites.net' 
// };

export const environment = {
  production: true, // true in prod
  apiUrl: 'https://api-ix-mfg-azure-dev.azurewebsites.net',
  tenantId: '278fe491-43f3-4c0a-a003-2bdc73de9f97',
  clientId: '2ffa26ea-7971-4b7f-90e7-7bc31ebc6945',
  redirectUri: 'http://localhost:4200/',
  apiScope: 'api://2ffa26ea-7971-4b7f-90e7-7bc31ebc6945/access_as_user'
};