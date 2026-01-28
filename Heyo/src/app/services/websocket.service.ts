import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export enum WebSocketConnectionState {
  CONNECTING = 'CONNECTING',
  CONNECTED = 'CONNECTED',
  DISCONNECTED = 'DISCONNECTED',
  ERROR = 'ERROR'
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;
  private connectionState$ = new BehaviorSubject<WebSocketConnectionState>(
    WebSocketConnectionState.DISCONNECTED
  );
  private messageSubject$ = new Subject<{ destination: string; body: any }>();
  private subscriptions: Map<string, StompSubscription> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  constructor() {}

  /**
   * Connect to WebSocket server with STOMP protocol
   * @param token JWT token for authentication
   * @param force If true, disconnect existing connection and reconnect with new token
   */
  async connect(token: string, force: boolean = false): Promise<void> {
    // If already connected and not forcing reconnect, return early
    if (this.isConnected() && !force) {
      console.log('[WebSocket] Already connected');
      return Promise.resolve();
    }

    // If forcing reconnect, disconnect first
    if (force && this.isConnected()) {
      console.log('[WebSocket] Force reconnect - disconnecting first...');
      await this.disconnect();
      // Small delay to ensure disconnect is complete
      await new Promise(resolve => setTimeout(resolve, 100));
    }

    return new Promise((resolve, reject) => {
      try {
        this.connectionState$.next(WebSocketConnectionState.CONNECTING);
        
        // Create STOMP client
        this.stompClient = new Client({
          webSocketFactory: () => new SockJS(`${environment.apiUrl.replace('/api', '')}/ws`),
          connectHeaders: {
            Authorization: `Bearer ${token}`
          },
          debug: (str) => {
            // console.log('[STOMP Debug]', str);
          },
          reconnectDelay: this.reconnectDelay,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
          onConnect: (frame) => {
            console.log('[WebSocket] Connected successfully', frame);
            console.log('[WebSocket] Frame headers:', frame.headers);
            this.connectionState$.next(WebSocketConnectionState.CONNECTED);
            this.reconnectAttempts = 0;
            
            // Test: subscribe to all messages to see what's coming through
            this.stompClient!.subscribe('/**', (msg) => {
              console.log('[WebSocket] *** ALL MESSAGES DEBUG ***');
              console.log('[WebSocket] Destination:', msg.headers['destination']);
              console.log('[WebSocket] Body:', msg.body);
            });
            
            resolve();
          },
          onStompError: (frame) => {
            console.error('[WebSocket] STOMP error', frame);
            this.connectionState$.next(WebSocketConnectionState.ERROR);
            reject(new Error('STOMP connection error'));
          },
          onWebSocketClose: (event) => {
            console.log('[WebSocket] Connection closed', event);
            this.connectionState$.next(WebSocketConnectionState.DISCONNECTED);
            // Don't auto-reconnect on close - let the application handle it
            // this.handleReconnect(token);
          },
          onWebSocketError: (event) => {
            console.error('[WebSocket] WebSocket error', event);
            this.connectionState$.next(WebSocketConnectionState.ERROR);
          }
        });

        // Activate the client
        this.stompClient.activate();
      } catch (error) {
        console.error('[WebSocket] Connection error', error);
        this.connectionState$.next(WebSocketConnectionState.ERROR);
        reject(error);
      }
    });
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): Promise<void> {
    return new Promise((resolve) => {
      if (!this.stompClient) {
        resolve();
        return;
      }

      // Clear all subscriptions
      this.subscriptions.forEach((sub) => sub.unsubscribe());
      this.subscriptions.clear();

      // Deactivate client
      this.stompClient.deactivate().then(() => {
        console.log('[WebSocket] Disconnected');
        this.stompClient = null;
        this.connectionState$.next(WebSocketConnectionState.DISCONNECTED);
        resolve();
      });
    });
  }

  /**
   * Subscribe to a destination (topic or queue)
   * Waits for connection if not yet connected
   */
  subscribe(destination: string, callback?: (message: any) => void): Observable<any> {
    const attemptSubscription = () => {
      if (!this.isConnected() || !this.stompClient) {
        console.warn('[WebSocket] Not connected yet, waiting for connection...');
        return;
      }

      // Unsubscribe if already subscribed
      if (this.subscriptions.has(destination)) {
        this.subscriptions.get(destination)?.unsubscribe();
      }

      try {
        // Create new subscription with explicit logging
        console.log(`[WebSocket] Creating subscription to ${destination}...`);
        const subscription = this.stompClient.subscribe(destination, (message: IMessage) => {
          console.log(`[WebSocket] RAW MESSAGE RECEIVED on ${destination}:`, message);
          console.log(`[WebSocket] Message body type:`, typeof message.body);
          console.log(`[WebSocket] Message body:`, message.body);
          
          try {
            const body = typeof message.body === 'string' 
              ? JSON.parse(message.body) 
              : message.body;
              
            console.log(`[WebSocket] ✓✓✓ PARSED MESSAGE RECEIVED on ${destination}:`, body);
            
            // Emit to subject for observable pattern
            this.messageSubject$.next({ destination, body });
            console.log(`[WebSocket] Emitted to messageSubject$`);
            
            // Call callback if provided
            if (callback) {
              console.log(`[WebSocket] Calling callback for ${destination} with:`, body);
              callback(body);
              console.log(`[WebSocket] Callback executed`);
            } else {
              console.warn(`[WebSocket] No callback registered for ${destination}`);
            }
          } catch (error) {
            console.error('[WebSocket] Error parsing message', error, 'Raw body:', message.body);
          }
        });

        this.subscriptions.set(destination, subscription);
        console.log(`[WebSocket] ✓ Successfully subscribed to ${destination}`);
        console.log(`[WebSocket] Active subscriptions:`, Array.from(this.subscriptions.keys()));
      } catch (error) {
        console.error(`[WebSocket] Failed to subscribe to ${destination}`, error);
      }
    };

    // If already connected, subscribe immediately
    if (this.isConnected()) {
      attemptSubscription();
    } else {
      // Wait for connection
      const connectionSub = this.connectionState$.subscribe(state => {
        if (state === WebSocketConnectionState.CONNECTED) {
          attemptSubscription();
          connectionSub.unsubscribe();
        }
      });
    }

    // Return observable filtered by destination
    return this.messageSubject$.asObservable().pipe(
      filter(msg => msg.destination === destination)
    );
  }

  /**
   * Unsubscribe from a destination
   */
  unsubscribe(destination: string): void {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
      console.log(`[WebSocket] Unsubscribed from ${destination}`);
    }
  }

  /**
   * Send a message to a destination
   * Waits for connection if not yet connected
   */
  send(destination: string, body: any): void {
    if (!this.isConnected() || !this.stompClient) {
      console.warn('[WebSocket] Not connected, queuing message...');
      
      // Wait for connection and retry
      const connectionSub = this.connectionState$.subscribe(state => {
        if (state === WebSocketConnectionState.CONNECTED) {
          this.sendNow(destination, body);
          connectionSub.unsubscribe();
        }
      });
      
      // Timeout after 5 seconds
      setTimeout(() => {
        connectionSub.unsubscribe();
        console.error('[WebSocket] Send timeout - connection not established');
      }, 5000);
      
      return;
    }

    this.sendNow(destination, body);
  }

  private sendNow(destination: string, body: any): void {
    if (!this.stompClient) return;

    try {
      this.stompClient.publish({
        destination,
        body: JSON.stringify(body)
      });
      console.log(`[WebSocket] ✓ Message sent to ${destination}:`, body);
    } catch (error) {
      console.error('[WebSocket] Error sending message', error);
    }
  }

  /**
   * Get connection state as observable
   */
  getConnectionState(): Observable<WebSocketConnectionState> {
    return this.connectionState$.asObservable();
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.connectionState$.value === WebSocketConnectionState.CONNECTED;
  }

  /**
   * Get all messages as observable
   */
  getMessages(): Observable<{ destination: string; body: any }> {
    return this.messageSubject$.asObservable();
  }

  /**
   * Handle reconnection logic
   */
  private handleReconnect(token: string): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[WebSocket] Max reconnection attempts reached');
      return;
    }

    this.reconnectAttempts++;
    console.log(`[WebSocket] Reconnecting... Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);

    setTimeout(() => {
      this.connect(token).catch(error => {
        console.error('[WebSocket] Reconnection failed', error);
      });
    }, this.reconnectDelay * this.reconnectAttempts);
  }

  /**
   * Set user online status
   */
  setOnlineStatus(online: boolean): void {
    if (!this.isConnected()) return;

    const destination = online ? '/app/user.online' : '/app/user.offline';
    this.send(destination, {});
  }

  /**
   * Send typing indicator
   */
  sendTypingIndicator(receiverId: number): void {
    if (!this.isConnected()) return;

    this.send('/app/chat.typing', { receiverId });
  }

  /**
   * Send chat message via WebSocket
   */
  sendChatMessage(receiverId: number, content: string): void {
    if (!this.isConnected()) return;

    this.send('/app/chat.send', { receiverId, content });
  }
}
