import ApiError from "./ApiError";

const API_BASE_URL = "http://localhost:8080";

async function apiClient(
  resourcePath: string,
  options?: RequestInit,
): Promise<Response> {
  const token = localStorage.getItem("token");

  const headers = new Headers(options?.headers);

  headers.set("Content-Type", "application/json");

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${resourcePath}`, {
    ...options,
    headers,
  });

  const isAuthRequest =
    resourcePath === "/api/auth/login" || resourcePath === "/api/auth/register";

  if (response.status === 401 && !isAuthRequest) {
    // Ignore a response from an older session after a new login.
    if (localStorage.getItem("token") === token) {
      window.dispatchEvent(new Event("auth:unauthorized"));
    }

    throw new ApiError(
      401,
      "Your session is no longer valid. Please sign in again.",
    );
  }

  if (!response.ok) {
    const errorData = await response.json();

    throw new ApiError(
      response.status,
      errorData.message,
      errorData.errors ?? [],
    );
  }

  return response;
}

export default apiClient;
