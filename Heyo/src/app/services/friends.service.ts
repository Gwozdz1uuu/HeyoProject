import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { User } from '../models';

@Injectable({
  providedIn: 'root'
})
export class FriendsService {
  private readonly apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getFriends(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/friends`);
  }

  sendFriendRequest(friendId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/friends/${friendId}`, {});
  }

  acceptFriendRequest(notificationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/friend-requests/${notificationId}/accept`, {});
  }

  declineFriendRequest(notificationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/friend-requests/${notificationId}/decline`, {});
  }

  removeFriend(friendId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/friends/${friendId}`);
  }

  searchUsers(query: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/search`, {
      params: { query }
    });
  }

  getUserProfile(userId: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${userId}`);
  }
}
