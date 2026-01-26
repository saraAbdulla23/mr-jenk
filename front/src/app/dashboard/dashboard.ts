import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ProductService } from '../services/product.service';
import { MediaService } from '../services/media.service';
import { Product } from '../services/product.model';
import { TokenStorageService } from '../services/token-storage.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
})
export class Dashboard implements OnInit {
  products: Product[] = [];
  loading = true;
  error = '';
  successMessage = '';

  user: any;

  productImages: Record<string, string[]> = {};
  productImageIndex: Record<string, number> = {};

  constructor(
    private productService: ProductService,
    private mediaService: MediaService,
    private router: Router,
    private tokenStore: TokenStorageService
  ) {}

  ngOnInit(): void {
    const storedUser = localStorage.getItem('user');
    this.user = storedUser ? JSON.parse(storedUser) : null;

    if (history.state?.message) {
      this.successMessage = history.state.message;
      setTimeout(() => (this.successMessage = ''), 5000);
    }

    this.loadProducts();
  }

  // ================= LOAD PRODUCTS =================
  loadProducts(): void {
    this.loading = true;

    this.productService.getAllProducts().subscribe({
      next: products => {
        this.products = products || [];
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

        // Only set images if available
        this.productImages[productId] =
          res.images.length > 0
            ? res.images.map(img => img.imagePath)
            : []; // leave empty if no image

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

  // ================= PRODUCT ACTIONS =================
  editProduct(product: Product): void {
    this.router.navigate(['/edit-product', product.productId]);
  }

  deleteProduct(product: Product): void {
    if (!confirm(`Are you sure you want to delete "${product.name}"?`)) return;

    this.productService.deleteProduct(product.productId).subscribe({
      next: () => {
        this.products = this.products.filter(p => p.productId !== product.productId);
        delete this.productImages[product.productId];
        delete this.productImageIndex[product.productId];

        this.successMessage = `Product "${product.name}" deleted successfully`;
        setTimeout(() => (this.successMessage = ''), 5000);
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

  logout(): void {
    this.tokenStore.logout();
    this.router.navigate(['/login']);
  }
}
