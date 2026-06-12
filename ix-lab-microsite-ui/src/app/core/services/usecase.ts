import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsecaseService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  submitForApproval(payload: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/usecase/v1/submit-for-approval-withblob`, payload);
  }

  getAllStories(page: number = 1, size: number = 10): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/usecase/v1/all`, {
      params: { page, size }
    });
  }

  getStoryDetailsById(storyId: string) {
    return this.http.get(`${this.baseUrl}/api/usecase/v1/${storyId}`);
  }

  saveAsDraft(payload: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/usecase/v1/save-draft-withblob`, payload);
  }

  updateStorySendForApproval(storyId: string, payload: FormData): Observable<string> {
    return this.http.put(
      `${this.baseUrl}/api/usecase/v1/${storyId}/submit-for-approval-withblob`,
      payload,
      { responseType: 'text' }
    );
  }

  updateStorySaveDraft(storyId: string, payload: FormData): Observable<any> {
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

  updateFaqs(usecaseId: number, faqs: any[]): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/api/usecase/v1/${usecaseId}/faqs`,
      faqs,
      { headers: { 'Content-Type': 'application/json' } }
    );
  }

  approveUsecase(usecaseId: number, approverId: number): Observable<string> {
    return this.http.put(
      `${this.baseUrl}/api/usecase/v1/${usecaseId}/approve`,
      {}, // empty body
      {
        params: { approverId: approverId },
        responseType: 'text'
      }
    );
  }

  sendToDraftUsecase(usecaseId: number): Observable<string> {
    return this.http.put(
      `${this.baseUrl}/api/usecase/v1/${usecaseId}/sendBackToDraft`,
      {}, // empty body
      {
        responseType: 'text'
      }
    );
  }


  favouriteUsecase(userId: number, usecaseId: string): Observable<{ data: any; message: string }> {
    return this.http.post<{ data: any; message: string }>(
      `${this.baseUrl}/api/usecase/v1/favourites`,
      {}, // empty body
      {
        params: { userId, usecaseId }
      }
    );
  }

  getFavouriteUsecases(
    userId: number,
    page: number = 1,
    size: number = 10
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/usecase/v1/favourites`, {
      params: { userId, page, size }
    });
  }

  deleteFavouriteUsecase(userId: number, usecaseId: string): Observable<string> {
  return this.http.delete(`${this.baseUrl}/api/usecase/v1/favourites`, {
    params: { userId, usecaseId },
    responseType: 'text'
  });
}


  getUsecasesByStatus(
    status: string,
    page: number = 1,
    size: number = 12
  ): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/usecase/v1/usecases`, {
      params: { status, page, size }
    });
  }

   /** Restore a previously archived usecase */
  restoreArchivedUsecase(storyId: string): Observable<string> {
    return this.http.patch(
      `${this.baseUrl}/api/usecase/v1/${storyId}/restore`,
      {}, // empty body
      { responseType: 'text' }
    );
  }
}