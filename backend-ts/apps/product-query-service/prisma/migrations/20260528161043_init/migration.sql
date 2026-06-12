-- CreateTable
CREATE TABLE "q_products" (
    "product_id" INTEGER NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "slug" VARCHAR(255) NOT NULL,
    "price" INTEGER NOT NULL,
    "discount_price" INTEGER,
    "genre_name" VARCHAR(100),
    "platform_name" VARCHAR(100),
    "description" TEXT,
    "review_rating" DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    "review_count" INTEGER NOT NULL DEFAULT 0,
    "specs" JSONB NOT NULL DEFAULT '[]',
    "images" JSONB NOT NULL DEFAULT '[]',
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "q_products_pkey" PRIMARY KEY ("product_id")
);

-- CreateIndex
CREATE UNIQUE INDEX "q_products_slug_key" ON "q_products"("slug");
