import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class HomePageService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getHomePageData() {
     return this.http.get(`${environment.apiUrl}/api/homepage/v1/home-page-configuration`);
  }

  getApprovedUsecaseCount() {
    return this.http.get(`${environment.apiUrl}/api/usecase/v1/count`);
  }
  
  saveHomePageConfig(payload: FormData) {
    return this.http.post(`${environment.apiUrl}/api/homepage/v1/home-page-configuration`, payload);
  }

  updateHomePageConfig(payload: FormData) {
    return this.http.put(`${environment.apiUrl}/api/homepage/v1/home-page-configuration`, payload);
  }

  getCountByIndustry() {
    return this.http.get(`${environment.apiUrl}/api/usecase/v1/usecases/count-by-industry`);
  }
}
