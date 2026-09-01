import React, { createContext, useState } from "react";
import { login as loginApi, register as registerApi } from "../api/authApi";
import type { LoginRequest, RegisterRequest } from "../types/auth";

interface AuthContextType {
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => void;
  register: (request: RegisterRequest) => Promise<void>;
  handleUnauthorized: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(
    localStorage.getItem("token") !== null,
  );

  const login = async (request: LoginRequest): Promise<void> => {
    const response = await loginApi(request);
    if (response.success) {
      localStorage.setItem("token", response.data.token);
      setIsAuthenticated(true);
    }
  };
  const logout = (): void => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
  };

  const register = async (request: RegisterRequest): Promise<void> => {
    await registerApi(request);
  };

  const handleUnauthorized = (): void => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, login, logout, register, handleUnauthorized }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export { AuthContext, AuthProvider };
