import { Module } from '@nestjs/common';
import Redis from 'ioredis';
import { RedisService } from './service/redis.service';

@Module({
  providers: [
    {
      provide: 'Redis',
      useFactory: () => {
        return new Redis({
          host: process.env.REDIS_HOST || 'localhost',
          port: process.env.REDIS_PORT
            ? parseInt(process.env.REDIS_PORT)
            : 6380,
        });
      },
    },
    RedisService,
  ],
  exports: ['Redis', RedisService],
})
export class RedisModule {}
