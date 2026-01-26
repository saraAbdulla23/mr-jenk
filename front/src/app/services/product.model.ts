export interface Product {
  productId: string;
  name: string;
  price: number;
  quantity: number;
  description?: string | null;
  userId?: string; // backend sets this from JWT
  imageUrls?: string[]; // array of product images
}
