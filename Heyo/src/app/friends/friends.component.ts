import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { FriendsService } from '../services/friends.service';
import { AuthService } from '../services/auth.service';
import { User } from '../models';

@Component({
  selector: 'app-friends',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './friends.component.html',
  styleUrl: './friends.component.css'
})
export class FriendsComponent implements OnInit {
  friends = signal<User[]>([]);
  searchResults = signal<User[]>([]);
  isLoading = signal(true);
  isSearching = signal(false);
  searchQuery = '';
  currentUserId: number | null = null;

  constructor(
    private friendsService: FriendsService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.currentUserId = this.authService.getCurrentUserId();
  }

  ngOnInit(): void {
    this.loadFriends();
  }

  loadFriends(): void {
    this.isLoading.set(true);
    this.friendsService.getFriends().subscribe({
      next: (friends) => {
        this.friends.set(friends);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading friends:', error);
        this.isLoading.set(false);
        this.snackBar.open('Błąd podczas ładowania znajomych', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  searchUsers(): void {
    if (!this.searchQuery.trim()) {
      this.searchResults.set([]);
      return;
    }

    this.isSearching.set(true);
    this.friendsService.searchUsers(this.searchQuery).subscribe({
      next: (users) => {
        // Filter out current user and already friends
        const friendIds = new Set(this.friends().map(f => f.id));
        const filtered = users.filter(u => 
          u.id !== this.currentUserId && !friendIds.has(u.id)
        );
        this.searchResults.set(filtered);
        this.isSearching.set(false);
      },
      error: (error) => {
        console.error('Error searching users:', error);
        this.isSearching.set(false);
        this.snackBar.open('Błąd podczas wyszukiwania', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  sendFriendRequest(userId: number): void {
    this.friendsService.sendFriendRequest(userId).subscribe({
      next: () => {
        this.snackBar.open('Wysłano zaproszenie do znajomych!', 'Zamknij', {
          duration: 2000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        // Remove from search results
        this.searchResults.update(results => 
          results.filter(u => u.id !== userId)
        );
      },
      error: (error) => {
        console.error('Error sending friend request:', error);
        const message = error.error?.message || 'Błąd podczas wysyłania zaproszenia';
        this.snackBar.open(message, 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  removeFriend(friendId: number): void {
    if (!confirm('Czy na pewno chcesz usunąć tego znajomego?')) {
      return;
    }

    this.friendsService.removeFriend(friendId).subscribe({
      next: () => {
        this.snackBar.open('Usunięto znajomego', 'Zamknij', {
          duration: 2000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        this.loadFriends();
      },
      error: (error) => {
        console.error('Error removing friend:', error);
        this.snackBar.open('Błąd podczas usuwania znajomego', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  viewProfile(userId: number): void {
    this.router.navigate(['/profile', userId]);
  }

  getDefaultAvatar(username: string): string {
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
  }

  formatLastSeen(lastSeen?: string): string {
    if (!lastSeen) return '';
    const date = new Date(lastSeen);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'teraz';
    if (diffMins < 60) return `${diffMins} min temu`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} h temu`;
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays} dni temu`;
  }
}
