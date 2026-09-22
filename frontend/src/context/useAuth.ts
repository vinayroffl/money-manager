import { useContext } from "react";
import { AuthContext } from "./authContextValue";

function useAuth() {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
}

export default useAuth;
