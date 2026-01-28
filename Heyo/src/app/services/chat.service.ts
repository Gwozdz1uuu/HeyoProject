import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { Conversation, ChatMessage, User } from '../models';
import { WebSocketService, WebSocketConnectionState } from './websocket.service';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly apiUrl = `${environment.apiUrl}/chat`;
  
  // Real-time message stream
  private newMessageSubject = new Subject<ChatMessage>();
  public newMessage$ = this.newMessageSubject.asObservable();
  
  // Typing indicators
  private typingSubject = new Subject<{ userId: number; username: string }>();
  public typing$ = this.typingSubject.asObservable();
  
  // Online status updates
  private onlineStatusSubject = new Subject<{ userId: number; online: boolean }>();
  public onlineStatus$ = this.onlineStatusSubject.asObservable();
  
  // Conversation updates
  private conversationUpdateSubject = new Subject<void>();
  public conversationUpdate$ = this.conversationUpdateSubject.asObservable();
  
  // Error stream
  private errorSubject = new Subject<{ error: string; type: string }>();
  public error$ = this.errorSubject.asObservable();

  constructor(
    private http: HttpClient,
    private wsService: WebSocketService
  ) {}

  /**
   * Initialize WebSocket subscriptions for real-time chat
   * IMPORTANT: Spring STOMP uses username in routing, not ID!
   */
  initializeWebSocket(userId: number, username: string): void {
    console.log('[ChatService] Initializing WebSocket subscriptions for user:', userId, 'username:', username);
    
    // Wait for connection before subscribing
    const setupSubscriptions = () => {
      if (!this.wsService.isConnected()) {
        console.warn('[ChatService] WebSocket not connected, will retry...');
        setTimeout(setupSubscriptions, 500);
        return;
      }
      
      console.log('[ChatService] WebSocket is connected, setting up subscriptions...');
      
      // Subscribe to incoming messages - Spring STOMP auto-maps /user/queue/* to /user/{username}/queue/*
      this.wsService.subscribe(`/user/queue/messages`, (message: ChatMessage) => {
        console.log('[ChatService] ✓✓✓ New message received via WebSocket:', message);
        this.newMessageSubject.next(message);
        this.conversationUpdateSubject.next();
      });

      // Subscribe to typing indicators
      this.wsService.subscribe(`/user/queue/typing`, (data: { userId: number; username: string }) => {
        console.log('[ChatService] ✓ Typing indicator received:', data);
        this.typingSubject.next(data);
      });

      // Subscribe to online status updates
      this.wsService.subscribe(`/user/queue/status`, (data: { userId: number; online: boolean }) => {
        console.log('[ChatService] ✓ Online status update received:', data);
        this.onlineStatusSubject.next(data);
      });
      
      // Subscribe to errors
      this.wsService.subscribe(`/user/queue/errors`, (error: { error: string; type: string }) => {
        console.error('[ChatService] ✗ Error received:', error);
        this.errorSubject.next(error);
      });
      
      console.log('[ChatService] ✓ All subscription callbacks registered');
    };
    
    // Try immediately if connected, otherwise wait
    if (this.wsService.isConnected()) {
      setupSubscriptions();
    } else {
      // Wait for connection
      const connectionSub = this.wsService.getConnectionState().subscribe(state => {
        if (state === WebSocketConnectionState.CONNECTED) {
          setTimeout(setupSubscriptions, 500); // Small delay for STOMP to be fully ready
          connectionSub.unsubscribe();
        }
      });
    }
  }

  /**
   * Clean up WebSocket subscriptions
   */
  cleanupWebSocket(username: string): void {
    this.wsService.unsubscribe(`/user/queue/messages`);
    this.wsService.unsubscribe(`/user/queue/typing`);
    this.wsService.unsubscribe(`/user/queue/status`);
    this.wsService.unsubscribe(`/user/queue/errors`);
  }

  // HTTP API methods (fallback and initial data loading)

  getConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.apiUrl}/conversations`);
  }

  getConversation(partnerId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/conversations/${partnerId}`);
  }

  /**
   * Send message via WebSocket for real-time delivery
   */
  sendMessage(receiverId: number, content: string): void {
    this.wsService.sendChatMessage(receiverId, content);
  }

  /**
   * Send typing indicator
   */
  sendTypingIndicator(receiverId: number): void {
    this.wsService.sendTypingIndicator(receiverId);
  }

  markAsRead(partnerId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/conversations/${partnerId}/read`, {});
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`);
  }

  searchConversations(query: string): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.apiUrl}/conversations/search`, {
      params: { query }
    });
  }

  createChatWithFriend(friendId: number): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.apiUrl}/conversations/create/${friendId}`, {});
  }

  getFriendsWithoutChat(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/friends/without-chat`);
  }
}
