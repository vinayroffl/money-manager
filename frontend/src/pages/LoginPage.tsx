import { useState } from "react";
import { useNavigate } from "react-router-dom";
import useAuth from "../context/useAuth";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async () => {
    const request = {
      email,
      password,
    };
    await login(request);
    navigate("/dashboard");
  };
  return (
    <div>
      <h1>Login</h1>
      <p>Login page</p>
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
