import { useState } from 'react';
import { Alert } from 'reactstrap';
import { SubmitHandler } from 'react-hook-form';
import '../authButton.css';
import '../authPage.css';
import tokenService from '../../services/token.service.ts';
import ButtonLink from '../../components/ButtonLink/ButtonLink.tsx';
import validators from '../validators.ts';
import { FormGenerator } from '../../components/Form/FormGenerator.tsx';
import { Authority } from '../../interfaces/User.ts';

interface RegisterFormInputs {username:string, password:string, firstName:string, lastName:string, authority: Authority.PLAYER | Authority.ADMIN}

export default function Register() {
  const [message, setMessage] = useState<string | null>(null);
  const [selectedRole, setSelectedRole] = useState<Authority.PLAYER | Authority.ADMIN | ''>('');

  const registerFields = [
    { tag: "Username", name: "username", type: "text", isRequired: true, validators: validators.username },
    { tag: "Password", name: "password", type: "password", isRequired: true, validators: validators.password },
    { tag: "First Name", name: "firstName", type: "text", isRequired: true },
    { tag: "Last Name", name: "lastName", type: "text", isRequired: true }
  ];

  const onSubmit: SubmitHandler<RegisterFormInputs> = async (data) => {
    setMessage(null);

    try {
      const request = {...data, authority: selectedRole};

      const registerResponse = await fetch("/api/v1/auth/signup", {
        headers: { "Content-Type": "application/json" },
        method: "POST",
        body: JSON.stringify(request),
      });

      if (!registerResponse.ok) {
        const errorData = await registerResponse.json();
        throw new Error(errorData.message || "Registration failed");
      }

      const loginRequest = {username: data.username, password: data.password};

      const loginResponse = await fetch("/api/v1/auth/signin", {
        headers: { "Content-Type": "application/json" },
        method: "POST",
        body: JSON.stringify(loginRequest),
      });

      if (!loginResponse.ok) {
        throw new Error("Login failed after registration");
      }

      const userData = await loginResponse.json();
      tokenService.setUser(userData);
      tokenService.updateLocalAccessToken(userData.token);
      window.location.href = "/dashboard";

    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Registration failed");
    }
  };

  return (
    <div className="auth-page-container">
      {message && <Alert color="danger">{message}</Alert>}
      <h1>Register</h1>

      <div className="auth-form-container">
        <FormGenerator<RegisterFormInputs>
          fields={registerFields}
          defaultValues={{username: '', password: '', firstName: '', lastName: '', authority: Authority.PLAYER}}
          onSubmit={onSubmit}
          submitText="Register"
          loadingText="Registering..."
          deactivateSecurity={true}
          extraBeforeSubmit={
              <div className="class-form-group" style={{ marginTop: '1.25rem' }}>
                <span style={{ display: 'block', marginBottom: '0.625rem', fontWeight: 'bold' }}>Select Role:</span>
                {/* flexWrap keeps the buttons usable on very narrow screens */}
                <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '1.25rem', marginTop: '0.625rem' }}>
                  <button type="button" onClick={() => setSelectedRole(Authority.PLAYER)}
                    style={{ padding: '0.625rem 1.25rem', border: 'none', borderRadius: '999px', cursor: 'pointer',
                      backgroundColor: selectedRole === Authority.PLAYER ? '#4CAF50' : '#ddd',
                      color: selectedRole === Authority.PLAYER ? 'white' : 'black' }}>
                    Player
                  </button>
                  <button type="button" onClick={() => setSelectedRole(Authority.ADMIN)}
                    style={{ padding: '0.625rem 1.25rem', border: 'none', borderRadius: '999px', cursor: 'pointer',
                      backgroundColor: selectedRole === Authority.ADMIN ? '#4CAF50' : '#ddd',
                      color: selectedRole === Authority.ADMIN ? 'white' : 'black' }}>
                    Admin
                  </button>
                </div>
              </div>
            }
        />
      </div>

      <div className="hero-div">
        <h4>Already have an account?</h4>
        <ButtonLink color="success" direction="/login" text="Sign In" />
      </div>
    </div>
  );
}
