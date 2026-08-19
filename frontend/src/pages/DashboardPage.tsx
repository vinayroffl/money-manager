import useAuth from "../context/useAuth";

function DashboardPage() {
  const { isAuthenticated, logout } = useAuth();

  console.log("Dashboard authentication:", isAuthenticated);

  return (
    <div>
      <h1>Dashboard</h1>
      <p>Authenticated: {isAuthenticated ? "Yes" : "No"}</p>
      <button type="button" onClick={logout}>
        Logout
      </button>
    </div>
  );
}

export default DashboardPage;
