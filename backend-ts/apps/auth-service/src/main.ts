import './env';
import { NestFactory, Reflector } from '@nestjs/core';
import { AppModule } from './app.module';
import { ValidationPipe } from '@nestjs/common';
import {
  ApiInterceptor,
  HttpExceptionFilter,
  winstonLogger,
} from '@app/global';

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    logger: winstonLogger,
    bufferLogs: true,
  });
  app.setGlobalPrefix('api');
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: {
        enableImplicitConversion: true,
      },
    }),
  );
  const reflector = app.get(Reflector);
  app.useGlobalFilters(new HttpExceptionFilter(winstonLogger));
  app.useGlobalInterceptors(new ApiInterceptor(reflector));
  await app.listen(process.env.PORT ?? 7003);
}
bootstrap();
