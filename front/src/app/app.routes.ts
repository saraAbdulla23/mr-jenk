import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Dashboard } from './dashboard/dashboard';
import { Register } from './register/register';
import { Profile } from './profile/profile';
import { CartComponent } from './cart/cart';
import { CheckoutComponent } from './checkout/checkout'; // ✅ Add this
import { AuthGuard } from './guards/auth.guard';
import { CreateProductComponent } from './create-product/create-product.component';
import { EditProduct } from './edit-product/edit-product';
import { OrderComponent } from './order/order';
import { SellerOrdersComponent } from './seller-orders/seller-orders';
import { SellerDashboardComponent } from './seller-dashboard/seller-dashboard';
import { UserDashboardComponent } from './user-dashboard/user-dashboard';

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
    data: { role: 'SELLER' },
  },
  {
    path: 'edit-product/:id',
    component: EditProduct,
    canActivate: [AuthGuard],
    data: { role: 'SELLER' },
  },
  {
    path: 'cart',
    component: CartComponent,
    canActivate: [AuthGuard],
    data: { role: 'CLIENT' }
  },
  {
    path: 'checkout',               // ✅ New checkout wizard route
    component: CheckoutComponent,
    canActivate: [AuthGuard],
    data: { role: 'CLIENT' }
  },
  {
    path: 'orders',                // ✅ New route for user orders
    component: OrderComponent,
    canActivate: [AuthGuard],
    data: { role: 'CLIENT' }       // only clients can see their orders
  },
  {
    path: 'seller/orders',
    component: SellerOrdersComponent,
    canActivate: [AuthGuard],
    data: { role: 'SELLER' }
  },
  {
    path: 'seller/dashboard',
    component: SellerDashboardComponent,
    canActivate: [AuthGuard],
    data: { role: 'SELLER' }
  },
  {
    path: 'user/dashboard',
    component: UserDashboardComponent,
    canActivate: [AuthGuard],
    data: { role: 'CLIENT' }
  }
];