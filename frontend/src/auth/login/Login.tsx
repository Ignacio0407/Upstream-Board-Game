import { Alert } from 'reactstrap';
import { useState } from 'react';
import tokenService from '../../services/token.service.ts';
import ButtonLink from '../../components/ButtonLink/ButtonLink.tsx';
import { FormGenerator } from '../../components/Form/FormGenerator.tsx';
import validators from '../validators.ts';

interface LoginFormInputs {
  username: string;
  password: string;
}

export default function Login() {
  const [message, setMessage] = useState<string | null>(null);

  const loginFields = [
    { tag: "Username", name: "username", type: "text", isRequired: true, validators: validators.username },
    { tag: "Password", name: "password", type: "password", isRequired: true, validators: validators.password }
  ];

  const onSubmit = async (data: LoginFormInputs) => {
    setMessage(null);
    try {
      const response = await fetch("/api/v1/auth/signin", {
        headers: { "Content-Type": "application/json" },
        method: "POST",
        body: JSON.stringify(data)
      });
      if (!response.ok) throw new Error("Invalid login attempt");

      const userData = await response.json();
      tokenService.setUser(userData);
      tokenService.updateLocalAccessToken(userData.token);
      window.location.href = "/dashboard";
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Error in login");
    }
  };

  return (
    <div className="auth-page-container">
      {message && <Alert color="primary">{message}</Alert>}
      <h1>Login</h1>

      <div className="auth-form-container">
        <FormGenerator<LoginFormInputs>
          fields={loginFields}
          defaultValues={{ username: '', password: '' }}
          onSubmit={onSubmit}
          submitText="Login"
          loadingText="Loading..."
          deactivateSecurity={true}
        />
      </div>

      <div className="hero-div">
        <h4>¿Don't have an account yet?</h4>                
        <ButtonLink color="success" direction="/register" text="Register" />
      </div>
    </div>
  );
}