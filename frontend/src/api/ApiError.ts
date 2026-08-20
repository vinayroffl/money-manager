interface ApiFieldError {
  field: string;
  message: string;
}

class ApiError extends Error {
  status: number;
  errors: ApiFieldError[];

  constructor(status: number, message: string, errors: ApiFieldError[] = []) {
    super(message);
    this.status = status;
    this.errors = errors;
  }
}

export type { ApiFieldError };
export default ApiError;
