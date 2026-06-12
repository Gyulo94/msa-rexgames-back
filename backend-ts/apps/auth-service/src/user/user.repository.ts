import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common';
import { Prisma, User } from '@prisma/client-auth';

@Injectable()
export class UserRepository {
  constructor(private readonly prisma: PrismaService) {}

  async findByEmail(email: string) {
    return this.prisma.user.findUnique({
      where: {
        email,
      },
    });
  }

  create(data: Prisma.UserCreateInput): Promise<User> {
    return this.prisma.user.create({
      data,
    });
  }

  async resetPassword(data: Prisma.UserUpdateArgs) {
    return this.prisma.user.update(data);
  }

  async createSocialUser(data: Prisma.UserCreateInput): Promise<User> {
    return this.prisma.user.create({
      data,
    });
  }
}
