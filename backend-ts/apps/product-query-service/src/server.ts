import express, { Application } from 'express';
import { ErrorMiddleware, ApiMiddleware, LoggerMiddleware } from './common';

class Server {
  private app: Application;
  private port: number;

  constructor(port: number) {
    this.app = express();
    this.port = port;

    this.initializeMiddlewares();
  }

  private initializeMiddlewares(): void {
    this.app.use(express.json());
    this.app.use(express.urlencoded({ extended: true }));
    this.app.use(new ApiMiddleware().use);
    this.app.use(new LoggerMiddleware().use);
    this.initializeRoutes();
    this.app.use(new ErrorMiddleware().use);
  }

  private initializeRoutes(): void {
    this.app.use(
      '/api/product-query',
      require('./product-query/routes').default,
    );
  }

  public listen(): void {
    this.app.listen(this.port, () => {
      console.log(`🚀 상품 조회 서비스 ${this.port}번 포트에서 실행 중`);
      console.log(`📍 환경변수: ${process.env.NODE_ENV}`);
    });
  }
}

export default Server;
