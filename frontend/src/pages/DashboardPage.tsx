import AppLayout from "../components/AppLayout";
import useAuth from "../context/useAuth";

function DashboardPage() {
  const { isAuthenticated, logout } = useAuth();

  return (
    <AppLayout>
      <h2>Dashboard</h2>
      <p>Authenticated: {isAuthenticated ? "Yes" : "No"}</p>

      <button type="button" onClick={logout}>
        Logout
      </button>
    </AppLayout>
  );
}

export default DashboardPage;
