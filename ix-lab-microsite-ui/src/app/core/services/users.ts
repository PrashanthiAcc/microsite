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


  constructor(private http: HttpClient) {}


getAllUsers() {
  return this.http.get(`${environment.apiUrl}/api/users/v1/all`);
}

addUser(addUser: any) {
  return this.http.post(`${environment.apiUrl}/api/users/v1/create`, addUser);
}

acceptUser(userEid: string, payload: any) {
  return this.http.put(`${environment.apiUrl}/api/users/v1/${userEid}/accept`, payload);
}

deleteUser(userId: string, payload: any) {
  return this.http.delete(`${environment.apiUrl}/api/users/v1/${userId}`, payload);
}

updateUser(userEid: string, payload: any) {
  return this.http.put(`${environment.apiUrl}/api/users/v1/${userEid}`, payload, {responseType: 'text'});
}


}