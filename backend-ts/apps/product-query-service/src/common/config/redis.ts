import { createClient } from "redis";
import { REDIS_HOST, REDIS_PORT } from "../constants";
import { LOGGER } from "./logger";

export const redis = createClient({
  socket: {
    host: REDIS_HOST,
    port: REDIS_PORT,
  },
});

redis.on("error", (err) => LOGGER.error("❌ Redis Client 오류", err));

redis
  .connect()
  .then(() => LOGGER.info(`🔌 Redis 연결 중`))
  .catch((err) => LOGGER.error("❌ Redis 연결 실패", err));
