import { Request, Response } from 'express';
import { ProductQueryService } from '../service/product-query.service';

export class ProductQueryController {
  private productQueryService: ProductQueryService;
  constructor() {
    this.productQueryService = new ProductQueryService();
  }

  async findAll(req: Request, res: Response) {
    const response = await this.productQueryService.findAll(req);
    return res.status(200).json(response);
  }

  async findById(req: Request, res: Response) {
    const productId = Number(req.params.productId);
    const response = await this.productQueryService.findById(productId);
    return res.status(200).json(response);
  }
}
