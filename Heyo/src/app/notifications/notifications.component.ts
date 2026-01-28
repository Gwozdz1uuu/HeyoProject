import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NotificationService } from '../services/notification.service';
import { FriendsService } from '../services/friends.service';
import { Notification } from '../models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, MatIconModule, MatSnackBarModule],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {
  notifications = signal<Notification[]>([]);
  isLoading = signal(true);

  ads = [
    { image: 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=200&h=300&fit=crop' },
    { image: 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=200&h=300&fit=crop' },
    { image: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=200&h=300&fit=crop' }
  ];

  constructor(
    private notificationService: NotificationService,
    private friendsService: FriendsService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading.set(true);
    this.notificationService.getNotifications().subscribe({
      next: (response) => {
        this.notifications.set(response.content);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
        this.isLoading.set(false);
      }
    });
  }

  markAsRead(notification: Notification): void {
    if (notification.read) return;
    
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications.update(notifs =>
          notifs.map(n => n.id === notification.id ? { ...n, read: true } : n)
        );
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update(notifs =>
          notifs.map(n => ({ ...n, read: true }))
        );
      }
    });
  }

  getNotificationText(notification: Notification): string {
    switch (notification.type) {
      case 'NEW_POST':
        return 'wstawił nowy post';
      case 'NEW_COMMENT':
        return 'skomentował Twój post';
      case 'NEW_LIKE':
        return 'polubił Twój post';
      case 'NEW_FOLLOWER':
        return 'zaczął Cię obserwować';
      case 'NEW_EVENT':
        return 'utworzył nowe wydarzenie';
      case 'EVENT_REMINDER':
        return 'przypomnienie o wydarzeniu';
      case 'BIRTHDAY':
        return 'ma dziś urodziny!';
      case 'FRIEND_REQUEST':
        return 'wysłał Ci zaproszenie do znajomych';
      case 'FRIEND_REQUEST_ACCEPTED':
        return 'zaakceptował Twoje zaproszenie do znajomych';
      case 'FRIEND_REQUEST_DECLINED':
        return 'odrzucił Twoje zaproszenie do znajomych';
      default:
        return notification.message || '';
    }
  }

  isFriendRequest(notification: Notification): boolean {
    return notification.type === 'FRIEND_REQUEST';
  }

  acceptFriendRequest(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.friendsService.acceptFriendRequest(notification.id).subscribe({
      next: () => {
        this.snackBar.open('Zaakceptowano zaproszenie!', 'Zamknij', {
          duration: 2000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        // Remove the notification from the list
        this.notifications.update(notifs =>
          notifs.filter(n => n.id !== notification.id)
        );
      },
      error: (error) => {
        console.error('Error accepting friend request:', error);
        this.snackBar.open('Błąd podczas akceptacji zaproszenia', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  declineFriendRequest(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.friendsService.declineFriendRequest(notification.id).subscribe({
      next: () => {
        this.snackBar.open('Odrzucono zaproszenie', 'Zamknij', {
          duration: 2000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        // Remove the notification from the list
        this.notifications.update(notifs =>
          notifs.filter(n => n.id !== notification.id)
        );
      },
      error: (error) => {
        console.error('Error declining friend request:', error);
        this.snackBar.open('Błąd podczas odrzucania zaproszenia', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'teraz';
    if (diffMins < 60) return `${diffMins} min temu`;
    if (diffHours < 24) return `${diffHours} godz. temu`;
    if (diffDays < 7) return `${diffDays} dni temu`;
    return date.toLocaleDateString('pl-PL');
  }

  getDefaultAvatar(username: string): string {
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
  }
}
