import { ProductQuery } from "@prisma/client-query";

export class ProductListResponse {
  products: ProductQuery[];
  totalCount: number;
  currentPage: number;
  totalPages: number;

  constructor(
    products: ProductQuery[],
    totalCount: number,
    currentPage: number,
    totalPages: number,
  ) {
    this.products = products;
    this.totalCount = totalCount;
    this.currentPage = currentPage;
    this.totalPages = totalPages;
  }
}
