export interface ErrorResponse {
  timestamp: string;
  code: string;
  message: string;
  detail?: string;
}

export interface ValidationError {
  field: string;
  message: string;
}

export class ApiError extends Error {
  constructor(
    public readonly code: string,
    public readonly statusCode: number,
    message: string,
    public readonly detail?: string
  ) {
    super(message);
    this.name = 'ApiError';
  }

  static fromResponse(response: ErrorResponse, statusCode: number): ApiError {
    return new ApiError(
      response.code,
      statusCode,
      response.message,
      response.detail
    );
  }
}

