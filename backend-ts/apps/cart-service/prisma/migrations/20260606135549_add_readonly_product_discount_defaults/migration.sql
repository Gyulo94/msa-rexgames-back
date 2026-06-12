-- AlterTable
ALTER TABLE "readonly_products" ADD COLUMN     "discount" INTEGER NOT NULL DEFAULT 0,
ADD COLUMN     "discountPrice" INTEGER NOT NULL DEFAULT 0;
