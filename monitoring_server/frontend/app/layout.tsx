import type { Metadata } from "next";
import Link from "next/link";
import type { ReactNode } from "react";
import "@/app/globals.css";

export const metadata: Metadata = {
  title: "Spring Monitoring Admin",
  description: "Admin dashboard for Actuator, Spring Boot Admin, Prometheus, Grafana, PostgreSQL, and Redis.",
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
                Spring Monitoring
              </Link>
            </div>

            <nav className="site-nav">
              <Link href="/">Overview</Link>
              <Link href="/statistics">Statistics</Link>
              <Link href="/actuator">Actuator</Link>
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
