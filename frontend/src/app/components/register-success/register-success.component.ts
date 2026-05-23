import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-register-success',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './register-success.component.html',
  styleUrls: ['./register-success.component.css']
})
export class RegisterSuccessComponent implements OnInit, OnDestroy {
  countdown = 5;
  private interval?: ReturnType<typeof setInterval>;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.interval = setInterval(() => {
      this.countdown--;
      if (this.countdown === 0) this.router.navigate(['/login']);
    }, 1000);
  }

  ngOnDestroy(): void {
    clearInterval(this.interval);
  }
}
