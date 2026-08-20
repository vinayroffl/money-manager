import { useState } from "react";
import { useNavigate } from "react-router-dom";
import useAuth from "../context/useAuth";
import ApiError from "../api/ApiError";

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
    <div>
      <h1>Login</h1>
      <p>Login page</p>
      {errorMessage && <p>{errorMessage}</p>}
      <input
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
      />
      <input
        type="password"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
      />
      <button type="button" onClick={handleLogin}>
        Login
      </button>
    </div>
  );
}

export default LoginPage;
