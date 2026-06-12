import { Kafka } from 'kafkajs';
import { KAFKA_BROKER } from '../constants';

export const kafka = new Kafka({
  clientId: 'product-query-service',
  brokers: [KAFKA_BROKER],
});
