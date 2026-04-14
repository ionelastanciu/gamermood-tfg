import { Routes } from '@angular/router';
import { IndexComponent } from './components/index/index.component';
import { LoginComponent } from './components/login/login.component';
import { SessionComponent } from './components/session/session.component';
import { RecommendationsComponent } from './components/recommendations/recommendations.component';
import { RegisterComponent } from './components/register/register.component';
import { RegisterSuccessComponent } from './components/register-success/register-success.component';

export const routes: Routes = [
  { path: '',               component: IndexComponent },
  { path: 'login',          component: LoginComponent },
  { path: 'register',       component: RegisterComponent },
  { path: 'register-success', component: RegisterSuccessComponent },
  { path: 'session',        component: SessionComponent },
  { path: 'recommendations', component: RecommendationsComponent },
  { path: '**',             redirectTo: '' }
];
