import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import useAuth from "../context/useAuth";
import ApiError from "../api/ApiError";
import AuthLayout from "../components/AuthLayout";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async () => {
    setErrorMessage("");

    const request = {
      email,
      password,
    };
    try {
      await login(request);
      navigate("/dashboard");
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Something went wrong. Please try again.");
      }
    }
  };
  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to manage your finances">
      <form
        className="auth-form"
        onSubmit={(event) => {
          event.preventDefault();
          void handleLogin();
        }}
      >
        {errorMessage && <div className="auth-error">{errorMessage}</div>}

        <div className="form-field">
          <label htmlFor="email">Email</label>

          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="password">Password</label>

          <input
            id="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </div>

        <button className="auth-button" type="submit">
          Login
        </button>

        <p className="auth-footer">
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </form>
    </AuthLayout>
  );
}

export default LoginPage;
