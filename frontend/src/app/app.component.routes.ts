import { Routes } from '@angular/router';
import { IndexComponent } from './components/index/index.component';
import { LoginComponent } from './components/login/login.component';
import { SessionComponent } from './components/session/session.component';
import { RecommendationsComponent } from './components/recommendations/recommendations.component';
import { RegisterComponent } from './components/register/register.component';
import { RegisterSuccessComponent } from './components/register-success/register-success.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '',                 component: IndexComponent },
  { path: 'login',            component: LoginComponent },
  { path: 'register',         component: RegisterComponent },
  { path: 'register-success', component: RegisterSuccessComponent },
  { path: 'dashboard',        component: DashboardComponent,       canActivate: [authGuard] },
  { path: 'session',          component: SessionComponent,         canActivate: [authGuard] },
  { path: 'recommendations',  component: RecommendationsComponent, canActivate: [authGuard] },
  { path: '**',               redirectTo: '' }
];
