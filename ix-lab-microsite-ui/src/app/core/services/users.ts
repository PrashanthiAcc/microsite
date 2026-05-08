import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface User {
  userId: number;
  userEid: string;
  name: string;
  role: string;
  accessStartDate: string;
  approvedBy: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {


  constructor(private http: HttpClient) { }


  getAllUsers() {
    return this.http.get(`${environment.apiUrl}/api/users/v1/all`);
  }

  addUser(addUser: any) {
    return this.http.post(`${environment.apiUrl}/api/users/v1/create`, addUser, { responseType: 'text' });
  }

  acceptUser(userEid: string, payload: any) {
    return this.http.put(`${environment.apiUrl}/api/users/v1/${userEid}/accept`, payload);
  }


  deleteUser(userId: string) {
    return this.http.delete(`${environment.apiUrl}/api/users/v1/${userId}`, { responseType: 'text' });
  }
  updateUser(userEid: string, payload: any) {
    return this.http.put(`${environment.apiUrl}/api/users/v1/${userEid}`, payload, { responseType: 'text' });
  }

  resetPassword(userEid: string, payload: { role: string; updaterEid: string; userPassword: string }) {
    return this.http.put(`${environment.apiUrl}/api/users/v1/${userEid}`, payload, { responseType: 'text' });
  }

  checkPassword(userEid: string, userPassword: string) {
  return this.http.get<{ passwordMatch: boolean }>(
    '/api/users/v1/check-password',
    { params: { userEid, userPassword } }
  );
}
}