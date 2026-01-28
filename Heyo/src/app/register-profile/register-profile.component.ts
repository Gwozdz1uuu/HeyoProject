import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ProfileService } from '../services/profile.service';
import { UploadService } from '../services/upload.service';
import { Interest, ProfileCreateRequest } from '../models';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-register-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatSelectModule,
    MatFormFieldModule
  ],
  templateUrl: './register-profile.component.html',
  styleUrl: './register-profile.component.css'
})
export class RegisterProfileComponent {
  profileForm: FormGroup;
  isLoading = signal(false);
  isUploading = signal(false);
  interests: Interest[] = [];
  selectedInterests = signal<number[]>([]);
  avatarPreview: string | null = null;
  avatarFile: File | null = null;
  showInterestsDropdown = signal(false);

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private profileService: ProfileService,
    private uploadService: UploadService,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      nickname: ['']
    });

    this.loadInterests();
  }

  loadInterests(): void {
    this.profileService.getAvailableInterests().subscribe({
      next: (interests) => {
        this.interests = interests;
      },
      error: (error) => {
        console.error('Error loading interests:', error);
        this.snackBar.open('Błąd podczas ładowania zainteresowań', 'Zamknij', {
          duration: 3000
        });
      }
    });
  }

  toggleInterest(interestId: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    const current = this.selectedInterests();
    
    if (checked) {
      this.selectedInterests.set([...current, interestId]);
    } else {
      this.selectedInterests.set(current.filter(id => id !== interestId));
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
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

      this.avatarFile = file;

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.avatarPreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeAvatar(): void {
    this.avatarPreview = null;
    this.avatarFile = null;
  }

  async onSubmit(): Promise<void> {
    if (this.profileForm.invalid) {
      this.markFormGroupTouched(this.profileForm);
      return;
    }

    this.isLoading.set(true);

    try {
      const { firstName, lastName, nickname } = this.profileForm.value;

      // Najpierw wyślij avatar (jeśli wybrany), tak samo jak w sekcji profilu
      let avatarUrl: string | undefined;
      if (this.avatarFile) {
        this.isUploading.set(true);
        const uploadResponse = await firstValueFrom(this.uploadService.uploadAvatar(this.avatarFile));
        // Zbuduj pełny URL tak samo jak w sekcji profilu
        avatarUrl = uploadResponse.url.startsWith('http')
          ? uploadResponse.url
          : `${environment.apiUrl.replace('/api', '')}${uploadResponse.url}`;
        this.isUploading.set(false);
      }

      const profileData: ProfileCreateRequest = {
        firstName,
        lastName,
        nickname: nickname || undefined,
        avatarUrl,
        interestIds: this.selectedInterests()
      };

      // Utwórz profil
      await firstValueFrom(this.authService.completeProfile(profileData));

      // Dla pewności ustaw avatar tak samo jak w sekcji profilu
      if (avatarUrl) {
        try {
          await firstValueFrom(this.profileService.updateAvatar(avatarUrl));
        } catch (e) {
          console.error('Error updating avatar after profile creation', e);
        }
      }

      this.isLoading.set(false);
      this.snackBar.open('Profil został utworzony! Witamy w Heyo!', 'Zamknij', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['success-snackbar']
      });
      this.router.navigate(['/home']);
    } catch (error: any) {
      this.isLoading.set(false);
      this.isUploading.set(false);
      let message = 'Błąd podczas tworzenia profilu. Spróbuj ponownie.';
      
      if (error.error?.message) {
        message = error.error.message;
      } else if (error.error?.validationErrors) {
        const errors = error.error.validationErrors;
        message = Object.values(errors).join(', ');
      }
      
      this.snackBar.open(message, 'Zamknij', {
        duration: 4000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
    }
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  getErrorMessage(field: string): string {
    const control = this.profileForm.get(field);
    if (control?.hasError('required')) {
      return 'To pole jest wymagane';
    }
    return '';
  }
}
