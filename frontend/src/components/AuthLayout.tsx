import type { ReactNode } from "react";

interface AuthLayoutProps {
  title: string;
  subtitle: string;
  children: ReactNode;
}

function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  return (
    <main className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Money Manager</h1>
          <h2>{title}</h2>
          <p>{subtitle}</p>
        </div>

        {children}
      </div>
    </main>
  );
}

export default AuthLayout;
