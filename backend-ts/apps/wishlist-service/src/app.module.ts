import {
  MiddlewareConsumer,
  Module,
  NestModule,
  RequestMethod,
} from '@nestjs/common';
import { GlobalModule, RequestMiddleware } from '@app/global';
import { PrismaModule } from './common';
import { WishlistModule } from './wishlist/wishlist.module';

@Module({
  imports: [GlobalModule, PrismaModule, WishlistModule],
  providers: [],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(RequestMiddleware)
      .forRoutes({ path: '*path', method: RequestMethod.ALL });
  }
}
