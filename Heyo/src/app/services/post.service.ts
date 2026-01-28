import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Post, PostCreateRequest, Comment, Page } from '../models';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private readonly apiUrl = `${environment.apiUrl}/posts`;

  constructor(private http: HttpClient) {}

  getFeed(page: number = 0, size: number = 20): Observable<Page<Post>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<Post>>(this.apiUrl, { params });
  }

  getUserPosts(userId: number, page: number = 0, size: number = 20): Observable<Page<Post>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<Post>>(`${this.apiUrl}/user/${userId}`, { params });
  }

  getPost(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  createPost(request: PostCreateRequest): Observable<Post> {
    return this.http.post<Post>(this.apiUrl, request);
  }

  likePost(id: number): Observable<Post> {
    return this.http.post<Post>(`${this.apiUrl}/${id}/like`, {});
  }

  addComment(postId: number, content: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/${postId}/comments`, { content });
  }

  getComments(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/${postId}/comments`);
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
