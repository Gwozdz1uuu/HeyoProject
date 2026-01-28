import { Component, OnInit, signal, Inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../services/profile.service';
import { PostService } from '../services/post.service';
import { EventService } from '../services/event.service';
import { UploadService } from '../services/upload.service';
import { AuthService } from '../services/auth.service';
import { WebSocketService } from '../services/websocket.service';
import { ProfileDTO, Post, Event as EventModel, Page } from '../models';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  profile = signal<ProfileDTO | null>(null);
  posts = signal<Post[]>([]);
  events = signal<EventModel[]>([]);
  isLoading = signal(true);
  isUploadingAvatar = signal(false);
  hoveredPostId = signal<number | null>(null);
  isOwnProfile = signal(true);
  private viewedUserId: number | null = null;

  constructor(
    private profileService: ProfileService,
    private postService: PostService,
    private eventService: EventService,
    private uploadService: UploadService,
    private authService: AuthService,
    private wsService: WebSocketService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      const currentUserId = this.authService.getCurrentUserId();

      if (idParam) {
        this.viewedUserId = Number(idParam);
        this.isOwnProfile.set(currentUserId !== null && this.viewedUserId === currentUserId);
      } else {
        this.viewedUserId = currentUserId;
        this.isOwnProfile.set(true);
      }

      if (this.viewedUserId && !this.isOwnProfile()) {
        this.loadProfileByUserId(this.viewedUserId);
        this.loadPostsForUser(this.viewedUserId);
        // Na razie sekcja wydarzeń pokazuje tylko własne wydarzenia użytkownika zalogowanego
        this.events.set([]);
      } else {
        this.loadMyProfile();
        if (currentUserId) {
          this.loadPostsForUser(currentUserId);
        }
        this.loadEvents();
      }
    });
  }

  private loadMyProfile(): void {
    this.profileService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.isLoading.set(false);
        this.snackBar.open('Błąd podczas ładowania profilu', 'Zamknij', {
          duration: 3000
        });
      }
    });
  }

  private loadProfileByUserId(userId: number): void {
    this.isLoading.set(true);
    this.profileService.getProfileByUserId(userId).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading profile by user id:', error);
        this.isLoading.set(false);
        this.snackBar.open('Błąd podczas ładowania profilu użytkownika', 'Zamknij', {
          duration: 3000
        });
      }
    });
  }

  private loadPostsForUser(userId: number): void {
    this.postService.getUserPosts(userId, 0, 9).subscribe({
      next: (response: Page<Post>) => {
        this.posts.set(response.content);
      },
      error: (error) => {
        console.error('Error loading posts:', error);
      }
    });
  }

  loadEvents(): void {
    this.eventService.getMyEvents(0, 10).subscribe({
      next: (response: Page<EventModel>) => {
        this.events.set(response.content);
      },
      error: (error) => {
        console.error('Error loading events:', error);
      }
    });
  }

  async onFileSelected(fileEvent: Event): Promise<void> {
    const input = fileEvent.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validate file type
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
      if (!validTypes.includes(file.type)) {
        this.snackBar.open('Nieprawidłowy format pliku. Dozwolone: JPG, PNG, WebP', 'Zamknij', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        return;
      }

      // Validate file size (5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('Plik jest za duży. Maksymalny rozmiar: 5MB', 'Zamknij', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        return;
      }

      this.isUploadingAvatar.set(true);

      try {
        const uploadResponse = await firstValueFrom(this.uploadService.uploadAvatar(file));
        // Construct full URL if relative path is returned
        const avatarUrl = uploadResponse.url.startsWith('http') 
          ? uploadResponse.url 
          : `${environment.apiUrl.replace('/api', '')}${uploadResponse.url}`;
        
        const updatedProfile = await firstValueFrom(
          this.profileService.updateAvatar(avatarUrl)
        );
        this.profile.set(updatedProfile);
        this.cdr.detectChanges(); // Force change detection to refresh avatar display
        this.snackBar.open('Zdjęcie profilowe zostało zaktualizowane', 'Zamknij', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      } catch (error: any) {
        console.error('Error uploading avatar:', error);
        this.snackBar.open('Błąd podczas przesyłania zdjęcia', 'Zamknij', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      } finally {
        this.isUploadingAvatar.set(false);
      }
    }
  }

  openEditUsernameDialog(): void {
    const currentUsername = this.profile()?.username || '';
    const dialogRef = this.dialog.open(EditUsernameDialogComponent, {
      width: '400px',
      data: { username: currentUsername }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined && result !== null && result !== '') {
        this.updateUsername(result);
      }
    });
  }

  updateUsername(username: string): void {
    this.profileService.updateUsername(username).subscribe({
      next: async (updatedProfile: ProfileDTO) => {
        this.profile.set(updatedProfile);
        
        // Update token if username was changed (new token provided)
        if (updatedProfile.newToken) {
          localStorage.setItem('auth_token', updatedProfile.newToken);
          
          // Reconnect WebSocket with new token (force reconnect)
          try {
            await this.wsService.disconnect();
            // Small delay to ensure disconnect is complete
            await new Promise(resolve => setTimeout(resolve, 100));
            await this.wsService.connect(updatedProfile.newToken, true);
            this.wsService.setOnlineStatus(true);
            console.log('[Profile] WebSocket reconnected with new token');
          } catch (error) {
            console.error('Error reconnecting WebSocket:', error);
          }
        }
        
        // Update localStorage with new username
        const userJson = localStorage.getItem('auth_user');
        if (userJson) {
          try {
            const user = JSON.parse(userJson);
            user.username = updatedProfile.username;
            localStorage.setItem('auth_user', JSON.stringify(user));
            // Update AuthService signal
            this.authService['currentUserSignal'].set(user);
          } catch (e) {
            console.error('Error updating user in localStorage:', e);
          }
        }
        
        this.snackBar.open('Username został zaktualizowany', 'Zamknij', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      },
      error: (error: any) => {
        console.error('Error updating username:', error);
        let errorMessage = 'Błąd podczas aktualizacji username';
        if (error.error?.message) {
          errorMessage = error.error.message;
        }
        this.snackBar.open(errorMessage, 'Zamknij', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  deletePost(postId: number): void {
    if (!confirm('Czy na pewno chcesz usunąć ten post?')) {
      return;
    }

    this.postService.deletePost(postId).subscribe({
      next: () => {
        this.posts.update(posts => posts.filter(p => p.id !== postId));
        this.snackBar.open('Post został usunięty', 'Zamknij', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      },
      error: (error) => {
        console.error('Error deleting post:', error);
        this.snackBar.open('Błąd podczas usuwania posta', 'Zamknij', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  setHoveredPost(postId: number | null): void {
    this.hoveredPostId.set(postId);
  }

  getDefaultAvatar(username: string): string {
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
  }

  formatEventDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', {
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getEventType(event: EventModel): string {
    const userId = this.authService.getCurrentUserId();
    if (!userId) return '';
    
    if (event.creatorId === userId) {
      return 'Utworzone przez Ciebie';
    } else if (event.isParticipating) {
      return 'Uczestniczysz';
    } else if (event.isInterested) {
      return 'Zainteresowany';
    }
    return '';
  }

  getPostTextPreview(content: string | undefined): string {
    if (!content) return '';
    return content.length > 100 ? content.substring(0, 100) + '...' : content;
  }
}

// Dialog component for editing username
@Component({
  selector: 'app-edit-username-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    FormsModule
  ],
  template: `
    <h2 mat-dialog-title>Edytuj username</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Username</mat-label>
        <input matInput [(ngModel)]="username" maxlength="50" minlength="3" 
               pattern="[a-zA-Z0-9_]+" #usernameInput="ngModel" />
        <mat-hint>3-50 znaków, tylko litery, cyfry i podkreślenia</mat-hint>
        <mat-error *ngIf="usernameInput.invalid && usernameInput.touched">
          <span *ngIf="usernameInput.errors?.['required']">Username jest wymagany</span>
          <span *ngIf="usernameInput.errors?.['minlength']">Username musi mieć minimum 3 znaki</span>
          <span *ngIf="usernameInput.errors?.['pattern']">Tylko litery, cyfry i podkreślenia</span>
        </mat-error>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Anuluj</button>
      <button mat-button color="primary" (click)="onSave()" 
              [disabled]="!username.trim() || usernameInput.invalid">Zapisz</button>
    </mat-dialog-actions>
  `
})
export class EditUsernameDialogComponent {
  username: string = '';

  constructor(
    public dialogRef: MatDialogRef<EditUsernameDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { username: string }
  ) {
    this.username = data.username || '';
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close(this.username.trim());
  }
}
