import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../services/session.service';
import { SessionRequest } from '../../models/session.model';

@Component({
  selector: 'app-session',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './session.component.html',
  styleUrls: ['./session.component.css']
})
export class SessionComponent {
  form: FormGroup;
  submitted = false;
  isLoading = false;

  constructor(
    private fb:             FormBuilder,
    private sessionService: SessionService,
    private router:         Router
  ) {
    this.form = this.fb.group({
      game:       ['', Validators.required],
      mood:       ['', Validators.required],
      intensity:  [5],
      experience: ['', Validators.required]
    });
  }

  get f() { return this.form.controls; }

  selectMood(mood: string): void {
    this.form.patchValue({ mood });
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.invalid) return;

    const body: SessionRequest = {
      game:       this.form.value.game,
      mood:       this.form.value.mood as 'happy' | 'neutral' | 'sad',
      intensity:  +this.form.value.intensity,
      experience: this.form.value.experience
    };

    this.isLoading = true;
    this.sessionService.createSession(body).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (session) => {
        this.router.navigate(['/recommendations'], { state: { sessionId: session.id } });
      },
      error: () => {
        // Backend no disponible: guardar datos localmente y continuar el flujo
        localStorage.setItem('sessionData', JSON.stringify(body));
        this.router.navigate(['/recommendations']);
      }
    });
  }
}
