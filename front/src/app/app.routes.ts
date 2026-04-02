import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Dashboard } from './dashboard/dashboard';
import { Register } from './register/register';
import { Profile } from './profile/profile';
import { AdminUsers } from './admin-users/admin-users';
import { ManageTravel } from './manage-travel/manage-travel';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  // ✅ Redirect root to login
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    component: Login,
  },
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [AuthGuard],
  },
  {
    path: 'register',
    component: Register,
  },
  {
    path: 'profile',
    component: Profile,
    canActivate: [AuthGuard],
  },

  {
    path: 'admin-users',
    component: AdminUsers,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' },
  },

  {
    path: 'manage-travel',
    component: ManageTravel,
    canActivate: [AuthGuard],
    data: { role: 'ADMIN' },
  },
  
];
