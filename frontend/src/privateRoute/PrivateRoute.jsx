import { useState, useEffect } from 'react';
import tokenService from '../services/token.service.ts';
import Login from '../auth/login/Login.tsx';

//interface PrivateRouteProps {children: React.ReactNode};
// : React.FC<PrivateRouteProps>
const PrivateRoute = ({ children }) => {
  const jwt = tokenService.getLocalAccessToken();
  const [isLoading, setIsLoading] = useState(true);
  const [isValid, setIsValid] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (jwt) {
      fetch(`/api/v1/auth/validate?token=${jwt}`, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
      })
        .then((response) => response.json())
        .then((data) => {
        //.then((data: boolean | { message?: string }) => {
          if (typeof data === 'object' && 'message' in data) {
            setMessage(data.message || 'Your token has expired. Please, sign in again.');
            setIsValid(false);
          } else {
            //setIsValid(data as boolean);
            setIsValid(data);
            if (!data) {
              setMessage('Your token has expired. Please, sign in again.');
            }
          }
          setIsLoading(false);
        })
        .catch((err) => {
          console.error('Error validating token:', err);
          setMessage('Authentication failed. Please log in again.');
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

  return isValid ? <> {children}</> : <Login message={message} navigation={!jwt} />;
};

export default PrivateRoute;