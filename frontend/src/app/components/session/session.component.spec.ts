import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SessionComponent } from './session.component';
import { SessionService } from '../../services/session.service';

describe('Session', () => {
  let component: SessionComponent;
  let fixture: ComponentFixture<SessionComponent>;
  let sessionSpy: {
    createSession: ReturnType<typeof vi.fn>;
    saveLocalSession: ReturnType<typeof vi.fn>;
    getLocalSession: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    sessionSpy = {
      createSession: vi.fn(),
      saveLocalSession: vi.fn(),
      getLocalSession: vi.fn().mockReturnValue(null)
    };

    await TestBed.configureTestingModule({
      imports: [SessionComponent],
      providers: [
        provideRouter([]),
        { provide: SessionService, useValue: sessionSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('formulario vacío debe ser inválido', () => {
    expect(component.form.valid).toBe(false);
  });

  it('selectMood actualiza el campo mood', () => {
    component.selectMood('happy');
    expect(component.form.value.mood).toBe('happy');
  });

  it('onGameSelect con un juego actualiza el campo game', () => {
    component.onGameSelect('Valorant');
    expect(component.form.value.game).toBe('Valorant');
    expect(component.showOtherGame).toBe(false);
  });

  it('onGameSelect con "other" activa showOtherGame y limpia game', () => {
    component.onGameSelect('other');
    expect(component.showOtherGame).toBe(true);
    expect(component.form.value.game).toBe('');
  });

  it('onOtherGameInput actualiza el campo game', () => {
    component.onOtherGameInput('Mi Juego Favorito');
    expect(component.form.value.game).toBe('Mi Juego Favorito');
  });

  it('resetForm limpia el formulario y resetea estados', () => {
    component.form.setValue({ game: 'Valorant', mood: 'happy', intensity: 8, experience: 'Buena sesión' });
    component.submitted = true;
    component.showOtherGame = true;
    component.resetForm();
    expect(component.form.value.game).toBeNull();
    expect(component.submitted).toBe(false);
    expect(component.showOtherGame).toBe(false);
  });

  it('onSubmit con formulario inválido no llama a createSession', () => {
    component.onSubmit();
    expect(sessionSpy.createSession).not.toHaveBeenCalled();
  });

  it('onSubmit con formulario válido llama a createSession y navega', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    sessionSpy.createSession.mockReturnValue(of({
      id: 1, game: 'Valorant', mood: 'happy', intensity: 7, experience: 'Fun', createdAt: '2024-01-01'
    }));

    component.form.setValue({ game: 'Valorant', mood: 'happy', intensity: 7, experience: 'Fun session' });
    component.onSubmit();

    expect(sessionSpy.createSession).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(
      ['/recommendations'],
      expect.objectContaining({ state: expect.any(Object) })
    );
  });

  it('onSubmit navega igualmente si createSession falla (modo offline)', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    sessionSpy.createSession.mockReturnValue(throwError(() => new Error('Network error')));

    component.form.setValue({ game: 'Valorant', mood: 'happy', intensity: 7, experience: 'Fun session' });
    component.onSubmit();

    expect(sessionSpy.saveLocalSession).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/recommendations'], expect.any(Object));
  });
});
