import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../services/session.service';

interface GameItem {
  title:       string;
  genre:       string;
  description: string;
}

const MOOD_LABELS: Record<string, string> = {
  happy:   'GENIAL',
  neutral: 'NORMAL',
  sad:     'FRUSTRADO'
};

const RESULT_TAGS: Record<string, string> = {
  happy:   '// VICTORIA //',
  neutral: '// SESIÓN COMPLETA //',
  sad:     '// ANÁLISIS COMPLETO //'
};

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
    { title: 'Celeste',       genre: 'Plataformas / Desafío',  description: 'Perfecto para mantener el momentum con desafíos gratificantes' },
    { title: 'Hades',         genre: 'Roguelike / Acción',     description: 'Combate fluido y progresión constante para seguir disfrutando' },
    { title: 'Rocket League', genre: 'Deportes / Competitivo', description: 'Canaliza esa energía positiva en partidas emocionantes' },
    { title: 'It Takes Two',  genre: 'Cooperativo / Aventura', description: 'Comparte la diversión con un amigo en esta aventura única' }
  ],
  neutral: [
    { title: 'Stardew Valley', genre: 'Simulación / Relajante', description: 'Experiencia tranquila perfecta para desconectar' },
    { title: 'Hollow Knight',  genre: 'Metroidvania',            description: 'Exploración envolvente a tu propio ritmo' },
    { title: 'Slay the Spire', genre: 'Roguelike / Cartas',     description: 'Estrategia por turnos sin presión de tiempo' },
    { title: 'A Short Hike',   genre: 'Exploración / Casual',   description: 'Aventura corta y encantadora para refrescar' }
  ],
  sad: [
    { title: 'Journey',         genre: 'Aventura / Artístico', description: 'Experiencia relajante y emocionalmente reconfortante' },
    { title: 'Animal Crossing', genre: 'Simulación Social',    description: 'Ritmo pausado y sin presión, pura relajación' },
    { title: 'Spiritfarer',     genre: 'Gestión / Narrativo',  description: 'Historia reconfortante con mecánicas relajantes' },
    { title: 'Unpacking',       genre: 'Puzzle / Zen',         description: 'Meditativo y satisfactorio, perfecto para desestresarse' }
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
  moodMessage      = '';
  moodLabel        = 'NORMAL';
  resultTag        = '// SESIÓN COMPLETA //';
  currentMood      = 'neutral';
  sessionGame      = '';
  sessionIntensity = 5;
  adviceList: string[]   = [];
  gamesList:  GameItem[] = [];

  sessionId:       number | null = null;
  recommendationId: number | null = null;
  feedbackSent    = false;
  feedbackUseful: boolean | null = null;
  retrying        = false;
  loadingRecommendation = false;

  readonly intensitySegments = Array.from({ length: 10 }, (_, i) => i + 1);
  readonly flippedCards = new Set<number>();

  constructor(private sessionService: SessionService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    const state     = history.state ?? {};
    const sid: number | undefined = state.sessionId;
    const mood      = state.mood      ?? this.sessionService.getLocalSession()?.mood      ?? 'neutral';
    const game      = state.game      ?? this.sessionService.getLocalSession()?.game      ?? '';
    const intensity = state.intensity ?? this.sessionService.getLocalSession()?.intensity ?? 5;

    this.initDisplay(mood, game, intensity, !sid);

    if (sid) {
      this.sessionId = sid;
      this.loadingRecommendation = true;
      this.sessionService.getRecommendation(sid).subscribe({
        next: (rec) => {
          this.recommendationId = rec.id;
          this.adviceList = this.parseRecommendations(rec.texto);
          this.loadingRecommendation = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.adviceList = this.getFallbackAdvice(this.currentMood);
          this.loadingRecommendation = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  retryRecommendation(): void {
    if (!this.sessionId) return;
    this.retrying = true;
    this.loadingRecommendation = true;
    this.sessionService.retryRecommendation(this.sessionId).subscribe({
      next: (rec) => {
        this.recommendationId = rec.id;
        this.adviceList       = this.parseRecommendations(rec.texto);
        this.feedbackSent     = false;
        this.feedbackUseful   = null;
        this.retrying         = false;
        this.loadingRecommendation = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.retrying = false;
        this.loadingRecommendation = false;
        this.cdr.detectChanges();
      }
    });
  }

  private initDisplay(mood: string, game: string, intensity: number, showFallbackAdvice = true): void {
    this.currentMood      = mood;
    this.sessionGame      = game;
    this.sessionIntensity = intensity;
    this.moodLabel        = MOOD_LABELS[mood]    ?? 'NORMAL';
    this.resultTag        = RESULT_TAGS[mood]     ?? RESULT_TAGS['neutral'];
    this.moodMessage      = MOOD_MESSAGES[mood]   ?? MOOD_MESSAGES['neutral'];
    this.adviceList       = showFallbackAdvice ? this.getFallbackAdvice(mood) : [];
    this.gamesList        = MOCK_GAMES[mood]      ?? MOCK_GAMES['neutral'];
  }

  private getFallbackAdvice(mood: string): string[] {
    return MOCK_ADVICE[mood] ?? MOCK_ADVICE['neutral'];
  }

  private parseRecommendations(texto: string): string[] {
    const lines = texto.split('\n');
    const items: string[] = [];
    let current = '';

    for (const line of lines) {
      const match = line.match(/^\s*\d+\.\s+(.+)/);
      if (match) {
        if (current.trim()) items.push(current.trim());
        current = match[1];
      } else if (line.trim() && current) {
        current += ' ' + line.trim();
      }
    }
    if (current.trim()) items.push(current.trim());

    return items.length >= 2 ? items : [texto.trim()];
  }

  padNum(n: number): string {
    return n.toString().padStart(2, '0');
  }

  toggleCard(index: number): void {
    if (this.flippedCards.has(index)) {
      this.flippedCards.delete(index);
    } else {
      this.flippedCards.add(index);
    }
  }

  gameCardClass(i: number): string {
    const base = `game-card game-card-mood-${this.currentMood} game-card-interactive anim-scale-in`;
    return this.flippedCards.has(i) ? base + ' card-open' : base;
  }

  sendFeedback(useful: boolean, comment: string): void {
    this.feedbackSent   = true;
    this.feedbackUseful = useful;

    if (this.recommendationId !== null) {
      this.sessionService.sendFeedback(this.recommendationId, {
        util: useful,
        comentario: comment || undefined
      }).subscribe();
    }
  }
}
