import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { ProductService } from '../services/product.service';
import { Product } from '../services/product.model';
import { UploadImageComponent } from '../upload-image/upload-image.component';

@Component({
  selector: 'app-create-product',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatDialogModule,
  ],
  templateUrl: './create-product.component.html',
})
export class CreateProductComponent {
  form: FormGroup;
  loading = false;

  errorMessage = '';
  successMessage = ''; // ✅ FIX: added to match template

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private router: Router,
    private dialog: MatDialog
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [null, [Validators.required, Validators.min(0.01)]],
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const productData: Product = {
      ...this.form.value,
    };

    this.productService.createProduct(productData).subscribe({
      next: (res) => {
        this.loading = false;
        this.successMessage = 'Product created successfully!';
        this.form.reset();

        const dialogRef = this.dialog.open(UploadImageComponent, {
          width: '400px',
          data: { productId: res.productId },
        });

        dialogRef.afterClosed().subscribe((result) => {
          const message = result?.uploaded
            ? 'Product and image uploaded successfully!'
            : 'Product created successfully!';
          this.router.navigate(['/dashboard'], { state: { message } });
        });
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to create product.';
        this.loading = false;
      },
    });
  }
}
