import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IndustryService {


  constructor(private http: HttpClient) {}

 getIndustries() {
  return this.http.get('/api/industry/v1/all/data');
}

// submitForApproval(payload: any): Observable<any> {
//   return this.http.post('/api/usecase/v1/submit-for-approval', payload);
// }

// getAllStories() {
//   return this.http.get('/api/usecase/v1/all');
// }

// getAllUsers() {
//   return this.http.get('/api/users/v1/all');
// }
}