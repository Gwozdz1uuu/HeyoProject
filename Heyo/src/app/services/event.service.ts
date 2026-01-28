import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Event, EventCreateRequest, Page } from '../models';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private readonly apiUrl = `${environment.apiUrl}/events`;

  constructor(private http: HttpClient) {}

  getUpcomingEvents(page: number = 0, size: number = 20): Observable<Page<Event>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<Event>>(this.apiUrl, { params });
  }

  getMyEvents(page: number = 0, size: number = 20): Observable<Page<Event>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<Event>>(`${this.apiUrl}/my`, { params });
  }

  getEvent(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  createEvent(request: EventCreateRequest): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, request);
  }

  toggleInterested(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/${id}/interested`, {});
  }

  toggleParticipating(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/${id}/participate`, {});
  }
}
