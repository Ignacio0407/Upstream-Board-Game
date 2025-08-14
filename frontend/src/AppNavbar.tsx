import React, { useState, useEffect, JSX } from 'react';
import { Navbar, NavbarBrand, NavLink, NavItem, Nav, NavbarText, NavbarToggler, Collapse } from 'reactstrap';
import { Link } from 'react-router-dom';
import tokenService from './services/token.service.ts';
import jwt_decode from 'jwt-decode';
import { post } from './util/fetchers.ts';
import {JwtPayload} from './interfaces/util'

const AppNavbar: React.FC = () => {
  const [roles, setRoles] = useState<string[]>([]);
  const [username, setUsername] = useState<string>('');
  const jwt: string|null = tokenService.getLocalAccessToken();
  const [collapsed, setCollapsed] = useState<boolean>(true);

  const toggleNavbar = () => setCollapsed((prev) => !prev);

  useEffect(() => {
    if (jwt) {
      try {
        const decoded = jwt_decode<JwtPayload>(jwt);
        setRoles(decoded.authorities);
        setUsername(decoded.sub);
      } catch (error) {
        console.error('Failed to decode JWT', error);
      }
    }
  }, [jwt]);

  const unlockAchievement = async () => {
    try {
      const response: Response = await post(`/api/v1/usersachievements/unlockrules/${username}`, jwt);
      if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
      }
      console.log('Achievement unlocked successfully!');
    } catch (error) {
      console.error('Failed to unlock achievement:', error);
    }
  };

  // Renderizado condicional de enlaces
  let adminLinks: JSX.Element = <></>;
  let ownerLinks: JSX.Element = <></>;
  let userLinks: JSX.Element = <></>;
  let userLogout: JSX.Element = <></>;
  let publicLinks: JSX.Element = <></>;

  roles.forEach((role: string) => {
    if (role === 'ADMIN') {
      adminLinks = (
        <>
          <NavItem>
            <NavLink style={{ color: 'white' }} tag={Link} to="/users">
              Users
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink style={{ color: 'white' }} tag={Link} to="/developers">
              Developers
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink style={{ color: 'white' }} tag={Link} to="/achievements">
              Achievements
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink style={{ color: 'white' }} id="docs" tag={Link} to="/docs">
              Docs
            </NavLink>
          </NavItem>
        </>
      );
    }
    if (role === 'PLAYER') {
      ownerLinks = (
        <>
          <NavItem>
            <NavLink style={{ color: 'white' }} tag={Link} to="/achievements">
              Achievements
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink style={{ color: 'white' }} tag={Link} to="/developers">
              Developers
            </NavLink>
          </NavItem>
        </>
      );
    }
  });

  if (!jwt) {
    publicLinks = (
      <>
        <NavItem>
          <NavLink style={{ color: 'white' }} id="plans" tag={Link} to="/plans">
            Pricing Plans
          </NavLink>
        </NavItem>
        <NavItem>
          <NavLink style={{ color: 'white' }} id="register" tag={Link} to="/register">
            Register
          </NavLink>
        </NavItem>
        <NavItem>
          <NavLink style={{ color: 'white' }} id="login" tag={Link} to="/login">
            Login
          </NavLink>
        </NavItem>
      </>
    );
  } else {
    userLinks = (
      <NavItem>
        <NavLink style={{ color: 'white' }} tag={Link} to="/dashboard">
          Dashboard
        </NavLink>
      </NavItem>
    );

    userLogout = (
      <>
        <NavItem>
          <NavLink style={{ color: 'white' }} id="rules" tag={Link} to="/rules" onClick={unlockAchievement}>
            Rules
          </NavLink>
        </NavItem>
        <NavItem>
          <NavLink style={{ color: 'white' }} id="plans" tag={Link} to="/plans">
            Pricing Plans
          </NavLink>
        </NavItem>
        <NavbarText style={{ color: 'white' }} className="justify-content-end">
          {username}
        </NavbarText>
        <NavItem className="d-flex">
          <NavLink style={{ color: 'white' }} id="logout" tag={Link} to="/logout">
            Logout
          </NavLink>
        </NavItem>
      </>
    );
  }

  return (
    <div>
      <Navbar expand="md" dark color="success">
        <NavbarBrand href="/">
          <img src="/images/salmonLogo.png" alt="Logo" style={{ height: 40, width: 40, marginRight: 5 }} />
          Upstream
        </NavbarBrand>
        <NavbarToggler onClick={toggleNavbar} className="ms-2" />
        <Collapse isOpen={!collapsed} navbar>
          <Nav className="me-auto mb-2 mb-lg-0" navbar>
            {userLinks}
            {adminLinks}
            {ownerLinks}
          </Nav>
          <Nav className="ms-auto mb-2 mb-lg-0" navbar>
            {publicLinks}
            {userLogout}
          </Nav>
        </Collapse>
      </Navbar>
    </div>
  );
};

export default AppNavbar;