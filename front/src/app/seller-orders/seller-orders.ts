import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select'; 
import { MatIconModule } from '@angular/material/icon'; 
import { OrderService } from '../services/order.service';
import { Order, OrderItem } from '../services/order.model';

@Component({
  selector: 'app-seller-orders',
  standalone: true,
  templateUrl: './seller-orders.html',
  styleUrls: ['./seller-orders.scss'],
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule
  ]
})
export class SellerOrdersComponent implements OnInit {

  orders: Order[] = [];
  filteredOrders: Order[] = [];

  // filters
  status?: string;
  startDate?: string;
  endDate?: string;

  // search
  searchOrderId: string = '';
  searchProduct: string = '';

  // pagination
  page = 0;
  size = 10;

  statuses = ['CREATED', 'CANCELLED', 'DELIVERED'];

  loading = false;

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;

    this.orderService.getSellerOrders(
      this.status,
      this.startDate,
      this.endDate,
      this.page,
      this.size
    ).subscribe({
      next: (data) => {
        this.orders = data;
        this.applySearch();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load orders', err);
        this.loading = false;
      }
    });
  }

  // Apply search locally
  applySearch(): void {
    this.filteredOrders = this.orders.filter(order => {
      const matchesOrderId = this.searchOrderId
        ? order.id.toLowerCase().includes(this.searchOrderId.toLowerCase())
        : true;

      const matchesProduct = this.searchProduct
        ? order.items.some((item: OrderItem) =>
            item.name.toLowerCase().includes(this.searchProduct.toLowerCase())
          )
        : true;

      return matchesOrderId && matchesProduct;
    });
  }

  // filters
  applyFilters(): void {
    this.page = 0;
    this.loadOrders();
  }

  clearFilters(): void {
    this.status = undefined;
    this.startDate = undefined;
    this.endDate = undefined;
    this.searchOrderId = '';
    this.searchProduct = '';
    this.loadOrders();
  }

  // pagination
  nextPage(): void {
    this.page++;
    this.loadOrders();
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadOrders();
    }
  }

  // mark delivered
  markDelivered(orderId: string): void {
    this.orderService.markAsDelivered(orderId).subscribe({
      next: () => this.loadOrders(),
      error: err => console.error('Delivery update failed', err)
    });
  }
}