import {
  MiddlewareConsumer,
  Module,
  NestModule,
  RequestMethod,
} from '@nestjs/common';
import { GlobalModule, JwtGuard, RequestMiddleware } from '@app/global';
import { PrismaModule } from './common';
import { CartModule } from './cart/cart.module';
import { APP_GUARD } from '@nestjs/core';

@Module({
  imports: [GlobalModule, PrismaModule, CartModule],
  providers: [
    {
      provide: APP_GUARD,
      useClass: JwtGuard,
    },
  ],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(RequestMiddleware)
      .forRoutes({ path: '*path', method: RequestMethod.ALL });
  }
}
