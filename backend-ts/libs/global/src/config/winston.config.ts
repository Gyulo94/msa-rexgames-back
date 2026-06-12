import { formatInTimeZone } from 'date-fns-tz';
import { WinstonModule, utilities } from 'nest-winston';
import winston from 'winston';

interface TimestampOptions {
  tz?: string;
}

const isTimestampOptions = (opts: unknown): opts is TimestampOptions => {
  return (
    typeof opts === 'object' &&
    opts !== null &&
    ('tz' in opts ? typeof (opts as { tz?: unknown }).tz === 'string' : true)
  );
};

const apeendTimestamp = winston.format((info, opts: unknown) => {
  if (isTimestampOptions(opts) && opts.tz) {
    info.timestamp = formatInTimeZone(
      new Date(),
      opts.tz,
      'yyyy-MM-dd HH:mm:ss',
    );
  }
  return info;
});

export const winstonLogger = WinstonModule.createLogger({
  format: winston.format.combine(
    apeendTimestamp({ tz: 'Asia/Seoul' }),
    winston.format.json(),
    process.env.NODE_ENV !== 'production'
      ? winston.format.colorize({ all: true })
      : winston.format.uncolorize(),
    winston.format.printf((info) => {
      const stack =
        info.stack || info.trace ? `\n${info.stack || info.trace}` : '';
      return `${info.timestamp} - ${info.level} [${process.pid}] [${info.context || process.env.APP_NAME}] : ${info.message}${stack}`;
    }),
  ),
  transports: [
    new winston.transports.Console({
      level: process.env.NODE_ENV === 'production' ? 'info' : 'silly',
      handleExceptions: true,
      handleRejections: true,
    }),
  ],
});
