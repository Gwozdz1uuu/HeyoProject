import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { EventService } from '../services/event.service';
import { Event } from '../models';
import { CreateEventDialogComponent } from './create-event-dialog.component';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, MatIconModule, MatSnackBarModule, MatDialogModule, MatButtonModule],
  templateUrl: './events.component.html',
  styleUrl: './events.component.css'
})
export class EventsComponent implements OnInit {
  events = signal<Event[]>([]);
  isLoading = signal(true);

  ads = [
    { image: 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=200&h=300&fit=crop' },
    { image: 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=200&h=300&fit=crop' },
    { image: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=200&h=300&fit=crop' }
  ];

  constructor(
    private eventService: EventService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.isLoading.set(true);
    this.eventService.getUpcomingEvents().subscribe({
      next: (response) => {
        this.events.set(response.content);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading events:', error);
        this.isLoading.set(false);
        this.snackBar.open('Błąd podczas ładowania wydarzeń', 'Zamknij', {
          duration: 3000
        });
      }
    });
  }

  toggleInterested(event: Event): void {
    this.eventService.toggleInterested(event.id).subscribe({
      next: (updatedEvent) => {
        this.events.update(events =>
          events.map(e => e.id === event.id ? updatedEvent : e)
        );
        const message = updatedEvent.isInterested 
          ? 'Dodano do zainteresowanych' 
          : 'Usunięto z zainteresowanych';
        this.snackBar.open(message, 'Zamknij', { duration: 2000 });
      },
      error: (error) => {
        console.error('Error toggling interest:', error);
      }
    });
  }

  toggleParticipating(event: Event): void {
    const wasParticipating = event.isParticipating;
    this.eventService.toggleParticipating(event.id).subscribe({
      next: (updatedEvent) => {
        console.log('Toggle participation response:', updatedEvent);
        console.log('isParticipating:', updatedEvent.isParticipating);
        this.events.update(events =>
          events.map(e => e.id === event.id ? updatedEvent : e)
        );
        // Show message based on the new state
        const message = updatedEvent.isParticipating 
          ? 'Bierzesz udział w wydarzeniu' 
          : 'Zrezygnowano z udziału';
        this.snackBar.open(message, 'Zamknij', { duration: 2000 });
      },
      error: (error) => {
        console.error('Error toggling participation:', error);
        this.snackBar.open('Błąd podczas aktualizacji udziału', 'Zamknij', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  formatEventDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    const isToday = date.toDateString() === now.toDateString();
    const isTomorrow = date.toDateString() === tomorrow.toDateString();
    
    const time = date.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });
    
    if (isToday) {
      return `dziś o ${time}`;
    } else if (isTomorrow) {
      return `jutro o ${time}`;
    } else {
      return date.toLocaleDateString('pl-PL', { 
        day: 'numeric', 
        month: 'short',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }

  getInterestedText(event: Event): string {
    if (event.interestedCount === 0) {
      return 'Bądź pierwszy zainteresowany!';
    } else if (event.interestedCount === 1) {
      return '1 osoba jest zainteresowana';
    } else {
      return `${event.interestedCount} osób jest zainteresowanych`;
    }
  }

  openCreateEventDialog(): void {
    const dialogRef = this.dialog.open(CreateEventDialogComponent, {
      width: '600px',
      maxWidth: '90vw'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Reload events after creation
        this.loadEvents();
      }
    });
  }
}
