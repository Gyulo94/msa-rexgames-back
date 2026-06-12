import { Router } from 'express';
import { ProductQueryController } from './controller/product-query.controller';
import { validate } from '@/common';
import { ProductFilterRequestSchema } from './schema/prodict-filter.schema';

const router = Router();
const controller = new ProductQueryController();
const bind = (func: Function) => func.bind(controller);

router.get(
  '',
  validate(ProductFilterRequestSchema, 'query'),
  bind(controller.findAll),
);
router.get('/:productId', bind(controller.findById));

export default router;
