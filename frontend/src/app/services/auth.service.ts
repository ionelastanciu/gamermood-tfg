import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/user.model';

const API = 'http://localhost:8081/api';
const TOKEN_KEY = 'gm_token';
const USER_KEY  = 'gm_user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private http: HttpClient) {}

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API}/auth/login`, body).pipe(
      tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        const user: User = { id: res.userId, username: res.username, email: res.email, roles: res.roles };
        localStorage.setItem(USER_KEY, JSON.stringify(user));
      })
    );
  }

  register(body: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${API}/auth/register`, body);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw) as User; } catch { return null; }
  }
}
