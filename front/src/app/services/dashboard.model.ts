export interface ProductCount {
    key: string;     // productId
    value: number;   // quantity
  }
  
  export interface UserDashboard {
    totalSpent: number;
    mostBoughtProducts: ProductCount[];
    topCategories: any[];
  }
  
  export interface SellerDashboard {
    totalRevenue: number;
    bestSellingProducts: ProductCount[];
    unitsSold: number;
  }