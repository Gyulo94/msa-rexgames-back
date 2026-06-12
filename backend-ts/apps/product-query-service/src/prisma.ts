import { PrismaClient } from '@prisma/client-query';
import { NODE_ENV } from './common';

class PrismaService {
  private static instance: PrismaClient;

  private constructor() {}

  public static getInstance(): PrismaClient {
    if (!PrismaService.instance) {
      PrismaService.instance = new PrismaClient({
        log:
          NODE_ENV === 'development'
            ? ['query', 'info', 'warn', 'error']
            : ['error'],
      });
      console.log('🔌 Prisma Client 연결 성공');
    }
    return PrismaService.instance;
  }
  public static async disconnect(): Promise<void> {
    if (PrismaService.instance) {
      await PrismaService.instance.$disconnect();
      console.log('🔌 Prisma Client 연결 종료');
    }
  }
}
export default PrismaService;
