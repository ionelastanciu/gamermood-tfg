import { Component, OnInit } from '@angular/core';
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
  sessions: SessionResponse[] = [];

  readonly moodLabels: Record<string, string> = {
    happy:   'Genial',
    neutral: 'Normal',
    sad:     'Frustrado'
  };

  constructor(
    private auth:           AuthService,
    private sessionService: SessionService,
    private router:         Router
  ) {}

  ngOnInit(): void {
    this.sessionService.getSessions().pipe(
      catchError(() => of([] as SessionResponse[]))
    ).subscribe(data => { this.sessions = data; });
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
