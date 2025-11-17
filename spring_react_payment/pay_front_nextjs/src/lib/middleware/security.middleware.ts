import { NextRequest, NextResponse } from 'next/server';
import { sanitizeInput, containsSqlInjection, containsXss } from '@/lib/utils';

export function securityMiddleware(request: NextRequest) {
  try {
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

    const pathname = request.nextUrl.pathname;
    if (pathname && (containsSqlInjection(pathname) || containsXss(pathname))) {
      return new NextResponse('Invalid request', { status: 400 });
    }

    const searchParams = request.nextUrl.searchParams;
    for (const [key, value] of searchParams.entries()) {
      if (value && (containsSqlInjection(value) || containsXss(value))) {
        return new NextResponse('Invalid request', { status: 400 });
      }
    }

    return response;
  } catch (error) {
    console.error('Security middleware error:', error);
    return NextResponse.next();
  }
}

