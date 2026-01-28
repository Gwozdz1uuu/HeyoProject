import { Component, OnInit, OnDestroy, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ChatService } from '../services/chat.service';
import { AuthService } from '../services/auth.service';
import { Conversation, ChatMessage, User } from '../models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatProgressSpinnerModule, MatSnackBarModule],
  templateUrl: './messages.component.html',
  styleUrl: './messages.component.css'
})
export class MessagesComponent implements OnInit, OnDestroy {
  conversations = signal<Conversation[]>([]);
  filteredConversations = signal<Conversation[]>([]);
  selectedConversation = signal<Conversation | null>(null);
  messages = signal<ChatMessage[]>([]);
  
  isLoadingConversations = signal(true);
  isLoadingMessages = signal(false);
  isSending = signal(false);
  isLoadingFriends = signal(false);
  isCreatingChat = signal(false);
  
  newMessage = '';
  searchQuery = '';
  showNewChatDialog = signal(false);
  friendsWithoutChat = signal<User[]>([]);
  currentUserId: number | null = null;
  
  // Real-time features
  typingUserId = signal<number | null>(null);
  typingUsername = signal<string>('');
  private typingTimeout: any;
  
  // Subscriptions for cleanup
  private subscriptions = new Subscription();

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.currentUserId = this.authService.getCurrentUserId();
  }

  ngOnInit(): void {
    this.loadConversations();
    this.initializeRealTimeFeatures();
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    this.subscriptions.unsubscribe();
    
    // Clean up WebSocket subscriptions
    const currentUser = this.authService.currentUser();
    if (currentUser) {
      this.chatService.cleanupWebSocket(currentUser.username);
    }
  }

  /**
   * Initialize real-time WebSocket features
   */
  private initializeRealTimeFeatures(): void {
    if (!this.currentUserId) {
      console.error('[MessagesComponent] Cannot initialize - no user ID');
      return;
    }

    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      console.error('[MessagesComponent] Cannot initialize - no user data');
      return;
    }

    console.log('[MessagesComponent] Initializing real-time features for user:', this.currentUserId, 'username:', currentUser.username);

    // Initialize WebSocket subscriptions - use username not ID for Spring STOMP!
    this.chatService.initializeWebSocket(this.currentUserId, currentUser.username);

    // Subscribe to new messages
    this.subscriptions.add(
      this.chatService.newMessage$.subscribe((message: ChatMessage) => {
        console.log('[MessagesComponent] New message event received');
        this.handleNewMessage(message);
      })
    );

    // Subscribe to typing indicators
    this.subscriptions.add(
      this.chatService.typing$.subscribe((data: { userId: number; username: string }) => {
        console.log('[MessagesComponent] Typing indicator received');
        this.handleTypingIndicator(data);
      })
    );

    // Subscribe to online status updates
    this.subscriptions.add(
      this.chatService.onlineStatus$.subscribe((data: { userId: number; online: boolean }) => {
        console.log('[MessagesComponent] Online status update received');
        this.handleOnlineStatusUpdate(data);
      })
    );

    // Subscribe to conversation updates
    this.subscriptions.add(
      this.chatService.conversationUpdate$.subscribe(() => {
        console.log('[MessagesComponent] Conversation update received');
        this.loadConversations();
      })
    );

    // Subscribe to errors
    this.subscriptions.add(
      this.chatService.error$.subscribe((error: { error: string; type: string }) => {
        console.error('[MessagesComponent] Error received:', error);
        this.handleError(error);
      })
    );

    console.log('[MessagesComponent] ✓ All subscriptions established');
  }

  /**
   * Handle WebSocket errors
   */
  private handleError(error: { error: string; type: string }): void {
    let message = 'Wystąpił błąd';
    
    if (error.error.includes('only message your friends')) {
      message = 'Możesz pisać tylko do znajomych. Dodaj tę osobę do znajomych.';
    } else {
      message = error.error;
    }
    
    this.snackBar.open(message, 'Zamknij', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  /**
   * Handle incoming real-time message
   */
  private handleNewMessage(message: ChatMessage): void {
    console.log('[MessagesComponent] New message received:', message);
    
    const isCurrentConversation = this.selectedConversation() && 
      (message.senderId === this.selectedConversation()!.partnerId || 
       message.receiverId === this.selectedConversation()!.partnerId);

    // Add message to current conversation if it's selected
    if (isCurrentConversation) {
      this.messages.update(msgs => [...msgs, message]);
      
      // Auto-scroll to bottom
      setTimeout(() => this.scrollToBottom(), 100);
      
      // Mark as read if not from me
      if (message.senderId !== this.currentUserId) {
        this.chatService.markAsRead(message.senderId).subscribe();
      }
    }

    // Update conversation list
    this.updateConversationLastMessage(message);
  }

  /**
   * Handle typing indicator
   */
  private handleTypingIndicator(data: { userId: number; username: string }): void {
    const isCurrentConversation = this.selectedConversation() && 
      data.userId === this.selectedConversation()!.partnerId;

    if (isCurrentConversation) {
      this.typingUserId.set(data.userId);
      this.typingUsername.set(data.username);

      // Clear typing indicator after 3 seconds
      clearTimeout(this.typingTimeout);
      this.typingTimeout = setTimeout(() => {
        this.typingUserId.set(null);
        this.typingUsername.set('');
      }, 3000);
    }
  }

  /**
   * Handle online status update
   */
  private handleOnlineStatusUpdate(data: { userId: number; online: boolean }): void {
    this.conversations.update(convos =>
      convos.map(c => c.partnerId === data.userId 
        ? { ...c, partnerOnline: data.online } 
        : c
      )
    );
    
    this.filteredConversations.update(convos =>
      convos.map(c => c.partnerId === data.userId 
        ? { ...c, partnerOnline: data.online } 
        : c
      )
    );
  }

  /**
   * Update conversation with new last message
   */
  private updateConversationLastMessage(message: ChatMessage): void {
    const partnerId = message.senderId === this.currentUserId 
      ? message.receiverId 
      : message.senderId;

    this.conversations.update(convos =>
      convos.map(c => c.partnerId === partnerId 
        ? { 
            ...c, 
            lastMessage: message.content, 
            lastMessageAt: message.createdAt,
            unreadCount: message.senderId === this.currentUserId ? c.unreadCount : c.unreadCount + 1
          } 
        : c
      ).sort((a, b) => {
        const dateA = a.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0;
        const dateB = b.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0;
        return dateB - dateA;
      })
    );
  }

  /**
   * Scroll chat to bottom
   */
  private scrollToBottom(): void {
    try {
      const messagesContainer = document.querySelector('.messages-list');
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  loadConversations(): void {
    this.isLoadingConversations.set(true);
    this.chatService.getConversations().subscribe({
      next: (conversations) => {
        this.conversations.set(conversations);
        this.filteredConversations.set(conversations);
        this.isLoadingConversations.set(false);
        
        // Auto-select first conversation if available
        if (conversations.length > 0 && !this.selectedConversation()) {
          this.selectConversation(conversations[0]);
        }
      },
      error: (error) => {
        console.error('Error loading conversations:', error);
        this.isLoadingConversations.set(false);
      }
    });
  }

  searchConversations(): void {
    const query = this.searchQuery.trim();
    if (!query) {
      this.filteredConversations.set(this.conversations());
      return;
    }

    this.chatService.searchConversations(query).subscribe({
      next: (conversations) => {
        this.filteredConversations.set(conversations);
      },
      error: (error) => {
        console.error('Error searching conversations:', error);
        this.filteredConversations.set(this.conversations());
      }
    });
  }

  openNewChatDialog(): void {
    this.showNewChatDialog.set(true);
    this.loadFriendsWithoutChat();
  }

  closeNewChatDialog(): void {
    this.showNewChatDialog.set(false);
  }

  loadFriendsWithoutChat(): void {
    this.isLoadingFriends.set(true);
    this.chatService.getFriendsWithoutChat().subscribe({
      next: (friends) => {
        this.friendsWithoutChat.set(friends);
        this.isLoadingFriends.set(false);
      },
      error: (error) => {
        console.error('Error loading friends:', error);
        this.isLoadingFriends.set(false);
        this.snackBar.open('Błąd podczas ładowania znajomych', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  createChatWithFriend(friend: User): void {
    this.isCreatingChat.set(true);
    this.chatService.createChatWithFriend(friend.id).subscribe({
      next: (conversation) => {
        // Add new conversation to list
        const updatedConversations = [conversation, ...this.conversations()];
        this.conversations.set(updatedConversations);
        this.filteredConversations.set(updatedConversations);
        
        // Select the new conversation
        this.selectConversation(conversation);
        
        this.isCreatingChat.set(false);
        this.closeNewChatDialog();
        
        this.snackBar.open('Rozpoczęto czat z ' + friend.username, 'Zamknij', {
          duration: 2000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      },
      error: (error) => {
        console.error('Error creating chat:', error);
        this.isCreatingChat.set(false);
        const message = error.error?.message || 'Błąd podczas tworzenia czatu';
        this.snackBar.open(message, 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  selectConversation(conversation: Conversation): void {
    this.selectedConversation.set(conversation);
    this.loadMessages(conversation.partnerId);
    
    // Mark as read
    if (conversation.unreadCount > 0) {
      this.chatService.markAsRead(conversation.partnerId).subscribe({
        next: () => {
          this.conversations.update(convos => 
            convos.map(c => c.id === conversation.id ? { ...c, unreadCount: 0 } : c)
          );
        }
      });
    }
  }

  loadMessages(partnerId: number): void {
    this.isLoadingMessages.set(true);
    this.chatService.getConversation(partnerId).subscribe({
      next: (messages) => {
        this.messages.set(messages);
        this.isLoadingMessages.set(false);
        
        // Scroll to bottom after messages loaded
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.isLoadingMessages.set(false);
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.selectedConversation()) return;
    
    const partnerId = this.selectedConversation()!.partnerId;
    const content = this.newMessage.trim();
    this.newMessage = '';

    console.log('[MessagesComponent] Sending message via WebSocket to partner:', partnerId);
    
    // Send via WebSocket for real-time delivery
    this.chatService.sendMessage(partnerId, content);
    
    // Message will be added via WebSocket subscription when confirmed
    console.log('[MessagesComponent] ✓ Message queued for sending');
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    } else if (event.key !== 'Enter') {
      // Send typing indicator
      this.sendTypingIndicator();
    }
  }

  onInput(): void {
    this.sendTypingIndicator();
  }

  private sendTypingIndicator(): void {
    if (!this.selectedConversation()) return;
    
    const partnerId = this.selectedConversation()!.partnerId;
    this.chatService.sendTypingIndicator(partnerId);
  }

  isFromMe(message: ChatMessage): boolean {
    return message.senderId === this.currentUserId;
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return date.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });
    } else if (diffDays === 1) {
      return 'wczoraj';
    } else if (diffDays < 7) {
      return `${diffDays} dni temu`;
    } else {
      return date.toLocaleDateString('pl-PL');
    }
  }

  getDefaultAvatar(username: string): string {
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
  }
}
