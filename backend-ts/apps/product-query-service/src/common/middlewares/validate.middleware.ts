import { Request, Response, NextFunction } from "express";
import { ZodObject, ZodError } from "zod";
import { ApiException } from "../exceptions/api.exception";
import { ErrorCode } from "../enums/error-code.enum";

declare global {
  namespace Express {
    interface Request {
      validated?: any;
    }
  }
}

export const validate = (
  schema: ZodObject<any>,
  value: "body" | "query" | "params" = "body",
) => {
  return async (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> => {
    try {
      const parsedData = await schema.parseAsync(req[value]);

      req.validated = parsedData;
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        throw new ApiException(ErrorCode.BAD_REQUEST, error.message);
      }
      next(error);
    }
  };
};
