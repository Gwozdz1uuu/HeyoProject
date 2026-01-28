import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UploadResponse {
  url: string;
  filename: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private readonly apiUrl = `${environment.apiUrl}/upload`;

  constructor(private http: HttpClient) {}

  uploadAvatar(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<UploadResponse>(`${this.apiUrl}/avatar`, formData);
  }

  uploadEventImage(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<UploadResponse>(`${this.apiUrl}/event-image`, formData);
  }
}
