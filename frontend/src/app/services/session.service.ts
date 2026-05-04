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

  getLocalMood(): string {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return 'neutral';
    return (JSON.parse(raw) as SessionRequest).mood ?? 'neutral';
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  createSession(body: SessionRequest): Observable<SessionResponse> {
    // TODO: endpoint POST /api/sessions (pendiente de backend)
    return this.http.post<SessionResponse>(`${API}/sessions`, body, { headers: this.headers() });
  }

  getSessions(): Observable<SessionResponse[]> {
    // TODO: endpoint GET /api/sessions (pendiente de backend)
    return this.http.get<SessionResponse[]>(`${API}/sessions`, { headers: this.headers() });
  }

  getRecommendations(sessionId: number): Observable<Recommendation[]> {
    // TODO: endpoint GET /api/sessions/{id}/recommendations (pendiente de backend)
    return this.http.get<Recommendation[]>(`${API}/sessions/${sessionId}/recommendations`, { headers: this.headers() });
  }

  sendFeedback(body: FeedbackRequest): Observable<void> {
    // TODO: endpoint POST /api/feedback (pendiente de backend)
    return this.http.post<void>(`${API}/feedback`, body, { headers: this.headers() });
  }
}
