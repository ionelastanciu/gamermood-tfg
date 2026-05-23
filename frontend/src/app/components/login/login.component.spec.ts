import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';

describe('Login', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authSpy: { login: ReturnType<typeof vi.fn>; getToken: ReturnType<typeof vi.fn>; isLoggedIn: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authSpy = { login: vi.fn(), getToken: vi.fn(), isLoggedIn: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('formulario vacío debe ser inválido', () => {
    expect(component.form.valid).toBe(false);
  });

  it('formulario con email y contraseña válidos debe ser válido', () => {
    component.form.setValue({ email: 'test@gamermood.com', password: 'password123' });
    expect(component.form.valid).toBe(true);
  });

  it('no debe llamar al servicio si el formulario es inválido', () => {
    component.onLogin();
    expect(authSpy.login).not.toHaveBeenCalled();
  });

  it('debe llamar a AuthService.login con los valores del formulario', () => {
    authSpy.login.mockReturnValue(of({
      token: 'jwt-token', userId: 1, username: 'gamer', email: 'test@gamermood.com', roles: ['USER']
    }));
    component.form.setValue({ email: 'test@gamermood.com', password: 'password123' });
    component.onLogin();
    expect(authSpy.login).toHaveBeenCalledWith({ email: 'test@gamermood.com', password: 'password123' });
  });

  it('debe mostrar error en credenciales incorrectas (401)', () => {
    authSpy.login.mockReturnValue(throwError(() => ({ status: 401 })));
    component.form.setValue({ email: 'wrong@test.com', password: 'wrongpass' });
    component.onLogin();
    expect(component.errorMessage).toBe('Email o contraseña incorrectos.');
  });

  it('debe mostrar error de conexión cuando el servidor no responde (0)', () => {
    authSpy.login.mockReturnValue(throwError(() => ({ status: 0 })));
    component.form.setValue({ email: 'test@test.com', password: 'password123' });
    component.onLogin();
    expect(component.errorMessage).toBe('No se pudo conectar con el servidor.');
  });

  it('isLoading debe ser false al terminar', () => {
    authSpy.login.mockReturnValue(throwError(() => ({ status: 500 })));
    component.form.setValue({ email: 'test@test.com', password: 'password123' });
    component.onLogin();
    expect(component.isLoading).toBe(false);
  });
});
