import { Component, ViewChild, ElementRef } from '@angular/core';
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
  @ViewChild('gameSelect') gameSelectRef!: ElementRef<HTMLSelectElement>;

  form: FormGroup;
  submitted     = false;
  isLoading     = false;
  showOtherGame = false;
  readonly intensitySegments = Array.from({ length: 10 }, (_, i) => i + 1);

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

  onGameSelect(value: string): void {
    if (value === 'other') {
      this.showOtherGame = true;
      this.form.patchValue({ game: '' });
    } else {
      this.showOtherGame = false;
      this.form.patchValue({ game: value });
    }
  }

  onOtherGameInput(value: string): void {
    this.form.patchValue({ game: value });
  }

  resetForm(): void {
<<<<<<< HEAD
    this.submitted    = false;
=======
    this.submitted     = false;
>>>>>>> feature/frontend-base-integration
    this.showOtherGame = false;
    this.form.reset({ intensity: 5 });
    if (this.gameSelectRef?.nativeElement) {
      this.gameSelectRef.nativeElement.value = '';
    }
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
        this.router.navigate(['/recommendations'], {
          state: {
            sessionId: session.id,
            mood:      session.mood,
            game:      session.game,
            intensity: session.intensity
          }
        });
      },
      error: () => {
        this.sessionService.saveLocalSession(body);
        this.router.navigate(['/recommendations'], {
          state: { mood: body.mood, game: body.game, intensity: body.intensity }
        });
      }
    });
  }
}
