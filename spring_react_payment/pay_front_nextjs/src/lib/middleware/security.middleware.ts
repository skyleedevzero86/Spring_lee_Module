import { NextRequest, NextResponse } from 'next/server';
import { sanitizeInput, containsSqlInjection, containsXss } from '@/src/lib/utils/security';

export function securityMiddleware(request: NextRequest) {
  const response = NextResponse.next();

  response.headers.set('X-Content-Type-Options', 'nosniff');
  response.headers.set('X-Frame-Options', 'DENY');
  response.headers.set('X-XSS-Protection', '1; mode=block');
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');
  response.headers.set('Permissions-Policy', 'geolocation=(), microphone=(), camera=()');

  if (request.nextUrl.protocol === 'https:') {
    response.headers.set(
      'Strict-Transport-Security',
      'max-age=31536000; includeSubDomains; preload'
    );
  }

  const url = request.nextUrl.toString();
  if (containsSqlInjection(url) || containsXss(url)) {
    return new NextResponse('Invalid request', { status: 400 });
  }

  const searchParams = request.nextUrl.searchParams;
  for (const [key, value] of searchParams.entries()) {
    if (containsSqlInjection(value) || containsXss(value)) {
      return new NextResponse('Invalid request', { status: 400 });
    }
  }

  return response;
}

