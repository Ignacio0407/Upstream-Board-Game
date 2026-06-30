import React from 'react';
import { Route, Routes } from 'react-router-dom';
import jwt_decode from 'jwt-decode';
import { ErrorBoundary } from 'react-error-boundary';
import AppNavbar from './AppNavbar.tsx';
import Home from './home/Home.tsx';
import PrivateRoute from './privateRoute/PrivateRoute.tsx';
import Register from './auth/register/Register.tsx';
import Login from './auth/login/Login.tsx';
import Logout from './auth/logout/Logout.tsx';
import PlanList from './public/plan/PlanList.tsx';
import tokenService from './services/token.service.ts';
import UserListAdmin from './admin/users/UserListAdmin.tsx';
import SwaggerDocs from './public/swagger/Swagger.tsx';
import DeveloperList from './developers/developers.tsx';
import AchievementList from './achievements/achievementList/AchievementList.tsx';
import AchievementEdit from './achievements/achievementEdit/AchievementEdit.tsx';
import Dashboard from './dashboard/Dashboard.tsx';
import CreateGame from './createGame/CreateGame.tsx';
import Rules from './rules/rules.tsx';
import Game from './match/Game.tsx';
import Lobby from './lobby/Lobby.tsx';
import EndGame from './match/EndGame.tsx';
import {JwtPayload} from './interfaces/util'
import DoNotHackMyApp from './components/DoNotHackMyApp/DoNotHackMyApp.tsx';
import AchievementCreate from './achievements/achievementCreate/AchievementCreate.tsx';
import { Authority } from './interfaces/User.ts';

const ErrorFallback: React.FC<{error: Error; resetErrorBoundary: () => void;}> = ({ error, resetErrorBoundary }) => {
  return (
    <div role="alert">
      <p>Something went wrong:</p>
      <pre>{error.message}</pre>
      <button onClick={resetErrorBoundary}>Try again</button>
    </div>
  );
};

const getRolesFromJWT = (token: string): string[] => {
    return jwt_decode<JwtPayload>(token).authorities;
  };

const App: React.FC = () => {
  const jwt: string|null = tokenService.getLocalAccessToken();
  let roles: string[] = [];

  if (jwt) {
    try {
      roles = getRolesFromJWT(jwt);
    } catch (e) {
      console.error('Invalid JWT token');
    }
  }

  let adminRoutes: React.ReactNode = null;
  let playerRoutes: React.ReactNode = null;
  let userRoutes: React.ReactNode = null;
  let publicRoutes: React.ReactNode = null;

  roles.forEach((role: string) => {
    if (role === Authority.ADMIN) {
      adminRoutes = (
        <>
          <Route path="/rules" element={<Rules />} />
          <Route path="/users" element={<PrivateRoute><UserListAdmin /></PrivateRoute>} />
          <Route path="/developers" element={<DeveloperList />} />
          <Route path="/achievements/" element={<PrivateRoute><AchievementList /></PrivateRoute>} />
          <Route path="/achievements/:achievementId" element={<PrivateRoute><AchievementEdit /></PrivateRoute>} />
          <Route path="/achievements/new" element={<PrivateRoute><AchievementCreate /></PrivateRoute>} />
          <Route path="/matches/:matchId/waiting" element={<PrivateRoute><Lobby /></PrivateRoute>} />
          <Route path="/matches/:matchId/game" element={<PrivateRoute><Game /></PrivateRoute>} />
          <Route path="/matches/:matchId/end" element={<PrivateRoute><EndGame /></PrivateRoute>} />
          <Route path="/creategame/" element={<CreateGame />} />
          <Route path="/docs" element={<SwaggerDocs />} />
          <Route path="/do-not-hack-my-app" element={<DoNotHackMyApp/>} />
        </>
      );
    }
    if (role === Authority.PLAYER) {
      playerRoutes = (
        <>
          <Route path="/rules" element={<Rules />} />
          <Route path="/matches/:matchId/waiting" element={<PrivateRoute><Lobby /></PrivateRoute>} />
          <Route path="/matches/:matchId/game" element={<PrivateRoute><Game /></PrivateRoute>} />
          <Route path="/matches/:matchId/end" element={<PrivateRoute><EndGame /></PrivateRoute>} />
          <Route path="/achievements/" element={<PrivateRoute><AchievementList /></PrivateRoute>} />
          <Route path="/creategame/" element={<CreateGame />} />
          <Route path="/developers" element={<DeveloperList />} />
          <Route path="/do-not-hack-my-app" element={<DoNotHackMyApp/>} />
        </>
      );
    }
  });

  if (!jwt) {
    publicRoutes = (
      <>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/rules" element={<Rules />} />
        <Route path="/do-not-hack-my-app" element={<DoNotHackMyApp/>} />
      </>
    );
  } else {
    userRoutes = (
      <>
        <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/logout" element={<Logout />} />
        <Route path="/login" element={<Login />} />
        <Route path="/rules" element={<Rules />} />
        <Route path="/do-not-hack-my-app" element={<DoNotHackMyApp/>} />
      </>
    );
  }

  return (
    <div>
      <ErrorBoundary FallbackComponent={ErrorFallback}>
        <AppNavbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/plans" element={<PlanList />} />
          {publicRoutes}
          {userRoutes}
          {adminRoutes}
          {playerRoutes}
        </Routes>
      </ErrorBoundary>
    </div>
  );
};

export default App;