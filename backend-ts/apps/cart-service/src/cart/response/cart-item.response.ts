export type CartItemResponse = {
  productId: number;
  quantity: number;
  product: {
    id: number;
    name: string;
    price: number;
    discount: number;
    discountPrice: number;
    stock: number;
    image: string | null;
  };
};
