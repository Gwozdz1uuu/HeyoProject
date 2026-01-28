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
    {
      id: 1,
      image: 'https://th.bing.com/th/id/OIP.Lp871-g7bp0ZydLC2YMQwQHaFb?w=243&h=180&c=7&r=0&o=7&cb=defcache2&dpr=1.3&pid=1.7&rm=3&defcache=1',
      title: 'Specjalna Oferta',
      description: 'Odkryj nasze najlepsze produkty w atrakcyjnych cenach!'
    },
    {
      id: 2,
      image: 'https://www.bing.com/images/search?view=detailV2&ccid=bls5%2FtwP&id=AACC59A29B2F2A4A0D85B2B59CBD6CA497D58125&thid=OIP.bls5_twPKTLxtDFBUI0DngHaHa&mediaurl=https%3A%2F%2Fa.allegroimg.com%2Foriginal%2F1170f6%2Fc82d88594e9ab8b2c2ff083ececd%2FPlakat-Reklamowy-B1-WYPRZEDAZ-na-witryne-sklepowa-70x100cm-Papier-150g&cdnurl=https%3A%2F%2Fth.bing.com%2Fth%2Fid%2FR.6e5b39fedc0f2932f1b43141508d039e%3Frik%3DJYHVl6RsvZy1sg%26pid%3DImgRaw%26r%3D0&exph=2560&expw=2560&q=oferta+specjalna+meme&form=IRPRST&ck=BB47B3F93E41353B6E499E8B77A8C192&selectedindex=23&itb=0&ajaxhist=0&ajaxserp=0&cit=ccid_0O8JzsvM*cp_0DD2B18611A89B887D54C1D0A4578F9C*mid_CE314115C7D673EAC12195F6757A9E6021973215*thid_OIP.0O8JzsvMXwFJTn8g1fwLugHaHa&vt=2',
      title: 'Nowy Produkt',
      description: 'Sprawdź najnowsze trendy i innowacyjne rozwiązania.'
    },
    {
      id: 3,
      image: 'https://www.bing.com/images/search?view=detailV2&ccid=7ECzELod&id=8B53006D93719D4EC5DE2AC166C04CFA2CC63E77&thid=OIP.7ECzELodTBT0aMhuZ664wgHaHX&mediaurl=https%3A%2F%2Fimage.ceneostatic.pl%2Fdata%2Fproducts%2F156859768%2Fi-naklejka-na-witryne-sklepowa-30cm.jpg&cdnurl=https%3A%2F%2Fth.bing.com%2Fth%2Fid%2FR.ec40b310ba1d4c14f468c86e67aeb8c2%3Frik%3Ddz7GLPpMwGbBKg%26pid%3DImgRaw%26r%3D0&exph=2538&expw=2550&q=oferta+specjalna+meme&form=IRPRST&ck=537EDB1CEB537CCB480198AB7A1CE03C&selectedindex=27&itb=0&ajaxhist=0&ajaxserp=0&cit=ccid_0O8JzsvM*cp_0DD2B18611A89B887D54C1D0A4578F9C*mid_CE314115C7D673EAC12195F6757A9E6021973215*thid_OIP.0O8JzsvMXwFJTn8g1fwLugHaHa&vt=2',
      title: 'Promocja',
      description: 'Nie przegap wyjątkowych okazji i zniżek!'
    }
  ];

  getFeedWithAds(): Array<{type: 'post', post: Post} | {type: 'ad', ad: any}> {
    const feedItems: Array<{type: 'post', post: Post} | {type: 'ad', ad: any}> = [];
    const posts = this.posts();
    let adIndex = 0;
    
    posts.forEach((post, index) => {
      feedItems.push({ type: 'post', post });
      
      // Dodaj reklamę co 2 posty (po 1, 3, 5, itd.)
      if ((index + 1) % 2 === 0 && adIndex < this.ads.length) {
        feedItems.push({ type: 'ad', ad: this.ads[adIndex] });
        adIndex++;
      }
    });
    
    return feedItems;
  }

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
