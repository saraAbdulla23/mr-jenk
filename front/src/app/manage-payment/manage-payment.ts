import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormGroup
} from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { PaymentService } from '../services/payment.service';
import { Payment } from '../services/payment.model';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-manage-payment',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './manage-payment.html',
  styleUrls: ['./manage-payment.scss']
})
export class ManagePayment implements OnInit {

  payments: Payment[] = [];
  loading = false;
  errorMessage = '';
  selectedPayment: Payment | null = null;

  form: FormGroup;

  statusOptions = ['Active', 'Inactive']; // Dropdown options

  constructor(
    private paymentService: PaymentService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      type: ['', Validators.required],
      status: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.fetchPayments();
  }

  // ================= FETCH PAYMENTS =================
  fetchPayments(): void {
    this.loading = true;
    this.errorMessage = '';
    this.paymentService.getAllPayments()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: payments => this.payments = payments,
        error: () => this.errorMessage = 'Failed to fetch payments'
      });
  }

  // ================= SELECT =================
  selectPayment(payment: Payment): void {
    this.selectedPayment = payment;

    this.form.patchValue({
      type: payment.type,
      status: payment.status || 'Active' // Default to Active if empty
    });
  }

  // ================= CLEAR =================
  clearSelection(): void {
    this.selectedPayment = null;
    this.form.reset({
      type: '',
      status: 'Active' // Default dropdown to Active
    });
  }

  // ================= SAVE =================
  savePayment(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.form.value;

    if (this.selectedPayment) {
      // Update existing payment
      this.paymentService.updatePayment(this.selectedPayment.id as any, formValue)
        .subscribe({
          next: () => {
            this.fetchPayments();
            this.clearSelection();
          },
          error: () => this.errorMessage = 'Failed to update payment'
        });
    } else {
      // Create new payment
      this.paymentService.createPayment(formValue).subscribe({
        next: () => {
          this.fetchPayments();
          this.clearSelection();
        },
        error: () => this.errorMessage = 'Failed to create payment'
      });
    }
  }

  // ================= DELETE =================
  deletePayment(payment: Payment): void {
    if (!confirm(`Delete payment with type ${payment.type}?`)) return;

    this.paymentService.deletePayment(payment.id as any).subscribe({
      next: () => this.fetchPayments(),
      error: () => this.errorMessage = 'Failed to delete payment'
    });
  }

  // ================= PAYPAL TEST =================
payWithPaypal(): void {
  const testAmount = 10.00; // fake test amount
  this.paymentService.createPaypalPayment(testAmount).subscribe({
    next: (res) => {
      if (res.approvalUrl) {
        window.location.href = res.approvalUrl; // redirect to PayPal sandbox
      } else {
        this.errorMessage = 'Failed to get PayPal approval URL';
      }
    },
    error: () => this.errorMessage = 'PayPal payment request failed'
  });
}
}