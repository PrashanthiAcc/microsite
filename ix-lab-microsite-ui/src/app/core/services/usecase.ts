import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UsecaseService {


  constructor(private http: HttpClient) {}


submitForApproval(payload: any): Observable<any> {
  return this.http.post('/api/usecase/v1/submit-for-approval', payload);
}

// getAllStories() {
//   return this.http.get('/api/usecase/v1/all');
// }
getAllStories(page: number = 1, size: number = 10): Observable<any> {
  return this.http.get('/api/usecase/v1/all', {
    params: { page, size }
  });
}



getStoryDetailsById(storyId: string) {
  return this.http.get(`/api/usecase/v1/${storyId}`);
}

saveAsDraft(payload: any): Observable<any> {
  return this.http.post('/api/usecase/v1/save-draft', payload);
}

updateStorySendForApproval(storyId: string, payload: any): Observable<string> {
  return this.http.put(
    `/api/usecase/v1/${storyId}/submit-for-approval`,
    payload,
    { responseType: 'text' }
  );
}


updateStorySaveDraft(storyId: string, payload: any): Observable<any> {
  return this.http.put(`/api/usecase/v1/${storyId}/save-draft`, payload,{ responseType: 'text' });
}

archiveUsecase(storyId: string): Observable<string> {
  return this.http.patch(
    `/api/usecase/v1/${storyId}/archive`,
    {}, // empty body
    { responseType: 'text' }
  );
}


discardDraft(storyId: string): Observable<string> {
  return this.http.delete(
    `/api/usecase/v1/${storyId}/discard`,
    { responseType: 'text' }
  );
}


// getAllUsers() {
//   return this.http.get('/api/users/v1/all');
// }
}