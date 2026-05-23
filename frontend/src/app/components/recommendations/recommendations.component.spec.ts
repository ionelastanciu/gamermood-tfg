import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RecommendationsComponent } from './recommendations.component';
import { SessionService } from '../../services/session.service';

describe('Recommendations', () => {
  let component: RecommendationsComponent;
  let fixture: ComponentFixture<RecommendationsComponent>;
  let sessionSpy: {
    getRecommendation: ReturnType<typeof vi.fn>;
    sendFeedback: ReturnType<typeof vi.fn>;
    retryRecommendation: ReturnType<typeof vi.fn>;
    getLocalSession: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    sessionSpy = {
      getRecommendation: vi.fn(),
      sendFeedback: vi.fn().mockReturnValue(of(undefined)),
      retryRecommendation: vi.fn(),
      getLocalSession: vi.fn().mockReturnValue(null)
    };

    await TestBed.configureTestingModule({
      imports: [RecommendationsComponent],
      providers: [
        provideRouter([]),
        { provide: SessionService, useValue: sessionSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RecommendationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('inicializa con mood neutral por defecto cuando no hay estado en el router', () => {
    expect(component.currentMood).toBe('neutral');
  });

  it('no llama a getRecommendation si no hay sessionId en el estado', () => {
    expect(sessionSpy.getRecommendation).not.toHaveBeenCalled();
  });

  it('sendFeedback marca feedbackSent y feedbackUseful', () => {
    component.sendFeedback(true, '');
    expect(component.feedbackSent).toBe(true);
    expect(component.feedbackUseful).toBe(true);
  });

  it('sendFeedback llama al servicio cuando hay recommendationId', () => {
    component.recommendationId = 42;
    component.sendFeedback(true, 'Muy útil');
    expect(sessionSpy.sendFeedback).toHaveBeenCalledWith(42, { util: true, comentario: 'Muy útil' });
  });

  it('sendFeedback no llama al servicio si recommendationId es null', () => {
    component.recommendationId = null;
    component.sendFeedback(false, '');
    expect(sessionSpy.sendFeedback).not.toHaveBeenCalled();
  });

  it('retryRecommendation no hace nada si sessionId es null', () => {
    component.sessionId = null;
    component.retryRecommendation();
    expect(sessionSpy.retryRecommendation).not.toHaveBeenCalled();
  });

  it('retryRecommendation llama al servicio y actualiza adviceList', () => {
    component.sessionId = 10;
    sessionSpy.retryRecommendation.mockReturnValue(of({
      id: 2, sesionId: 10, texto: 'Nueva recomendación generada', fuente: 'GROQ', createdAt: '2024-01-01'
    }));
    component.retryRecommendation();
    expect(sessionSpy.retryRecommendation).toHaveBeenCalledWith(10);
    expect(component.adviceList).toEqual(['Nueva recomendación generada']);
    expect(component.feedbackSent).toBe(false);
    expect(component.retrying).toBe(false);
  });

  it('retryRecommendation restablece retrying en caso de error', () => {
    component.sessionId = 10;
    sessionSpy.retryRecommendation.mockReturnValue(throwError(() => new Error('Error')));
    component.retryRecommendation();
    expect(component.retrying).toBe(false);
  });
});
