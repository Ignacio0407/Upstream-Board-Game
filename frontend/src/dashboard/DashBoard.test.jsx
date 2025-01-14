jest.mock('sockjs-client', () => jest.fn());
jest.mock('@stomp/stompjs', () => ({
  Client: jest.fn().mockImplementation(() => ({
    activate: jest.fn(),
    subscribe: jest.fn(),
    deactivate: jest.fn(),
  })),
}));

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom'; // Para mockear navegación
import Dashboard from '../components/Dashboard';
import tokenService from '../services/token.service';

// Mock de tokenService para simular autenticación
jest.mock('../services/token.service', () => ({
  getLocalAccessToken: jest.fn(() => 'mock-jwt-token'),
  getUser: jest.fn(() => ({ id: 1, roles: ['ADMIN'] })),
}));

jest.mock('../util/useFetchState', () =>
  jest.fn((initialState, url, jwt) => [
    initialState, // matches y originalMatches
    jest.fn(),    // setter
  ])
);

// Mock de navigate
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedNavigate,
}));

describe('Dashboard Component', () => {
  beforeEach(() => {
    jest.clearAllMocks(); // Limpiar mocks antes de cada test
  });

  it('renders the dashboard with user information', () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    // Verificar que se muestra el mensaje de bienvenida
    expect(screen.getByText(/Game Listing for/)).toBeInTheDocument();

    // Verificar que el botón de "Create Game" está presente
    expect(screen.getByText('Create Game')).toBeInTheDocument();
  });

  it('navigates to the create game page when clicking the button', () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    // Click en el botón de "Create Game"
    fireEvent.click(screen.getByText('Create Game'));

    // Verificar que se navegó a la dirección correcta
    expect(mockedNavigate).toHaveBeenCalledWith('/creategame');
  });

  it('renders the matches table correctly', () => {
    // Mockear datos de partidas
    jest.mock('../util/useFetchState', () =>
      jest.fn(() => [
        [
          {
            id: 1,
            name: 'Match 1',
            playersNum: 4,
            state: 'ESPERANDO',
            password: '',
          },
        ],
        jest.fn(),
      ])
    );

    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    // Verificar que la tabla de partidas está renderizada
    expect(screen.getByText('Match 1')).toBeInTheDocument();
    expect(screen.getByText('Join game')).toBeInTheDocument();
    expect(screen.getByText('Spectate game')).toBeInTheDocument();
  });
});