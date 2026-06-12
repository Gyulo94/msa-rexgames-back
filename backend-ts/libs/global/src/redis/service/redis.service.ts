import { Inject, Injectable } from '@nestjs/common';
import * as Redis from 'ioredis';

@Injectable()
export class RedisService {
  constructor(@Inject('Redis') private readonly redis: Redis.Redis) {}

  async set(key: string, value: string, expireTime?: number) {
    return this.redis.set(key, value, 'EX', expireTime ?? 10);
  }

  async get(key: string) {
    return await this.redis.get(key);
  }

  async del(key: string) {
    if (key.includes('*')) {
      let cursor = '0';
      do {
        const [nextCursor, keys] = await this.redis.scan(
          cursor,
          'MATCH',
          key,
          'COUNT',
          100,
        );
        cursor = nextCursor;
        if (keys.length > 0) {
          await this.redis.del(...keys);
        }
      } while (cursor !== '0');
    } else {
      await this.redis.del(key);
    }
  }
}
