import { ProductQueryRepository } from '../../product-query/repository/product-query.repository';
import { LOGGER, kafka, redis, RedisKey } from '@/common';

const consumer = kafka.consumer({ groupId: 'product-query-consumer' });
const productQueryRepository = new ProductQueryRepository();

export async function startProductQueryConsumer(): Promise<void> {
  try {
    await consumer.connect();
    await consumer.subscribe({ topic: 'product-topic', fromBeginning: true });
    await consumer.run({
      eachMessage: async ({ message }) => {
        if (!message.value) return;

        try {
          const event = JSON.parse(message.value.toString());
          if (event.name === undefined) {
            await productQueryRepository.deleteProduct(event.productId);
            LOGGER.info(
              `Kafka 메시지 처리 완료 (삭제): Product ID ${event.productId}`,
            );
          } else {
            await productQueryRepository.upsertProduct(event);
            LOGGER.info(
              `Kafka 메시지 처리 완료: Product ID ${event.productId}`,
            );
          }
          const detailKey = RedisKey.productDetail(event.productId);
          await redis.del(detailKey);

          const searchKeys = await redis.keys('products:search:*');
          if (searchKeys.length > 0) {
            await redis.del(searchKeys);
            LOGGER.info(
              `Redis 상품 검색 캐시 무효화 완료 (${searchKeys.length}개 키 삭제)`,
            );
          }

          LOGGER.info(`Kafka 메시지 처리 완료: Product ID ${event.productId}`);
        } catch (err: any) {
          LOGGER.error(`Kafka 메시지 처리 중 오류 발생: ${err.message}`, err);
        }
      },
    });
  } catch (error: any) {
    LOGGER.error(`Kafka Consumer 시작 실패: ${error.message}`, error);
  }
}
