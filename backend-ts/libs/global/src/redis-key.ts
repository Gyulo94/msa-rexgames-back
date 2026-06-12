export const RedisKey = {
  verificationRegister: (token: string) => `verification:register:${token}`,
  verificationReset: (token: string) => `verification:reset:${token}`,
  userRefreshToken: (userId: number) => `RT:${userId}`,
  cachedTokens: (oldRefreshToken: string) => `cachedTokens:${oldRefreshToken}`,
  cachedCart: (userId: number) => `cart:${userId}`,
};
