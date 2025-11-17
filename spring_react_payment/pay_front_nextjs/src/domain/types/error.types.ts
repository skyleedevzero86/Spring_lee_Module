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
  public readonly retryAfter?: number;

  constructor(
    public readonly code: string,
    public readonly statusCode: number,
    message: string,
    public readonly detail?: string,
    retryAfter?: number
  ) {
    super(message);
    this.name = 'ApiError';
    this.retryAfter = retryAfter;
  }

  static fromResponse(
    response: ErrorResponse,
    statusCode: number,
    retryAfter?: number
  ): ApiError {
    return new ApiError(
      response.code,
      statusCode,
      response.message,
      response.detail,
      retryAfter
    );
  }
}


