import { useState, useEffect } from 'react';
import tokenService from '../services/token.service.ts';
import Login from '../auth/login/Login.tsx';

const PrivateRoute = ({ children }: {children: React.ReactNode}) => {
  const jwt = tokenService.getLocalAccessToken();
  const [isLoading, setIsLoading] = useState(true);
  const [isValid, setIsValid] = useState(false);

  useEffect(() => {
    if (jwt) {
      fetch(`/api/v1/auth/validate?token=${encodeURIComponent(jwt)}`, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
      })
        .then((response) => response.json())
        .then((data) => {
          if (typeof data === 'object' && 'message' in data) {
            setIsValid(false);
          } else {
            setIsValid(data);
          }
          setIsLoading(false);
        })
        .catch((err) => {
          console.error('Error validating token:', err);
          setIsValid(false);
          setIsLoading(false);
        });
    } else {
      setIsLoading(false);
      setIsValid(false);
    }
  }, [jwt]);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return isValid ? <> {children}</> : <Login />;
};

export default PrivateRoute;