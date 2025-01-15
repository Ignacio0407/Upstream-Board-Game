import React from 'react';
import Chat from "./Chat"
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { get, post } from '../util/fetchers';

jest.mock('../util/fetchers', () => ({
  get: jest.fn(),
  post: jest.fn(),
}));

jest.mock('../services/token.service', () => ({
  getLocalAccessToken: jest.fn(() => 'mock-jwt-token'),
}));

jest.mock('sockjs-client', () => jest.fn());
jest.mock('@stomp/stompjs', () => ({
  Client: jest.fn().mockImplementation(() => ({
    activate: jest.fn(),
    subscribe: jest.fn(),
    publish: jest.fn(),
    deactivate: jest.fn(),
  })),
}));

describe('Chat Component', () => {
  const mockMatch = { id: 1 };
  const mockPlayers = [
    { id: 1, color: 'blue', user: { username: 'Player1' } },
    { id: 2, color: 'red', user: { username: 'Player2' } },
  ];
  const mockCurrentPlayer = { id: 1 };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the Chat component and loads messages', async () => {
    console.log('Test: renders the Chat component and loads messages');

    const mockMessages = [
      {
        id: 1,
        content: 'Hello World',
        player: { id: 2, user: { username: 'Player2' } },
        match: { id: 1}
      },
    ];

    get.mockResolvedValueOnce({
      json: jest.fn().mockResolvedValueOnce(mockMessages),
    });

    render(
      <Chat
        match={mockMatch}
        players={mockPlayers}
        currentPlayer={mockCurrentPlayer}
      />
    );

    console.log('Component rendered');

    // Verificar que el chat no es visible inicialmente
    expect(screen.queryByText('Chat de la partida')).not.toBeInTheDocument();

    // Simular clic para mostrar el chat
    const chatTab = screen.getByText('Chat');
    fireEvent.click(chatTab);
    console.log('Chat tab clicked');

    // Esperar que los mensajes sean cargados y renderizados
    await waitFor(() => {
      expect(screen.getByText('Chat de la partida')).toBeInTheDocument();
    });

    console.log('Messages loaded and rendered');
    expect(screen.getByText('Hello World')).toBeInTheDocument();
    expect(screen.getByText('Player2')).toBeInTheDocument();
  });

  it('sends a message when the form is submitted', async () => {
    console.log('Test: sends a message when the form is submitted');

    get.mockResolvedValueOnce({ json: jest.fn().mockResolvedValueOnce([]) });
    post.mockResolvedValueOnce({});

    render(
      <Chat
        match={mockMatch}
        players={mockPlayers}
        currentPlayer={mockCurrentPlayer}
      />
    );

    // Mostrar el chat
    fireEvent.click(screen.getByText('Chat'));
    console.log('Chat tab clicked');

    // Simular escritura de un mensaje y envío
    const input = screen.getByPlaceholderText('Escribe un mensaje...');
    fireEvent.change(input, { target: { value: 'Test Message' } });
    console.log('Input changed:', input.value);

    const sendButton = screen.getByText('Enviar');
    fireEvent.click(sendButton);
    console.log('Send button clicked');

    // Verificar que el mensaje fue enviado
    await waitFor(() => {
      expect(post).toHaveBeenCalledWith('/api/v1/messages', 'mock-jwt-token', {
        playerId: 1,
        matchId: 1,
        newMessage: 'Test Message',
      });
    });

    console.log('Message sent');
    // Verificar que el campo de texto se vacía después de enviar
    expect(input.value).toBe('');
  });
});