import type { ReactNode } from "react";
import useAuth from "../context/useAuth";
import { NavLink } from "react-router-dom";

interface AppLayoutProps {
  children: ReactNode;
}

function AppLayout({ children }: AppLayoutProps) {
  const { logout } = useAuth();

  return (
    <div className="app-layout">
      <header className="app-header">
        <h1>Money Manager</h1>

        <button type="button" onClick={logout}>
          Logout
        </button>
      </header>

      <div className="app-body">
        <aside className="app-sidebar">
          <nav>
            <NavLink
              to="/dashboard"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Dashboard
            </NavLink>
            <NavLink
              to="/transactions"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Transactions
            </NavLink>

            <NavLink
              to="/categories"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Categories
            </NavLink>

            <NavLink
              to="/budgets"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Budgets
            </NavLink>
          </nav>
        </aside>

        <main className="app-content">{children}</main>
      </div>
    </div>
  );
}

export default AppLayout;
