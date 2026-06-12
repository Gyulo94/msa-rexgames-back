/*
  Warnings:

  - You are about to drop the column `imageUrl` on the `readonly_products` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "readonly_products" DROP COLUMN "imageUrl",
ADD COLUMN     "discount" INTEGER NOT NULL DEFAULT 0,
ADD COLUMN     "discountPrice" INTEGER NOT NULL DEFAULT 0,
ADD COLUMN     "image" TEXT,
ADD COLUMN     "stock" INTEGER NOT NULL DEFAULT 0,
ALTER COLUMN "price" SET DEFAULT 0;
