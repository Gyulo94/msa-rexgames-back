import { HttpModule } from '@nestjs/axios';
import { Global, Logger, Module } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { RedisModule } from './redis/redis.module';

@Global()
@Module({
  imports: [HttpModule, RedisModule],
  providers: [JwtService, Logger],
  exports: [JwtService, Logger, HttpModule, RedisModule],
})
export class GlobalModule {}
