import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Interest, ProfileDTO } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  getAvailableInterests(): Observable<Interest[]> {
    return this.http.get<Interest[]>(`${this.apiUrl}/interests`);
  }

  getMyProfile(): Observable<ProfileDTO> {
    return this.http.get<ProfileDTO>(`${this.apiUrl}/profiles/me`);
  }

  updateProfile(profile: ProfileDTO): Observable<ProfileDTO> {
    return this.http.put<ProfileDTO>(`${this.apiUrl}/profiles/me`, profile);
  }

  updateUsername(username: string): Observable<ProfileDTO> {
    return this.getMyProfile().pipe(
      switchMap(profile => {
        const updatedProfile: ProfileDTO = { ...profile, username };
        return this.updateProfile(updatedProfile);
      })
    );
  }

  updateAvatar(avatarUrl: string): Observable<ProfileDTO> {
    return this.getMyProfile().pipe(
      switchMap(profile => {
        const updatedProfile: ProfileDTO = { ...profile, avatarUrl };
        return this.updateProfile(updatedProfile);
      })
    );
  }
}
