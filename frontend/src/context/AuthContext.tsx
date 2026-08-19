import React, { createContext, useState } from "react";
import { login as loginApi, register as registerApi } from "../api/authApi";
import type { LoginRequest, RegisterRequest } from "../types/auth";

interface AuthContextType {
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => void;
  register: (request: RegisterRequest) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(
    localStorage.getItem("token") !== null,
  );

  const login = async (request: LoginRequest): Promise<void> => {
    const response = await loginApi(request);

    console.log("AuthContext login response:", response);
    console.log("Login successful:", response.success);

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
    const response = await registerApi(request);
    console.log("AuthContext register response:", response);
    console.log("Register successful:", response.success);
    if (response.success) {
      setIsAuthenticated(false);
    }
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}

export { AuthContext, AuthProvider };
