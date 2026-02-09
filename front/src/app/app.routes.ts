import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Dashboard } from './dashboard/dashboard';
import { Register } from './register/register';
import { Profile } from './profile/profile';
import { AuthGuard } from './guards/auth.guard';
import { CreateProductComponent } from './create-product/create-product.component';
import { EditProduct } from './edit-product/edit-product';

export const routes: Routes = [
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
  },
  {
    path: 'create-product',
    component: CreateProductComponent,
    canActivate: [AuthGuard],
    data: { role: 'ROLE_SELLER' },
  },
  {
    path: 'edit-product/:id',
    component: EditProduct,
    canActivate: [AuthGuard],
    data: { role: 'ROLE_SELLER' },
  },
];
