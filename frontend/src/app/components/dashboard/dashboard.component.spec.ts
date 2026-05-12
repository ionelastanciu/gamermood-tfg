import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../services/auth.service';
import { SessionService } from '../../services/session.service';
import { SessionResponse } from '../../models/session.model';

describe('Dashboard', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let authSpy: { logout: ReturnType<typeof vi.fn> };
  let sessionSpy: { getSessions: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authSpy = { logout: vi.fn() };
    sessionSpy = { getSessions: vi.fn().mockReturnValue(of([])) };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authSpy },
        { provide: SessionService, useValue: sessionSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('llama a getSessions al iniciar', () => {
    expect(sessionSpy.getSessions).toHaveBeenCalled();
  });

  it('sessions se rellena con los datos del servicio', () => {
    const mockSessions: SessionResponse[] = [
      { id: 1, game: 'Valorant', mood: 'happy', intensity: 7, experience: 'Fun', createdAt: '2024-01-01' }
    ];
    sessionSpy.getSessions.mockReturnValue(of(mockSessions));
    component.ngOnInit();
    expect(component.sessions).toEqual(mockSessions);
  });

  it('sessions queda vacío si el servicio falla', () => {
    sessionSpy.getSessions.mockReturnValue(throwError(() => new Error('Network error')));
    component.ngOnInit();
    expect(component.sessions).toEqual([]);
  });

  it('formatDate devuelve una cadena de fecha en español', () => {
    const result = component.formatDate('2024-06-15T10:00:00');
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });

  it('logout llama a AuthService.logout y navega a /login', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    component.logout();
    expect(authSpy.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
