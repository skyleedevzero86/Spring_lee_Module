import type { Metadata, Viewport } from "next";
import "./globals.css";
import { QueryProvider } from '@/app/providers/QueryProvider';

export const metadata: Metadata = {
  title: "토스 페이먼츠 결제 시스템",
  description: "토스 페이먼츠를 활용한 결제 시스템",
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <body className="antialiased">
        <QueryProvider>
          {children}
        </QueryProvider>
      </body>
    </html>
  );
}
