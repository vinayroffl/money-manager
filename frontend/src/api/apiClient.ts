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

  if (!response.ok) {
    throw new ApiError(response.status, response.statusText);
  }

  return response;
}

export default apiClient;
