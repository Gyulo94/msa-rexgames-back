import 'dotenv/config';
import Server from './server';
import PrismaService from './prisma';
import { NODE_ENV, PORT } from './common';
import { startProductQueryConsumer } from './common/event/product-query.consumer';

class Application {
  private server: Server;

  constructor() {
    this.validateEnv();
    this.server = new Server(PORT);
  }

  private validateEnv(): void {
    if (!NODE_ENV) {
      console.warn(
        "NODE_ENV가 설정되지 않았습니다. 기본값으로 'development'를 사용합니다.",
      );
    }
  }

  public async start(): Promise<void> {
    this.server.listen();
    await startProductQueryConsumer();
  }
}

const app = new Application();
app.start();

process.on('SIGINT', async () => {
  console.log('\n🛑 서버 종료 중...');
  await PrismaService.disconnect();
  process.exit(0);
});

process.on('SIGTERM', async () => {
  console.log('\n🛑 서버 종료 중...');
  await PrismaService.disconnect();
  process.exit(0);
});
