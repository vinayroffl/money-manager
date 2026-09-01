export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  tokenType: string;
};

export type RegisterResponse = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  createdAt: string;
};
