import type { Metadata } from "next";
import Link from "next/link";
import type { ReactNode } from "react";
import "@/app/globals.css";

export const metadata: Metadata = {
  title: "스프링 모니터링 관리자",
  description: "Actuator, Spring Boot Admin, Prometheus, Grafana, PostgreSQL, Redis를 위한 관리자 대시보드입니다.",
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="ko">
      <body>
        <div className="site-backdrop" />
        <div className="site-shell">
          <header className="site-header">
            <div className="brand-block">
              <p className="brand-kicker">IdolGlow Lab</p>
              <Link href="/" className="brand-name">
                스프링 모니터링
              </Link>
            </div>

            <nav className="site-nav">
              <Link href="/">개요</Link>
              <Link href="/statistics">통계</Link>
              <Link href="/actuator">액추에이터</Link>
            </nav>

            <div className="header-pills">
              <span>JDK 25</span>
              <span>Spring Boot 4.0.3</span>
              <span>PostgreSQL + Redis</span>
              <span>Prometheus + Grafana</span>
            </div>
          </header>

          <main className="page-frame">{children}</main>
        </div>
      </body>
    </html>
  );
}
