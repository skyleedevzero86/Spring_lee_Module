import type { Metadata } from "next";
import "./globals.css";
import { ErrorBoundaryWrapper } from '@/src/components/common/ErrorBoundaryWrapper';
import { QueryProvider } from '@/src/providers/QueryProvider';
import { MonitoringProvider } from '@/src/components/monitoring/MonitoringProvider';
import { ErrorTrackerInit } from '@/src/components/common/ErrorTrackerInit';

export const metadata: Metadata = {
  title: "토스 페이먼츠 결제 시스템",
  description: "토스 페이먼츠를 활용한 결제 시스템",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
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
