import { Component, OnInit } from '@angular/core';
import { CartService } from '../services/cart.service';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service'; // ✅ import UserService
import * as countries from 'i18n-iso-countries';
import enLocale from 'i18n-iso-countries/langs/en.json';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatButtonModule
  ],
  templateUrl: './checkout.html',
  styleUrls: ['./checkout.scss']
})
export class CheckoutComponent implements OnInit {

  currentStep = 1;
  addressForm: FormGroup;
  cart: any;
  loading = false;
  orderSuccess = false;
  errorMessage = '';
  countriesList: { code: string; name: string; flag: string }[] = [];

  constructor(
    private cartService: CartService,
    private fb: FormBuilder,
    private router: Router,
    private userService: UserService // ✅ inject UserService
  ) {
    this.addressForm = this.fb.group({
      street: ['', Validators.required],
      city: ['', Validators.required],
      zip: ['', Validators.required],
      country: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.loadCart();
    this.loadCountries();
    this.loadUserAddress(); // ✅ pre-fill address
  }

  loadCart(): void {
    this.cartService.getCart().subscribe(res => {
      this.cart = res;
      if (!this.cart.total) {
        this.cart.total = this.cart.items.reduce((acc: number, i: any) => acc + i.subtotal, 0);
      }
    });
  }

  loadCountries(): void {
    countries.registerLocale(enLocale);
    const countryNames = countries.getNames("en", { select: "official" });
    this.countriesList = Object.entries(countryNames).map(([code, name]) => ({
      code,
      name: name as string,
      flag: this.getFlagEmoji(code)
    }));
  }

  getFlagEmoji(countryCode: string): string {
    return countryCode
      .toUpperCase()
      .replace(/./g, char => String.fromCodePoint(127397 + char.charCodeAt(0)));
  }

  /** ✅ Pre-fill address from user profile if available */
  loadUserAddress(): void {
    this.userService.getCurrentUser().subscribe({
      next: user => {
        if (user.address) {
          // Expecting address string: "street, city, zip, country"
          const parts = user.address.split(',').map(p => p.trim());
          this.addressForm.patchValue({
            street: parts[0] || '',
            city: parts[1] || '',
            zip: parts[2] || '',
            country: parts[3] || ''
          });
        }
      },
      error: err => {
        console.warn('Could not load user address', err);
      }
    });
  }

  nextStep(): void {
    if (this.currentStep === 1 && this.addressForm.invalid) {
      this.addressForm.markAllAsTouched();
      return;
    }
    this.currentStep++;
  }

  prevStep(): void {
    this.currentStep--;
  }

  confirmOrder(): void {
    this.loading = true;
    const addressString = `${this.addressForm.value.street}, ${this.addressForm.value.city}, ${this.addressForm.value.zip}, ${this.addressForm.value.country}`;

    this.cartService.checkout(addressString).subscribe({
      next: () => {
        this.orderSuccess = true;
        this.loading = false;
        this.cart.items = [];
        this.cart.total = 0;

        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 2000);
      },
      error: err => {
        this.errorMessage = err?.error?.message || 'Checkout failed';
        this.loading = false;
      }
    });
  }
}