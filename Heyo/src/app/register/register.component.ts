import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, ValidationErrors, AbstractControl } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = signal(false);
  hidePassword = signal(true);

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private router: Router,
    private authService: AuthService
  ) {
    this.registerForm = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        confirmEmail: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(4)]],
        confirmPassword: ['', [Validators.required, Validators.minLength(4)]]
      },
      {
        validators: [this.matchFieldsValidator('email', 'confirmEmail'), this.matchFieldsValidator('password', 'confirmPassword')]
      }
    );
  }

  private matchFieldsValidator(field1: string, field2: string) {
    return (group: AbstractControl): ValidationErrors | null => {
      const control1 = group.get(field1);
      const control2 = group.get(field2);

      if (!control1 || !control2) {
        return null;
      }

      if (control2.errors && !control2.errors['mismatch']) {
        return null;
      }

      if (control1.value !== control2.value) {
        control2.setErrors({ mismatch: true });
        return { mismatch: true };
      } else {
        control2.setErrors(null);
        return null;
      }
    };
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched(this.registerForm);
      return;
    }

    this.isLoading.set(true);

    const { email, password } = this.registerForm.value;
    
    this.authService.register(email, password).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        if (response.profileCompleted) {
          this.snackBar.open('Konto zostało utworzone. Zalogowano automatycznie!', 'Zamknij', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/home']);
        } else {
          this.snackBar.open('Konto utworzone! Teraz uzupełnij swój profil.', 'Zamknij', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/register/profile']);
        }
      },
      error: (error) => {
        this.isLoading.set(false);
        let message = 'Błąd podczas rejestracji. Spróbuj ponownie.';
        
        if (error.error?.message) {
          message = error.error.message;
        } else if (error.error?.validationErrors) {
          const errors = error.error.validationErrors;
          message = Object.values(errors).join(', ');
        } else if (error.status === 0) {
          message = 'Nie można połączyć z serwerem. Sprawdź czy backend jest uruchomiony.';
        }
        
        this.snackBar.open(message, 'Zamknij', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  getErrorMessage(field: string): string {
    const control = this.registerForm.get(field);
    if (control?.hasError('required')) {
      return 'To pole jest wymagane';
    }
    if (field.toLowerCase().includes('email') && control?.hasError('email')) {
      return 'Wprowadź poprawny adres e-mail';
    }
    if (control?.hasError('minlength')) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `Minimum ${minLength} znaki/ów`;
    }
    if (control?.hasError('mismatch')) {
      return 'Wartości muszą być takie same';
    }
    return '';
  }
}
