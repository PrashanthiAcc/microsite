import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

// app-state.service.ts
@Injectable({ providedIn: 'root' })
export class AppStateService {
  private appNameSubject = new BehaviorSubject<string>('');
  appName$ = this.appNameSubject.asObservable();

  setApplicationName(name: string) {
    this.appNameSubject.next(name);
  }
}
