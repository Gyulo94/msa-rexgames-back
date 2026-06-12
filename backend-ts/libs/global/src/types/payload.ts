export type JwtPayload = {
  id: number;
  role: string;
  exp?: number;
  iat?: number;
  nbf?: number;
};
