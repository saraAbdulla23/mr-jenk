import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router, ActivatedRoute } from '@angular/router';
import { OrderService } from '../services/order.service';
import { Order, OrderStatus } from '../services/order.model';

@Component({
  selector: 'app-order',
  templateUrl: './order.html',
  styleUrls: ['./order.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ]
})
export class OrderComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  selectedStatus: OrderStatus | '' = '';
  selectedDate: string = '';
  selectedOrder?: Order;

  constructor(
    private orderService: OrderService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  private loadOrders(): void {
    this.orderService.getMyOrders().subscribe({
      next: (orders) => {
        this.orders = orders.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.filteredOrders = [...this.orders];
      },
      error: (err) => console.error('Failed to load orders', err)
    });
  }

  filterOrders(): void {
    this.filteredOrders = this.orders.filter(order => {
      const statusMatch = this.selectedStatus ? order.status === this.selectedStatus : true;
      const dateMatch = this.selectedDate ? new Date(order.createdAt).toDateString() === new Date(this.selectedDate).toDateString() : true;
      return statusMatch && dateMatch;
    });
  }

  viewOrder(order: Order): void {
    this.selectedOrder = order;
  }

  backToList(): void {
    this.selectedOrder = undefined;
  }

  getStatusTimeline(): string[] {
    return ['CREATED', 'DELIVERED']; // full possible statuses
  }
  
  isStepCompleted(step: string): boolean {
    if (!this.selectedOrder) return false;
    const orderStatus = this.selectedOrder.status;
    const orderIndex = this.getStatusTimeline().indexOf(orderStatus);
    const stepIndex = this.getStatusTimeline().indexOf(step);
    return stepIndex < orderIndex || step === orderStatus;
  }

  cancelSelectedOrder(): void {
    if (!this.selectedOrder) return;
  
    if (!confirm('Are you sure you want to cancel this order?')) return;
  
    this.orderService.cancelOrder(this.selectedOrder.id).subscribe({
      next: (updatedOrder) => {
        this.selectedOrder = updatedOrder;
        this.loadOrders(); // refresh list
      },
      error: (err) => console.error('Cancel failed', err)
    });
  }
  
  redoSelectedOrder(): void {
    if (!this.selectedOrder) return;
  
    if (!confirm('Do you want to place this order again?')) return;
  
    this.orderService.redoOrder(this.selectedOrder.id).subscribe({
      next: (newOrder) => {
        alert('Order placed again successfully!');
        this.selectedOrder = newOrder;
        this.loadOrders(); // refresh list
      },
      error: (err) => console.error('Redo failed', err)
    });
  }
}