import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-session',
  standalone: true,
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './session.component.html',
  styleUrls: ['./session.component.css']
})
export class SessionComponent {
  selectedMood: string = '';
  game: string = '';
  intensity: number = 5;
  experience: string = '';

  constructor(private router: Router) {}

  selectMood(mood: string) {
    this.selectedMood = mood;
  }

  onSubmit() {
    if (!this.selectedMood) {
      alert('Por favor, selecciona cómo te sientes');
      return;
    }

    const sessionData = {
      mood: this.selectedMood,
      game: this.game,
      intensity: this.intensity,
      experience: this.experience
    };

    // Guardar en localStorage (ya que sessionStorage no está disponible en artifacts)
    localStorage.setItem('sessionData', JSON.stringify(sessionData));
    
    this.router.navigate(['/recommendations']);
  }
}