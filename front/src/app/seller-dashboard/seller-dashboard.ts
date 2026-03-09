import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { DashboardService } from '../services/dashboard.service';
import { SellerDashboard } from '../services/dashboard.model';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, registerables } from 'chart.js';
import { ProductService } from '../services/product.service';
import { Product } from '../services/product.model';

Chart.register(...registerables);

@Component({
  selector: 'app-seller-dashboard',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, BaseChartDirective],
  templateUrl: './seller-dashboard.html',
  styleUrls: ['./seller-dashboard.scss'],
})
export class SellerDashboardComponent implements OnInit {

  dashboard?: SellerDashboard;

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
        if (p.productId) {
          this.productNames[p.productId] = p.name;
        }
      });

      // Then load seller dashboard
      this.dashboardService.getSellerDashboard()
        .subscribe(data => {

          console.log("SELLER DASHBOARD:", data);

          this.dashboard = data;

          // Replace product IDs with names for the chart
          this.chartLabels = data.bestSellingProducts.map(p =>
            this.productNames[p.key] || p.key
          );

          this.chartData = data.bestSellingProducts.map(p => p.value);
        });

    });

  }

}