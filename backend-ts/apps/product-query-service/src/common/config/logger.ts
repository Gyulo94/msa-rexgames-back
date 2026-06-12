import winston from "winston";
import { formatInTimeZone } from "date-fns-tz";
import { NODE_ENV, SERVICE_NAME } from "../constants";

export class LoggerService {
  private logger: winston.Logger;
  private context?: string;

  constructor(context?: string) {
    this.context = context;
    this.logger = this.createLogger();
  }

  private createLogger(): winston.Logger {
    const seoulTimestamp = winston.format((info) => {
      info.timestamp = formatInTimeZone(
        new Date(),
        "Asia/Seoul",
        "yyyy-MM-dd HH:mm:ss",
      );
      return info;
    });

    return winston.createLogger({
      level: NODE_ENV === "production" ? "info" : "silly",

      format: winston.format.combine(
        seoulTimestamp(),
        winston.format.errors({ stack: true }),
        winston.format.splat(),
      ),

      transports: [
        new winston.transports.Console({
          level: NODE_ENV === "production" ? "info" : "silly",
          format: winston.format.combine(
            seoulTimestamp(),
            winston.format.colorize(),
            winston.format.printf(
              ({ timestamp, level, message, context, ...meta }) => {
                let log = `[${SERVICE_NAME}] ${timestamp} [${level.padEnd(7)}]`;

                if (context) {
                  log += ` [${context}]`;
                }

                log += ` : ${message}`;

                if (Object.keys(meta).length > 0) {
                  log += ` ${JSON.stringify(meta)}`;
                }

                return log;
              },
            ),
          ),
        }),
      ],
    });
  }

  public info(message: string, context?: string) {
    const ctx = context || this.context;
    this.logger.info(message, ctx ? { context: ctx } : {});
  }

  public error(message: string | Error, context?: string) {
    const ctx = context || this.context;
    if (message instanceof Error) {
      this.logger.error(message.message, {
        ...(ctx ? { context: ctx } : {}),
        stack: message.stack,
        name: message.name,
      });
    } else {
      this.logger.error(message, ctx ? { context: ctx } : {});
    }
  }

  public warn(message: string, context?: string) {
    const ctx = context || this.context;
    this.logger.warn(message, ctx ? { context: ctx } : {});
  }

  public http(message: string, meta: any = {}) {
    this.logger.http(message, {
      ...meta,
      context: meta.context || this.context || "RequestLogger",
    });
  }
}

export const LOGGER = new LoggerService();
