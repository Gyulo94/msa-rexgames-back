import { z } from 'zod';

export const ProductFilterRequestSchema = z.object({
  genres: z
    .string()
    .transform((val) => (val ? val.split(',').filter(Boolean) : []))
    .pipe(z.array(z.string()))
    .optional(),

  platforms: z
    .string()
    .transform((val) => (val ? val.split(',').filter(Boolean) : []))
    .pipe(z.array(z.string()))
    .optional(),

  minPrice: z
    .string()
    .transform((val) => parseInt(val, 10))
    .pipe(z.number())
    .optional(),

  maxPrice: z
    .string()
    .transform((val) => parseInt(val, 10))
    .pipe(z.number())
    .optional(),

  sortBy: z.string().optional(),
  sortOrder: z.string().optional(),
  name: z.string().optional(),
  hasDiscount: z.string().optional(),

  page: z
    .string()
    .transform((val) => parseInt(val, 10))
    .pipe(z.number())
    .optional(),

  take: z
    .string()
    .transform((val) => parseInt(val, 10))
    .pipe(z.number())
    .optional(),
});

export type ProductFilterRequest = z.infer<typeof ProductFilterRequestSchema>;
