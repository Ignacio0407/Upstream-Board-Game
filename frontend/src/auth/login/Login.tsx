import { useState } from 'react';
import { Alert } from 'reactstrap';
import { useForm, SubmitHandler } from 'react-hook-form';
import '../../static/css/auth/authButton.css';
import tokenService from '../../services/token.service.ts';
import BotonLink from '../../util/BotonLink.tsx';
import { FormInput } from '../../components/Form/FormInput.tsx';

interface LoginFormInputs {
  username: string;
  password: string;
}

export default function Login() {
  const [message, setMessage] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormInputs>({
    defaultValues: {
      username: '',
      password: ''
    }
  });

  const onSubmit: SubmitHandler<LoginFormInputs> = async (data) => {
    setMessage(null);
    
    try {
      const response = await fetch("/api/v1/auth/signin", {
        headers: { "Content-Type": "application/json" },
        method: "POST",
        body: JSON.stringify(data)});
      
      if (!response.ok) {
        throw new Error("Invalid login attempt");
      }
      
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
      {message && (
        <Alert color="primary">{message}</Alert>
      )}

      <h1>Login</h1>

      <div className="auth-form-container">
        <form onSubmit={handleSubmit(onSubmit)}>
          <FormInput
            tag="Username"
            name="username"
            type="text"
            register={register}
            errors={errors}
            isRequired={true}
          />
          
          <FormInput
            tag="Password"
            name="password"
            type="password"
            register={register}
            errors={errors}
            isRequired={true}
          />
          
          <div className="custom-button-row">
            <button 
              type="submit" 
              className="auth-button"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Cargando...' : 'Login'}
            </button>
          </div>
        </form>
      </div>
      
      <div className="hero-div">
        <h4>¿No tienes cuenta?</h4>                
        <BotonLink color="success" direction="/register" text="Regístrate" />
      </div>
    </div>
  );
}