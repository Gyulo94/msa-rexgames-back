export type ProductEvent = {
  productId: number;
  name: string;
  slug: string;
  price: number;
  discount: number;
  discountPrice: number;
  genreName: string;
  platformName: string;
  description: string;
  reviewRating: number;
  reviewCount: number;
  stock: number;
  specs: Record<string, any>;
  images: string[];
};
