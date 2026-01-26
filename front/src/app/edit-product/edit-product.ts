import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { Router, ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';

import { ProductService } from '../services/product.service';
import { MediaService } from '../services/media.service';
import { Product } from '../services/product.model';
import { Media } from '../services/media.model';

@Component({
  selector: 'app-edit-product',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule],
  templateUrl: './edit-product.html',
  styleUrls: ['./edit-product.scss'],
})
export class EditProduct implements OnInit {
  product: Product = {
    productId: '',
    name: '',
    price: 0,
    quantity: 0,
    userId: '',
    description: '',
  };

  loading = true;
  error = '';
  user: any;
  successMessage = '';

  images: Media[] = [];
  newFiles: File[] = [];
  uploading = false;

  constructor(
    private productService: ProductService,
    private mediaService: MediaService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const storedUser = localStorage.getItem('user');
    this.user = storedUser ? JSON.parse(storedUser) : null;

    const productId = this.route.snapshot.paramMap.get('id');
    if (!productId) {
      this.error = 'No product ID provided';
      this.loading = false;
      return;
    }

    this.productService.getAllProducts().subscribe({
      next: products => {
        const found = products.find(p => p.productId === productId);
        if (!found) {
          this.error = 'Product not found';
          this.loading = false;
          return;
        }

        this.product = found;
        this.loadImages();
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.error = 'Failed to load product';
        this.loading = false;
      }
    });
  }

  // ================= MEDIA =================
  loadImages(): void {
    this.mediaService.getImagesByProduct(this.product.productId).subscribe({
      next: res => this.images = res.images || [],
      error: err => console.error('Failed to load images', err),
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    Array.from(input.files).forEach(file => this.newFiles.push(file));
  }

  uploadImages(): void {
    if (!this.newFiles.length) return;

    this.uploading = true;

    const uploads = this.newFiles.map(file =>
      this.mediaService.uploadMedia(file, this.product.productId)
    );

    forkJoin(uploads).subscribe({
      next: responses => {
        const uploaded = responses.map(r => r.media);
        this.images.push(...uploaded);
        this.newFiles = [];
        this.uploading = false;
      },
      error: err => {
        console.error('Upload failed', err);
        this.uploading = false;
      }
    });
  }

  deleteImage(media: Media): void {
    if (!confirm('Delete this image?')) return;

    this.mediaService.deleteMedia(media).subscribe({
      next: () => {
        this.images = this.images.filter(img => img.id !== media.id);
      },
      error: err => {
        console.error(err);
        alert('Failed to delete image');
      }
    });
  }

  // ================= PRODUCT =================
  save(): void {
    this.productService.updateProduct(this.product.productId, this.product).subscribe({
      next: () => {
        this.router.navigate(['/dashboard'], {
          state: { message: 'Product updated successfully!' }
        });
      },
      error: err => {
        console.error(err);
        alert('Failed to update product');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }
}
