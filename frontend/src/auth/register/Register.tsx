import { useState } from 'react';
import { Alert } from 'reactstrap';
import { useForm, SubmitHandler } from 'react-hook-form';
import '../../static/css/auth/authButton.css';
import '../../static/css/auth/authPage.css';
import tokenService from '../../services/token.service.ts';
import BotonLink from '../../util/BotonLink.tsx';
import { FormInput } from '../../components/Form/FormInput.tsx';

interface RegisterFormInputs {username:string, password:string, firstName:string, lastName:string, authority: 'PLAYER' | 'ADMIN'}

const validators = {
  username: {
    required: 'Username is required',
    pattern: {
      value: /^[a-zA-Z0-9_]{3,20}$/,
      message: 'Username must be 3-20 characters, letters, numbers, or underscore'
    }
  },
  password: {
    required: 'Password is required',
    minLength: {
      value: 3,
      message: 'Password must be at least 3 characters'
    }
  },
  authority: {
    required: 'Role is required'
  }
};

export default function Register() {
  const [message, setMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedRole, setSelectedRole] = useState<'PLAYER' | 'ADMIN' | ''>('');

  const { register, handleSubmit, formState: { errors }, setValue } = useForm<RegisterFormInputs>({
    defaultValues: {
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      authority: 'PLAYER'
    }
  });

  const handleRoleSelect = (role: 'PLAYER' | 'ADMIN') => {
    setSelectedRole(role);
    setValue('authority', role);
  };

  const onSubmit: SubmitHandler<RegisterFormInputs> = async (data) => {
    setIsSubmitting(true);
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
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-page-container">
      {message && <Alert color="danger">{message}</Alert>}

      <h1>Register</h1>

      <div className="auth-form-container">
        <form onSubmit={handleSubmit(onSubmit)}>
          
          <FormInput tag="Username" name="username" type="text" register={register} errors={errors} isRequired={true} 
          validators={validators.username}/>

          <FormInput tag="Password" name="password" type="password" register={register} errors={errors} isRequired={true}
            validators={validators.password}/>

          <FormInput tag="First Name" name="firstName" type="text" register={register} errors={errors} isRequired={true}/>

          <FormInput tag="Last Name" name="lastName" type="text" register={register} errors={errors} isRequired={true}/>

          <div className="class-form-group" style={{ marginTop: '20px', marginLeft: '200px' }}>
            <label style={{ display: 'block', marginBottom: '10px' }}>Select Role:</label>
            <div style={{ display: 'flex', gap: '20px', marginTop: '10px', marginLeft: '-55px' }}>
              <button type="button" onClick={() => handleRoleSelect('PLAYER')}
                style={{padding: '10px 20px', backgroundColor: selectedRole === 'PLAYER' ? '#4CAF50' : '#ddd',
                  color: selectedRole === 'PLAYER' ? 'white' : 'black', border: 'none', borderRadius: '50px', cursor: 'pointer'}}>
                Player
              </button>
              <button type="button" onClick={() => handleRoleSelect('ADMIN')}
                style={{padding: '10px 20px', backgroundColor: selectedRole === 'ADMIN' ? '#4CAF50' : '#ddd',
                  color: selectedRole === 'ADMIN' ? 'white' : 'black', border: 'none', borderRadius: '50px', cursor: 'pointer'}}>
                Admin
              </button>
            </div>
            {errors.authority && (<span className="class-error-message">{errors.authority.message}</span>)}
          </div>

          <input type="hidden" {...register('authority', validators.authority)} />

          <div className="custom-button-row">
            <button type="submit" className="auth-button" disabled={isSubmitting || !selectedRole}>
              {isSubmitting ? 'Registering...' : 'Register'}
            </button>
          </div>
        </form>
      </div>

      <div className="hero-div">
        <h4>Already have an account?</h4>
        <BotonLink color="success" direction="/login" text="Sign In" />
      </div>
    </div>
  );
}