import type { Metadata } from "next";
import "./globals.css";
import { ErrorBoundaryWrapper } from '@/components/common/ErrorBoundaryWrapper';
import { QueryProvider } from '@/app/providers/QueryProvider';
import { MonitoringProvider } from '@/app/providers/MonitoringProvider';
import { ErrorTrackerInit } from '@/components/common/ErrorTrackerInit';

export const metadata: Metadata = {
  title: "토스 페이먼츠 결제 시스템",
  description: "토스 페이먼츠를 활용한 결제 시스템",
  charset: "UTF-8",
  viewport: "width=device-width, initial-scale=1",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <body className="antialiased">
        <ErrorTrackerInit />
        <MonitoringProvider>
          <QueryProvider>
            <ErrorBoundaryWrapper>{children}</ErrorBoundaryWrapper>
          </QueryProvider>
        </MonitoringProvider>
      </body>
    </html>
  );
}
