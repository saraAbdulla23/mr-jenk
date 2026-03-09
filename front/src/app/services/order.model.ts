export interface OrderItem {
    productId: string;
    name: string;
    price: number;
    sellerId: string;
    quantity: number;
  }
  
  export type OrderStatus = 'CREATED' | 'CANCELLED' | 'DELIVERED';
  export type PaymentMethod = 'PAY_ON_DELIVERY';
  
  export interface Order {
    id: string; // matches backend @Id
    userId: string;
    items: OrderItem[];
    totalAmount: number;
    status: OrderStatus;
    paymentMethod: PaymentMethod;
    address: string;
    createdAt: string; // ISO string from backend
  }