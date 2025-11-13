import { NextRequest, NextResponse } from 'next/server';
import { securityMiddleware } from '@/src/middleware/security.middleware';

export function middleware(request: NextRequest) {
  return securityMiddleware(request);
}

export const config = {
  matcher: [
    '/api/:path*',
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};

