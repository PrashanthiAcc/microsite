// export const environment = {
//   production: false,
//   apiUrl: 'https://api-ix-mfg-azure-dev.azurewebsites.net'
// }


export const environment = {
  production: false,
 
  // ✅ Your backend API
  apiUrl: 'https://api-ix-mfg-azure-dev.azurewebsites.net',

  // ✅ Azure B2C tenant info
  tenantName: 'ixtsdev', // 🔥 ADD THIS
  tenantDomain: 'ixtsdev.onmicrosoft.com', // 🔥 ADD THIS
  policyName: 'B2C_1_ixtsdev_signin', // 🔥 ADD THIS

  // ✅ Existing values (keep)
  tenantId: '278fe491-43f3-4c0a-a003-2bdc73de9f97',
  clientId: '2ffa26ea-7971-4b7f-90e7-7bc31ebc6945',

  // ✅ Redirect
  redirectUri: 'http://localhost:4200',

  // ✅ API scope (already correct)
  apiScope: 'api://2ffa26ea-7971-4b7f-90e7-7bc31ebc6945/access_as_user'
};
