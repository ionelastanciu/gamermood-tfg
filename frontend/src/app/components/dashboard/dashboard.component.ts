import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { SessionService } from '../../services/session.service';
import { SessionResponse } from '../../models/session.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  sessions:     SessionResponse[] = [];
  loadError     = false;
  isLoadingSessions = false;

  readonly moodLabels: Record<string, string> = {
    happy:   'Genial',
    neutral: 'Normal',
    sad:     'Frustrado'
  };

  constructor(
    private auth:           AuthService,
    private sessionService: SessionService,
    private router:         Router,
    private cdr:            ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadSessions();
  }

  loadSessions(): void {
    this.loadError        = false;
    this.isLoadingSessions = true;
    this.sessionService.getSessions().pipe(
      catchError((err) => {
        if (err.status === 401 || err.status === 403) {
          this.auth.logout();
          this.router.navigate(['/login']);
        }
        this.loadError = true;
        return of([] as SessionResponse[]);
      })
    ).subscribe(data => {
      this.sessions          = data;
      this.isLoadingSessions = false;
      this.cdr.detectChanges();
    });
  }

  deleteSession(id: number): void {
    if (!confirm('¿Eliminar esta sesión?')) return;
    this.sessionService.deleteSession(id).subscribe({
      next: () => {
        this.sessions = this.sessions.filter(s => s.id !== id);
        this.cdr.detectChanges();
      },
      error: () => alert('No se pudo eliminar la sesión.')
    });
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('es-ES', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
