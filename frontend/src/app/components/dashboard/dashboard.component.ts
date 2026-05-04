import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SessionResponse } from '../../models/session.model';

const MOCK_SESSIONS: SessionResponse[] = [
  {
    id: 1,
    game: 'Valorant',
    mood: 'sad',
    intensity: 8,
    experience: 'Muchas derrotas seguidas, empecé a tiltar bastante. Necesito un descanso.',
    createdAt: '2026-05-03T22:15:00Z'
  },
  {
    id: 2,
    game: 'Minecraft',
    mood: 'happy',
    intensity: 6,
    experience: 'Buena sesión creativa, muy relajante y productiva. Acabé mi base.',
    createdAt: '2026-05-02T19:30:00Z'
  },
  {
    id: 3,
    game: 'League of Legends',
    mood: 'neutral',
    intensity: 5,
    experience: 'Partida mediocre, sin emociones fuertes ni en un sentido ni en otro.',
    createdAt: '2026-05-01T21:00:00Z'
  }
];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  // TODO: reemplazar con llamada real a SessionService.getSessions()
  sessions: SessionResponse[] = MOCK_SESSIONS;

  readonly moodLabels: Record<string, string> = {
    happy:   'Genial',
    neutral: 'Normal',
    sad:     'Frustrado'
  };

  constructor(private auth: AuthService, private router: Router) {}

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
