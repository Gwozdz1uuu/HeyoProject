import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PostService } from '../services/post.service';
import { AuthService } from '../services/auth.service';
import { Post, PostCreateRequest, Comment } from '../models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  posts = signal<Post[]>([]);
  isLoading = signal(true);
  isCreatingPost = signal(false);
  newPostContent = '';
  newPostImageUrl = '';
  
  // Comment management
  expandedComments = signal<Set<number>>(new Set());
  postComments = signal<Map<number, Comment[]>>(new Map());
  loadingComments = signal<Set<number>>(new Set());
  commentInputs = signal<Map<number, string>>(new Map());
  isSubmittingComment = signal<Map<number, boolean>>(new Map());

  ads = [
    { id: 1, image: 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=200&h=300&fit=crop' },
    { id: 2, image: 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=200&h=300&fit=crop' },
    { id: 3, image: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=200&h=300&fit=crop' }
  ];

  constructor(
    private postService: PostService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Check if user is authenticated before loading posts
    if (!this.authService.isAuthenticated()) {
      console.warn('User not authenticated, redirecting to login');
      this.authService.logout();
      return;
    }
    this.loadPosts();
  }

  loadPosts(): void {
    this.isLoading.set(true);
    this.postService.getFeed().subscribe({
      next: (response) => {
        this.posts.set(response.content);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading posts:', error);
        this.isLoading.set(false);
        this.snackBar.open('Błąd podczas ładowania postów', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  createPost(): void {
    if (!this.newPostContent.trim() && !this.newPostImageUrl.trim()) {
      return;
    }

    // Check authentication before creating post
    if (!this.authService.isAuthenticated()) {
      this.snackBar.open('Musisz być zalogowany, aby utworzyć post', 'Zamknij', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'top'
      });
      return;
    }

    this.isCreatingPost.set(true);
    const request: PostCreateRequest = {
      content: this.newPostContent,
      imageUrl: this.newPostImageUrl || undefined
    };

    this.postService.createPost(request).subscribe({
      next: (post) => {
        this.posts.update(posts => [post, ...posts]);
        this.newPostContent = '';
        this.newPostImageUrl = '';
        this.isCreatingPost.set(false);
        this.snackBar.open('Post utworzony!', 'Zamknij', {
          duration: 2000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      },
      error: (error) => {
        console.error('Error creating post:', error);
        console.error('Error status:', error?.status);
        console.error('Error message:', error?.message);
        console.error('Error details:', error?.error);
        
        let errorMessage = 'Błąd podczas tworzenia posta';
        if (error?.status === 401) {
          errorMessage = 'Sesja wygasła. Zaloguj się ponownie.';
          this.authService.logout();
        } else if (error?.status === 403) {
          errorMessage = 'Brak uprawnień. Sprawdź, czy jesteś zalogowany.';
        } else if (error?.error?.message) {
          errorMessage = error.error.message;
        }
        
        this.isCreatingPost.set(false);
        this.snackBar.open(errorMessage, 'Zamknij', {
          duration: 4000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
      }
    });
  }

  likePost(post: Post): void {
    this.postService.likePost(post.id).subscribe({
      next: (updatedPost) => {
        this.posts.update(posts => 
          posts.map(p => p.id === post.id ? updatedPost : p)
        );
      },
      error: (error) => {
        console.error('Error liking post:', error);
      }
    });
  }

  toggleComments(postId: number): void {
    const expanded = new Set(this.expandedComments());
    if (expanded.has(postId)) {
      expanded.delete(postId);
    } else {
      expanded.add(postId);
      this.loadComments(postId);
    }
    this.expandedComments.set(expanded);
  }

  isCommentsExpanded(postId: number): boolean {
    return this.expandedComments().has(postId);
  }

  loadComments(postId: number): void {
    // Check if comments are already loaded
    if (this.postComments().has(postId)) {
      return;
    }

    this.loadingComments.update(loading => {
      const newSet = new Set(loading);
      newSet.add(postId);
      return newSet;
    });

    this.postService.getComments(postId).subscribe({
      next: (comments) => {
        this.postComments.update(map => {
          const newMap = new Map(map);
          newMap.set(postId, comments);
          return newMap;
        });
        this.loadingComments.update(loading => {
          const newSet = new Set(loading);
          newSet.delete(postId);
          return newSet;
        });
      },
      error: (error) => {
        console.error('Error loading comments:', error);
        this.loadingComments.update(loading => {
          const newSet = new Set(loading);
          newSet.delete(postId);
          return newSet;
        });
      }
    });
  }

  getComments(postId: number): Comment[] {
    return this.postComments().get(postId) || [];
  }

  isLoadingComments(postId: number): boolean {
    return this.loadingComments().has(postId);
  }

  setCommentInput(postId: number, value: string): void {
    this.commentInputs.update(map => {
      const newMap = new Map(map);
      newMap.set(postId, value);
      return newMap;
    });
  }

  getCommentInput(postId: number): string {
    return this.commentInputs().get(postId) || '';
  }

  addComment(postId: number): void {
    const content = this.getCommentInput(postId);
    if (!content.trim()) {
      return;
    }

    this.isSubmittingComment.update(map => {
      const newMap = new Map(map);
      newMap.set(postId, true);
      return newMap;
    });

    this.postService.addComment(postId, content.trim()).subscribe({
      next: (comment) => {
        // Add comment to the list
        this.postComments.update(map => {
          const newMap = new Map(map);
          const existingComments = newMap.get(postId) || [];
          newMap.set(postId, [comment, ...existingComments]);
          return newMap;
        });

        // Update post comments count
        this.posts.update(posts => 
          posts.map(p => 
            p.id === postId 
              ? { ...p, commentsCount: p.commentsCount + 1 }
              : p
          )
        );

        // Clear input
        this.setCommentInput(postId, '');
        this.isSubmittingComment.update(map => {
          const newMap = new Map(map);
          newMap.delete(postId);
          return newMap;
        });
      },
      error: (error) => {
        console.error('Error adding comment:', error);
        this.snackBar.open('Błąd podczas dodawania komentarza', 'Zamknij', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'top'
        });
        this.isSubmittingComment.update(map => {
          const newMap = new Map(map);
          newMap.delete(postId);
          return newMap;
        });
      }
    });
  }

  isSubmittingCommentFor(postId: number): boolean {
    return this.isSubmittingComment().get(postId) || false;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }

  getDefaultAvatar(username: string): string {
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
  }
}
