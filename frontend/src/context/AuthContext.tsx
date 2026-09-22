import React, { useCallback, useEffect, useState } from "react";
import { login as loginApi, register as registerApi } from "../api/authApi";
import type { LoginRequest, RegisterRequest } from "../types/auth";
import { AuthContext } from "./authContextValue";

function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(
    localStorage.getItem("token") !== null,
  );

  const login = async (request: LoginRequest): Promise<void> => {
    const response = await loginApi(request);

    if (!response.success || !response.data?.token) {
      throw new Error(response.message || "Login failed");
    }

    localStorage.setItem("token", response.data.token);
    setIsAuthenticated(true);
  };
  const logout = (): void => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
  };

  const register = async (request: RegisterRequest): Promise<void> => {
    await registerApi(request);
  };

  const handleUnauthorized = useCallback((): void => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
  }, []);

  useEffect(() => {
    window.addEventListener("auth:unauthorized", handleUnauthorized);

    return () => {
      window.removeEventListener("auth:unauthorized", handleUnauthorized);
    };
  }, [handleUnauthorized]);

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, login, logout, register, handleUnauthorized }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export { AuthProvider };
