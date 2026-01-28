import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { User, AuthRequest, RegisterRequest, AuthResponse, ProfileCreateRequest, ProfileDTO } from '../models';
import { WebSocketService } from './websocket.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  
  private currentUserSignal = signal<User | null>(this.getUserFromStorage());
  
  public currentUser = this.currentUserSignal.asReadonly();
  public isAuthenticated = computed(() => this.currentUserSignal() !== null);

  private wsService = inject(WebSocketService);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Auto-connect WebSocket if user is already logged in
    const token = this.getToken();
    if (token && this.currentUser()) {
      this.initializeWebSocket(token);
    }
  }

  login(username: string, password: string): Observable<AuthResponse> {
    const request: AuthRequest = { username, password };
    
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        this.setSession(response);
        if (response.profileCompleted) {
          this.initializeWebSocket(response.token);
        }
      })
    );
  }

  register(email: string, password: string): Observable<AuthResponse> {
    const request: RegisterRequest = { email, password };
    
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => {
        this.setSession(response);
        // Don't initialize WebSocket if profile is not completed
        if (response.profileCompleted) {
          this.initializeWebSocket(response.token);
        }
      })
    );
  }

  completeProfile(profileData: ProfileCreateRequest): Observable<ProfileDTO> {
    return this.http.post<ProfileDTO>(`${environment.apiUrl}/profiles/complete`, profileData).pipe(
      tap(profile => {
        // If backend returned new token (because username was changed to nickname),
        // update stored token and current user info.
        if (profile.newToken) {
          localStorage.setItem(this.TOKEN_KEY, profile.newToken);

          const userJson = localStorage.getItem(this.USER_KEY);
          if (userJson) {
            try {
              const user: User = JSON.parse(userJson);
              user.username = profile.username;
              localStorage.setItem(this.USER_KEY, JSON.stringify(user));
              this.currentUserSignal.set(user);
            } catch {
              // ignore parse errors, user will be refreshed on next login
            }
          }

          this.initializeWebSocket(profile.newToken);
        } else {
          const token = this.getToken();
          if (token) {
            this.initializeWebSocket(token);
          }
        }
      })
    );
  }

  logout(): void {
    // Set offline status and disconnect WebSocket
    this.wsService.setOnlineStatus(false);
    this.wsService.disconnect().then(() => {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      this.currentUserSignal.set(null);
      this.router.navigate(['/login']);
    });
  }

  private setSession(response: AuthResponse): void {
    const user: User = {
      id: response.userId,
      username: response.username,
      email: response.email,
      avatarUrl: response.avatarUrl,
      online: true
    };
    
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private async initializeWebSocket(token: string): Promise<void> {
    try {
      console.log('[AuthService] Initializing WebSocket connection...');
      await this.wsService.connect(token);
      console.log('[AuthService] ✓ WebSocket connected successfully');
      
      // Wait a bit for subscriptions to be established
      await new Promise(resolve => setTimeout(resolve, 500));
      
      // Set online status
      this.wsService.setOnlineStatus(true);
      console.log('[AuthService] ✓ Online status set');
    } catch (error) {
      console.error('[AuthService] ✗ WebSocket connection failed:', error);
      // Don't block login on WebSocket failure
    }
  }

  private getUserFromStorage(): User | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userJson = localStorage.getItem(this.USER_KEY);
    
    if (token && userJson) {
      try {
        return JSON.parse(userJson);
      } catch {
        return null;
      }
    }
    return null;
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUserId(): number | null {
    const user = this.currentUser();
    return user ? user.id : null;
  }
}
