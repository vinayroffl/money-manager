import { useState } from "react";
import useAuth from "../context/useAuth";
import { Link, useNavigate } from "react-router-dom";
import ApiError from "../api/ApiError";
import AuthLayout from "../components/AuthLayout";

function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleRegister = async () => {
    setErrorMessage("");
    const request = {
      email,
      firstName,
      lastName,
      password,
    };
    try {
      await register(request);
      navigate("/login");
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("Something went wrong. Please try again.");
      }
    }
  };
  return (
    <AuthLayout
      title="Create your account"
      subtitle="Start managing your finances today"
    >
      <form
        className="auth-form"
        onSubmit={(event) => {
          event.preventDefault();
          void handleRegister();
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
          <label htmlFor="firstName">First Name</label>
          <input
            id="firstName"
            type="text"
            value={firstName}
            onChange={(event) => setFirstName(event.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="lastName">Last Name</label>
          <input
            id="lastName"
            type="text"
            value={lastName}
            onChange={(event) => setLastName(event.target.value)}
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
          Register
        </button>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </form>
    </AuthLayout>
  );
}

export default RegisterPage;
