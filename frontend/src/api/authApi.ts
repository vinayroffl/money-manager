import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from "../types/auth";
import type { ApiResponse } from "../types/api";
import apiClient from "./apiClient";

async function login(
  request: LoginRequest,
): Promise<ApiResponse<LoginResponse>> {
  const response = await apiClient("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    console.log("HTTP error:", response.status);
  }

  const data = await response.json();

  return data;
}

async function register(
  request: RegisterRequest,
): Promise<ApiResponse<RegisterResponse>> {
  const response = await apiClient("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    console.log("HTTP error:", response.status);
  }

  const data = await response.json();

  return data;
}

export { login, register };
