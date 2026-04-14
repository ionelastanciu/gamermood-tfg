import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/user.model';

const API = 'http://localhost:8081/api';
const TOKEN_KEY = 'gm_token';

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private http: HttpClient) {}

  login(body: LoginRequest): Observable<AuthResponse> {
    // TODO: endpoint POST /api/auth/login (pendiente de backend)
    return this.http.post<AuthResponse>(`${API}/auth/login`, body).pipe(
      tap(res => localStorage.setItem(TOKEN_KEY, res.token))
    );
  }

  register(body: RegisterRequest): Observable<void> {
    // TODO: endpoint POST /api/auth/register (pendiente de backend)
    return this.http.post<void>(`${API}/auth/register`, body);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
