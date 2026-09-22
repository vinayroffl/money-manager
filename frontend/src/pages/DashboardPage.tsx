import AppLayout from "../components/AppLayout";
import useAuth from "../context/useAuth";

function DashboardPage() {
  const { isAuthenticated } = useAuth();

  return (
    <AppLayout>
      <h2>Dashboard</h2>
      <p>Authenticated: {isAuthenticated ? "Yes" : "No"}</p>
    </AppLayout>
  );
}

export default DashboardPage;
