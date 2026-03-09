import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ProductService } from '../services/product.service';
import { MediaService } from '../services/media.service';
import { CartService } from '../services/cart.service';
import { Product } from '../services/product.model';
import { TokenStorageService } from '../services/token-storage.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,          // for ngModel
    MatCardModule,
    MatButtonModule,
    MatBadgeModule,
    MatIconModule,
    MatFormFieldModule,   // <-- add this
    MatInputModule        // <-- add this
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard implements OnInit {
  products: Product[] = [];
  allProducts: Product[] = []; // full unfiltered list
  loading = true;
  error = '';
  successMessage = '';
  user: any;

  productImages: Record<string, string[]> = {};
  productImageIndex: Record<string, number> = {};

  cartItemCount = 0;

  // ================= SEARCH & FILTER =================
  searchKeyword: string = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;

  constructor(
    private productService: ProductService,
    private mediaService: MediaService,
    private cartService: CartService,
    private router: Router,
    private tokenStore: TokenStorageService
  ) {}

  ngOnInit(): void {
    this.user = this.tokenStore.getUser();

    this.tokenStore.user$.subscribe(user => {
      if (user) {
        this.user = user;
        if (user.role === 'CLIENT') {
          this.loadCartCount();
        }
      }
    });

    // Subscribe to cart changes globally (auto-update badge)
    this.cartService.cartItemsCount$.subscribe(count => {
      this.cartItemCount = count;
    });

    this.loadProducts();
  }

  // ================= LOAD PRODUCTS =================
  loadProducts(): void {
    this.loading = true;

    this.productService.getAllProducts().subscribe({
      next: products => {
        this.allProducts = products || [];
        this.products = [...this.allProducts]; // initialize filtered list
        this.loadImagesForProducts();
      },
      error: err => {
        console.error(err);
        this.error = 'Failed to load products';
        this.loading = false;
      }
    });
  }

  // ================= LOAD MEDIA =================
  private loadImagesForProducts(): void {
    if (!this.products.length) {
      this.loading = false;
      return;
    }

    const requests = this.products.map(product =>
      this.mediaService.getImagesByProduct(product.productId).pipe(
        catchError(() => of({ images: [], count: 0 }))
      )
    );

    forkJoin(requests).subscribe(results => {
      results.forEach((res, index) => {
        const productId = this.products[index].productId;
        this.productImages[productId] = res.images?.length
          ? res.images.map(img => img.imagePath)
          : [];
        this.productImageIndex[productId] = 0;
      });
      this.loading = false;
    });
  }

  // ================= IMAGE CAROUSEL =================
  nextImage(productId: string): void {
    const images = this.productImages[productId];
    if (!images?.length) return;
    this.productImageIndex[productId] =
      (this.productImageIndex[productId] + 1) % images.length;
  }

  prevImage(productId: string): void {
    const images = this.productImages[productId];
    if (!images?.length) return;
    this.productImageIndex[productId] =
      (this.productImageIndex[productId] - 1 + images.length) % images.length;
  }

  goToImage(productId: string, index: number): void {
    this.productImageIndex[productId] = index;
  }

  // ================= SEARCH & FILTER LOGIC =================
  applyFilters(): void {
    this.products = this.allProducts.filter(product => {
      const matchesKeyword = !this.searchKeyword ||
        product.name.toLowerCase().includes(this.searchKeyword.toLowerCase());
      const matchesMinPrice = this.minPrice == null || product.price >= this.minPrice;
      const matchesMaxPrice = this.maxPrice == null || product.price <= this.maxPrice;

      return matchesKeyword && matchesMinPrice && matchesMaxPrice;
    });
  }

  // ================= ADD TO CART =================
  addToCart(product: any): void {
    if (!this.user) {
      alert('You must be logged in to add items to cart.');
      return;
    }

    const item = {
      productId: product.productId,
      name: product.name,
      price: product.price,
      sellerId: product.userId,
      quantity: 1,
    };

    this.cartService.addToCart(item).subscribe({
      next: () => {
        this.successMessage = `"${product.name}" added to cart!`;
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        console.error('Add to cart failed', err);
        if (err.status === 401) {
          alert('Unauthorized. Please login first.');
          this.router.navigate(['/login']);
        } else if (err.status === 400) {
          alert('Invalid request: ' + (err.error?.message || ''));
        } else {
          alert('Failed to add to cart. Try again.');
        }
      },
    });
  }

  loadCartCount(): void {
    this.cartService.getCart().subscribe({
      next: cart => {
        this.cartItemCount = cart?.items?.length || 0;
      },
      error: err => {
        console.error('Failed to load cart', err);
      }
    });
  }

  // ================= NAVIGATION =================
  editProduct(product: Product): void {
    this.router.navigate(['/edit-product', product.productId]);
  }

  deleteProduct(product: Product): void {
    if (!confirm(`Are you sure you want to delete "${product.name}"?`)) return;

    this.productService.deleteProduct(product.productId).subscribe({
      next: () => {
        this.products = this.products.filter(p => p.productId !== product.productId);
        this.allProducts = this.allProducts.filter(p => p.productId !== product.productId);
        this.successMessage = `Product deleted`;
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: err => {
        console.error(err);
        alert('Failed to delete product');
      }
    });
  }

  goToCreateProduct(): void {
    this.router.navigate(['/create-product']);
  }

  goToProfile(): void {
    this.router.navigate(['/profile']);
  }

  goToCart(): void {
    this.router.navigate(['/cart']);
  }
  goToSellerDashboard(): void {
    this.router.navigate(['/seller/dashboard']);
  }
  
  goToUserDashboard(): void {
    this.router.navigate(['/user/dashboard']);
  }

  logout(): void {
    this.tokenStore.logout();
    this.router.navigate(['/login']);
  }
}