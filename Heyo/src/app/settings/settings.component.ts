import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatSlideToggleModule,
    MatSelectModule,
    MatFormFieldModule,
    MatCardModule,
    MatSnackBarModule
  ],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {
  // Settings state
  emailNotifications = signal(true);
  pushNotifications = signal(true);
  profileVisibility = signal(true);
  language = signal('pl');
  
  currentUser: any = null;

  constructor(
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadSettings();
  }

  loadUserInfo(): void {
    this.currentUser = this.authService.currentUser();
  }

  loadSettings(): void {
    // Load settings from localStorage or backend
    const savedEmailNotif = localStorage.getItem('settings_email_notifications');
    const savedPushNotif = localStorage.getItem('settings_push_notifications');
    const savedProfileVisibility = localStorage.getItem('settings_profile_visibility');
    const savedLanguage = localStorage.getItem('settings_language');

    if (savedEmailNotif !== null) {
      this.emailNotifications.set(savedEmailNotif === 'true');
    }
    if (savedPushNotif !== null) {
      this.pushNotifications.set(savedPushNotif === 'true');
    }
    if (savedProfileVisibility !== null) {
      this.profileVisibility.set(savedProfileVisibility === 'true');
    }
    if (savedLanguage) {
      this.language.set(savedLanguage);
    }
  }

  onEmailNotificationsChange(value: boolean): void {
    this.emailNotifications.set(value);
    localStorage.setItem('settings_email_notifications', value.toString());
    this.snackBar.open('Ustawienia powiadomień email zostały zaktualizowane', 'Zamknij', {
      duration: 2000,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  onPushNotificationsChange(value: boolean): void {
    this.pushNotifications.set(value);
    localStorage.setItem('settings_push_notifications', value.toString());
    this.snackBar.open('Ustawienia powiadomień push zostały zaktualizowane', 'Zamknij', {
      duration: 2000,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  onProfileVisibilityChange(value: boolean): void {
    this.profileVisibility.set(value);
    localStorage.setItem('settings_profile_visibility', value.toString());
    this.snackBar.open('Widoczność profilu została zaktualizowana', 'Zamknij', {
      duration: 2000,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  onLanguageChange(value: string): void {
    this.language.set(value);
    localStorage.setItem('settings_language', value);
    this.snackBar.open('Język został zmieniony', 'Zamknij', {
      duration: 2000,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }
}
