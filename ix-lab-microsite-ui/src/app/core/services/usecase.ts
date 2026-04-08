import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsecaseService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  submitForApproval(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/usecase/v1/submit-for-approval-withblob`, payload);
  }

  getAllStories(page: number = 1, size: number = 8): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/usecase/v1/all`, {
      params: { page, size }
    });
  }

  getStoryDetailsById(storyId: string) {
    return this.http.get(`${this.baseUrl}/api/usecase/v1/${storyId}`);
  }

  saveAsDraft(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/usecase/v1/save-draft-withblob`, payload);
  }

  updateStorySendForApproval(storyId: string, payload: any): Observable<string> {
    return this.http.put(
      `${this.baseUrl}/api/usecase/v1/${storyId}/submit-for-approval-withblob`,
      payload,
      { responseType: 'text' }
    );
  }

  updateStorySaveDraft(storyId: string, payload: any): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/api/usecase/v1/${storyId}/save-draft-withblob`,
      payload,
      { responseType: 'text' }
    );
  }

  archiveUsecase(storyId: string): Observable<string> {
    return this.http.patch(
      `${this.baseUrl}/api/usecase/v1/${storyId}/archive`,
      {},
      { responseType: 'text' }
    );
  }

  discardDraft(storyId: string): Observable<string> {
    return this.http.delete(
      `${this.baseUrl}/api/usecase/v1/${storyId}/discard`,
      { responseType: 'text' }
    );
  }

  getFeaturedStories() {
  return this.http.get<any>(`${environment.apiUrl}/api/usecase/v1/all?page=1&size=10`);
}
}