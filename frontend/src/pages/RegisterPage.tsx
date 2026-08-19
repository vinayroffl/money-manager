import { useState } from "react";
import useAuth from "../context/useAuth";
import { useNavigate } from "react-router-dom";

function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleRegister = async () => {
    const request = {
      email,
      firstName,
      lastName,
      password,
    };
    await register(request);
    navigate("/dashboard");
  };
  return (
    <div>
      <h1>Register</h1>
      <p>Register page</p>
      <input
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
      />
      <input
        type="text"
        value={firstName}
        onChange={(event) => setFirstName(event.target.value)}
      />
      <input
        type="text"
        value={lastName}
        onChange={(event) => setLastName(event.target.value)}
      />
      <input
        type="password"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
      />
      <button type="button" onClick={handleRegister}>
        register
      </button>
    </div>
  );
}

export default RegisterPage;
