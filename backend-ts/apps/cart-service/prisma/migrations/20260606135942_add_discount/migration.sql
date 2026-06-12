/*
  Warnings:

  - You are about to drop the column `imageUrl` on the `readonly_products` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "readonly_products" DROP COLUMN "imageUrl",
ADD COLUMN     "image" TEXT,
ALTER COLUMN "price" SET DEFAULT 0;
