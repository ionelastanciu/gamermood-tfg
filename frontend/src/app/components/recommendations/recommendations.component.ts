import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

interface GameItem {
  title:       string;
  genre:       string;
  description: string;
}

const MOOD_MESSAGES: Record<string, string> = {
  happy:   '¡Excelente sesión! Aquí hay formas de mantener ese momentum',
  neutral: 'Una sesión tranquila. Te ayudamos a encontrar lo que necesitas',
  sad:     'Entendemos tu frustración. Aquí hay consejos para mejorar'
};

const MOCK_ADVICE: Record<string, string[]> = {
  happy: [
    'Aprovecha este momento positivo para intentar desafíos más difíciles en tus juegos favoritos',
    'Comparte tu experiencia con amigos, ¡la positividad es contagiosa!',
    'Considera grabar tus mejores momentos para recordar este buen día',
    'Es el momento perfecto para probar ese juego nuevo que tenías pendiente'
  ],
  neutral: [
    'Tómate un descanso corto, estira y descansa la vista unos minutos',
    'Prueba cambiar de género de juego para refrescar tu experiencia',
    'Escucha tu música favorita mientras juegas para mejorar el ambiente',
    'Establece metas pequeñas y alcanzables para la próxima sesión'
  ],
  sad: [
    'Recuerda que todos los jugadores tienen malas rachas, es completamente normal',
    'Considera jugar algo más relajado o cooperativo con amigos',
    'Tómate un descanso más largo, sal a caminar o haz otra actividad',
    'No te obsesiones con el rendimiento, el objetivo principal es divertirse',
    'Analiza qué salió mal sin juzgarte duramente, usa esto para mejorar'
  ]
};

const MOCK_GAMES: Record<string, GameItem[]> = {
  happy: [
    { title: 'Celeste',      genre: 'Plataformas/Desafío',   description: 'Perfecto para mantener el momentum con desafíos gratificantes' },
    { title: 'Hades',        genre: 'Roguelike/Acción',       description: 'Combate fluido y progresión constante para seguir disfrutando' },
    { title: 'Rocket League', genre: 'Deportes/Competitivo',  description: 'Canaliza esa energía positiva en partidas emocionantes' },
    { title: 'It Takes Two', genre: 'Cooperativo/Aventura',   description: 'Comparte la diversión con un amigo en esta aventura única' }
  ],
  neutral: [
    { title: 'Stardew Valley', genre: 'Simulación/Relajante', description: 'Experiencia tranquila perfecta para desconectar' },
    { title: 'Hollow Knight',  genre: 'Metroidvania',          description: 'Exploración envolvente a tu propio ritmo' },
    { title: 'Slay the Spire', genre: 'Roguelike/Cartas',      description: 'Estrategia por turnos sin presión de tiempo' },
    { title: 'A Short Hike',   genre: 'Exploración/Casual',    description: 'Aventura corta y encantadora para refrescar' }
  ],
  sad: [
    { title: 'Journey',        genre: 'Aventura/Artístico',    description: 'Experiencia relajante y emocionalmente reconfortante' },
    { title: 'Animal Crossing', genre: 'Simulación Social',    description: 'Ritmo pausado y sin presión, pura relajación' },
    { title: 'Spiritfarer',    genre: 'Gestión/Narrativo',     description: 'Historia reconfortante con mecánicas relajantes' },
    { title: 'Unpacking',      genre: 'Puzzle/Zen',            description: 'Meditativo y satisfactorio, perfecto para desestresarse' }
  ]
};

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit {
  moodMessage = 'Basado en tu sesión, aquí están nuestros consejos';
  adviceList: string[]   = [];
  gamesList:  GameItem[] = [];

  feedbackSent    = false;
  feedbackUseful: boolean | null = null;

  ngOnInit(): void {
    const sessionId: number | undefined = history.state?.sessionId;

    if (sessionId) {
      // TODO: llamar a SessionService.getRecommendations(sessionId) cuando el backend esté disponible
      // this.sessionService.getRecommendations(sessionId).subscribe(recs => this.mapRecommendations(recs));
    }

    // Fallback: datos locales guardados por el formulario de sesión
    const mood = this.getMoodFromStorage();
    this.loadFromMock(mood);
  }

  private getMoodFromStorage(): string {
    const raw = localStorage.getItem('sessionData');
    if (!raw) return 'neutral';
    return JSON.parse(raw).mood ?? 'neutral';
  }

  private loadFromMock(mood: string): void {
    this.moodMessage = MOOD_MESSAGES[mood] ?? MOOD_MESSAGES['neutral'];
    this.adviceList  = MOCK_ADVICE[mood]   ?? MOCK_ADVICE['neutral'];
    this.gamesList   = MOCK_GAMES[mood]    ?? MOCK_GAMES['neutral'];
  }

  sendFeedback(useful: boolean, _comment: string): void {
    this.feedbackSent   = true;
    this.feedbackUseful = useful;

    // TODO: llamar a SessionService.sendFeedback() cuando el backend esté disponible
    // const sessionId = history.state?.sessionId;
    // if (sessionId) {
    //   this.sessionService.sendFeedback({ recommendationId: sessionId, useful, comment: _comment || undefined });
    // }
  }
}
