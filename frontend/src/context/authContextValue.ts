import { createContext } from "react";
import type { LoginRequest, RegisterRequest } from "../types/auth";

interface AuthContextType {
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => void;
  register: (request: RegisterRequest) => Promise<void>;
  handleUnauthorized: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined,
);
