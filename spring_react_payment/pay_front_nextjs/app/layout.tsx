import type { Metadata } from "next";
import "./globals.css";

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
        {children}
      </body>
    </html>
  );
}
