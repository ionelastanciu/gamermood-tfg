import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionRequest, SessionResponse } from '../models/session.model';
import { Recommendation, FeedbackRequest } from '../models/recommendation.model';
import { AuthService } from './auth.service';

const API          = 'http://localhost:8081/api';
const SESSION_KEY  = 'gm_session';

@Injectable({ providedIn: 'root' })
export class SessionService {

  constructor(private http: HttpClient, private auth: AuthService) {}

  // --- Fallback local (sin backend) ---

  saveLocalSession(data: SessionRequest): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(data));
  }

  getLocalSession(): SessionRequest | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw) as SessionRequest; } catch { return null; }
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  createSession(body: SessionRequest): Observable<SessionResponse> {
    return this.http.post<SessionResponse>(`${API}/sessions`, body, { headers: this.headers() });
  }

  getSessions(): Observable<SessionResponse[]> {
    return this.http.get<SessionResponse[]>(`${API}/sessions`, { headers: this.headers() });
  }

  getRecommendation(sessionId: number): Observable<Recommendation> {
    return this.http.post<Recommendation>(`${API}/recommendations/${sessionId}`, {}, { headers: this.headers() });
  }

  sendFeedback(recommendationId: number, body: FeedbackRequest): Observable<void> {
    return this.http.post<void>(`${API}/feedback/${recommendationId}`, body, { headers: this.headers() });
  }

  retryRecommendation(sessionId: number): Observable<Recommendation> {
    return this.http.post<Recommendation>(`${API}/recommendations/${sessionId}/retry`, {}, { headers: this.headers() });
  }
}
