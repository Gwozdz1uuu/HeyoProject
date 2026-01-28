import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EventService } from '../services/event.service';
import { UploadService } from '../services/upload.service';
import { EventCreateRequest } from '../models';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-create-event-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './create-event-dialog.component.html',
  styleUrl: './create-event-dialog.component.css'
})
export class CreateEventDialogComponent {
  eventForm: FormGroup;
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  isUploading = false;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<CreateEventDialogComponent>,
    private eventService: EventService,
    private uploadService: UploadService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.eventForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(1000)]],
      eventDate: ['', Validators.required],
      eventTime: ['', Validators.required],
      location: ['', [Validators.maxLength(200)]],
      hashtags: ['', [Validators.maxLength(200)]]
    });
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

      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage(): void {
    this.selectedFile = null;
    this.imagePreview = null;
  }

  async onSubmit(): Promise<void> {
    if (this.eventForm.invalid) {
      this.eventForm.markAllAsTouched();
      return;
    }

    // Validate date is in the future
    const formValue = this.eventForm.value;
    const eventDate = new Date(formValue.eventDate);
    const eventTime = formValue.eventTime.split(':');
    eventDate.setHours(parseInt(eventTime[0]), parseInt(eventTime[1]), 0, 0);
    
    if (eventDate <= new Date()) {
      this.snackBar.open('Data wydarzenia musi być w przyszłości', 'Zamknij', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isSubmitting = true;

    try {
      let imageUrl: string | undefined;

      // Upload image if selected
      if (this.selectedFile) {
        this.isUploading = true;
        try {
          const uploadResponse = await firstValueFrom(this.uploadService.uploadEventImage(this.selectedFile));
          // Construct full URL if relative path is returned
          imageUrl = uploadResponse.url.startsWith('http')
            ? uploadResponse.url
            : `${environment.apiUrl.replace('/api', '')}${uploadResponse.url}`;
        } catch (error) {
          console.error('Error uploading image:', error);
          this.snackBar.open('Błąd podczas przesyłania zdjęcia', 'Zamknij', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
          this.isSubmitting = false;
          this.isUploading = false;
          return;
        } finally {
          this.isUploading = false;
        }
      }

      // Create event request
      const eventRequest: EventCreateRequest = {
        title: formValue.title,
        description: formValue.description || undefined,
        imageUrl: imageUrl,
        eventDate: eventDate.toISOString(),
        location: formValue.location || undefined,
        hashtags: formValue.hashtags || undefined
      };

      // Create event
      const createdEvent = await firstValueFrom(this.eventService.createEvent(eventRequest));
      
      this.snackBar.open('Wydarzenie zostało utworzone', 'Zamknij', {
        duration: 3000,
        panelClass: ['success-snackbar']
      });

      this.dialogRef.close(createdEvent);
    } catch (error: any) {
      console.error('Error creating event:', error);
      let errorMessage = 'Błąd podczas tworzenia wydarzenia';
      if (error.error?.message) {
        errorMessage = error.error.message;
      }
      this.snackBar.open(errorMessage, 'Zamknij', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
    } finally {
      this.isSubmitting = false;
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
