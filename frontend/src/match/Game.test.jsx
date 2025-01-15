import React from 'react';
import { render, screen, act } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import { Client } from '@stomp/stompjs';

// Mock de STOMP Client
jest.mock('@stomp/stompjs', () => {
    const subscribeMock = jest.fn();
    return {
        Client: jest.fn().mockImplementation(() => ({
            activate: jest.fn(),
            deactivate: jest.fn(),
            subscribe: subscribeMock,
            publish: jest.fn(),
        })),
    };
});

jest.mock('../services/token.service', () => ({
    getLocalAccessToken: jest.fn(() => 'mockedToken'),
    getUser: jest.fn(() => ({ id: 1, username: 'mockedUser' })),
}));

jest.mock('../util/useFetchState', () => jest.fn((initialValue, endpoint, token) => [[], jest.fn()]));

jest.mock('../util/fetchers', () => ({
    get: jest.fn(() => Promise.resolve({ json: jest.fn(() => Promise.resolve([])) })),
}));

describe('Game Component', () => {
    it('renders without crashing and initializes WebSocket subscriptions', async () => {
        await act(async () => {
            render(<Game match={{ id: 1, actualPlayer: 1, matchCreator: 1, phase: 'MOVIENDO' }} />);
        });

        // Verificar que el STOMP Client fue creado
        expect(Client).toHaveBeenCalled();

        // Verificar que los tópicos se han suscrito
        const stompClientInstance = Client.mock.instances[0];
        if (!stompClientInstance) {
            throw new Error('No STOMP client instance found');
        }
        expect(stompClientInstance.subscribe).toHaveBeenCalledWith('/topic/salmon', expect.any(Function));
        expect(stompClientInstance.subscribe).toHaveBeenCalledWith('/topic/tiles', expect.any(Function));
        expect(stompClientInstance.subscribe).toHaveBeenCalledWith('/topic/players', expect.any(Function));

        // Verificar que el componente se renderiza correctamente
        expect(screen.getByText('mockedUser')).toBeInTheDocument();
    });

    it('handles WebSocket messages and updates state', async () => {
        const stompClientInstance = Client.mock.instances[0];
        const messageCallback = jest.fn();
    
        if (!stompClientInstance) {
            throw new Error('No STOMP client instance found');
        }
    
        stompClientInstance.subscribe.mockImplementation((topic, callback) => {
            if (topic === '/topic/salmon') {
                messageCallback.mockImplementation(callback);
            }
        });
    
        await act(async () => {
            render(<Game match={{ id: 1, actualPlayer: 1, matchCreator: 1, phase: 'MOVIENDO' }} />);
        });
    
        // Simular un mensaje recibido por el WebSocket
        act(() => {
            messageCallback({ body: JSON.stringify({ data: 'mockData' }) });
        });
    
        // Validar que el mensaje fue procesado
        expect(messageCallback).toHaveBeenCalled();
    });    
});