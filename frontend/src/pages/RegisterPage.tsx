import { useState } from "react";
import useAuth from "../context/useAuth";
import { useNavigate } from "react-router-dom";
import ApiError from "../api/ApiError";

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
    <div>
      <h1>Register</h1>
      <p>Register page</p>
      {errorMessage && <p>{errorMessage}</p>}
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
