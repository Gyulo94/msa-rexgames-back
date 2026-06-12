export const RedisKey = {
  productSearch(filterState: string) {
    return `products:search:${Buffer.from(filterState).toString("base64")}`;
  },
  productDetail(id: number) {
    return `products:detail:${id}`;
  },
};
