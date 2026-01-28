import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SidenavComponent } from '../sidenav/sidenav.component';
import { WebSocketService, WebSocketConnectionState } from '../services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    SidenavComponent
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent implements OnInit, OnDestroy {
  connectionState = signal<WebSocketConnectionState>(WebSocketConnectionState.DISCONNECTED);
  WebSocketConnectionState = WebSocketConnectionState;
  showConnectionStatus = signal(false);
  
  private subscription?: Subscription;

  constructor(private wsService: WebSocketService) {}

  ngOnInit(): void {
    // Monitor WebSocket connection state
    this.subscription = this.wsService.getConnectionState().subscribe(state => {
      this.connectionState.set(state);
      
      // Show status indicator for 3 seconds when state changes
      if (state !== WebSocketConnectionState.CONNECTED) {
        this.showConnectionStatus.set(true);
      } else {
        // Hide after successful connection
        setTimeout(() => {
          this.showConnectionStatus.set(false);
        }, 2000);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  getConnectionStatusText(): string {
    switch (this.connectionState()) {
      case WebSocketConnectionState.CONNECTING:
        return 'Łączenie...';
      case WebSocketConnectionState.CONNECTED:
        return 'Połączono';
      case WebSocketConnectionState.DISCONNECTED:
        return 'Rozłączono';
      case WebSocketConnectionState.ERROR:
        return 'Błąd połączenia';
      default:
        return '';
    }
  }
}
