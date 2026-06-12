interface Success<T> {
  statusCode: number;
  message: string;
  body: T;
  timestamp: string;
  path: string;
}

interface Error {
  statusCode: number;
  message: string;
  path: string;
  timestamp: string;
  code?: string;
}
export type Api<T> = Success<T> | Error;
