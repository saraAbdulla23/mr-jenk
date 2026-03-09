import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

import { CartService } from '../services/cart.service';
import { ProductService } from '../services/product.service';
import { CartResponse, CartItem } from '../models/cart.model';
import { Product } from '../services/product.model';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule
  ],
  templateUrl: './cart.html',
  styleUrls: ['./cart.scss'],
})
export class CartComponent implements OnInit {

  cart!: CartResponse;
  products: Product[] = []; // store all products for stock checking

  constructor(
    private cartService: CartService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadCart();
  }

  // Load all products to know stock
  loadProducts(): void {
    this.productService.getAllProducts().subscribe(res => {
      this.products = res;
    });
  }

  // Load cart items
  loadCart(): void {
    this.cartService.getCart().subscribe(res => {
      this.cart = res;
    });
  }

  // Get max available stock for a cart item
  getProductStock(productId: string): number {
    const product = this.products.find(p => p.productId === productId);
    return product ? product.quantity : 1;
  }

  // Update quantity with stock validation
  updateQuantity(productId: string, quantity: number): void {
    if (quantity <= 0) return;

    const maxStock = this.getProductStock(productId);

    if (quantity > maxStock) {
      alert(`Cannot add more than available stock (${maxStock})`);
      // Reset the input to max stock
      const cartItem = this.cart.items.find(i => i.productId === productId);
      if (cartItem) cartItem.quantity = maxStock;
      return;
    }

    // Proceed to update quantity in cart
    this.cartService.updateQuantity(productId, quantity)
      .subscribe(cart => this.cart = cart);
  }

  // Remove item from cart
  removeItem(productId: string): void {
    this.cartService.removeItem(productId)
      .subscribe(cart => this.cart = cart);
  }

  // Navigate to checkout
  checkout(): void {
    this.router.navigate(['/checkout']);
  }
}