export interface CartItem {
    productId: string;
    name: string;
    price: number;
    quantity: number;
    subtotal: number;
  }
  
  export interface CartResponse {
    items: CartItem[];
    totalAmount: number;
  }