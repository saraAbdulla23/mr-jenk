import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { DashboardService } from '../services/dashboard.service';
import { UserDashboard } from '../services/dashboard.model';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, registerables } from 'chart.js';
import { ProductService } from '../services/product.service';
import { Product } from '../services/product.model';

Chart.register(...registerables);

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, BaseChartDirective],
  templateUrl: './user-dashboard.html',
  styleUrls: ['./user-dashboard.scss'],
})
export class UserDashboardComponent implements OnInit {

  dashboard?: UserDashboard;

  chartLabels: string[] = [];
  chartData: number[] = [];

  // Map productId -> productName
  productNames: Record<string, string> = {};

  constructor(
    private dashboardService: DashboardService,
    private productService: ProductService
  ) {}

  ngOnInit(): void {

    // Load all products first
    this.productService.getAllProducts().subscribe(products => {
      products.forEach((p: Product) => {
        if (p.productId) this.productNames[p.productId] = p.name;
      });

      // Then load user dashboard
      this.dashboardService.getUserDashboard()
        .subscribe(data => {
          console.log("USER DASHBOARD:", data);
          this.dashboard = data;

          this.chartLabels = data.mostBoughtProducts.map(p =>
            this.productNames[p.key] || p.key
          );
          this.chartData = data.mostBoughtProducts.map(p => p.value);
        });

    });

  }

}