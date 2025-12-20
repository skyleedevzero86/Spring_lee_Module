import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'PassyKey - 패스키 인증',
  description: '안전한 패스키 인증 시스템',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <head>
        <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>🔐</text></svg>" />
        <link
          rel="stylesheet"
          as="style"
          crossOrigin="anonymous"
          href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@latest/dist/web/static/pretendard-dynamic-subset.min.css"
        />
      </head>
      <body>{children}</body>
    </html>
  );
}

