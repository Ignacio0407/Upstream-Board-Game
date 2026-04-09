import { vi, describe, test, expect, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import Chat from './Chat';
import * as fetchersModule from '../../util/fetchers';
import WebSocketManager from '../../util/webSocketManager';
import * as colorParser from '../../util/ColorParser';
import Match from '../../interfaces/Match';
import Player, { emptyPlayer } from '../../interfaces/Player';
import Message from '../../interfaces/Message';
import { State } from '../../interfaces/State';
import { Phase } from '../../interfaces/Phase';

// ─── Mocks ───────────────────────────────────────────────────────────────────

vi.mock('sockjs-client', () => ({ default: vi.fn() }));

const mockClientInstance = {
  activate: vi.fn(),
  deactivate: vi.fn(),
  subscribe: vi.fn(),
  active: false,
};

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn().mockImplementation(function () {
    return mockClientInstance;
  }),
}));

vi.mock('../../services/token.service', () => ({
  default: { getLocalAccessToken: vi.fn(() => 'mock-jwt') },
}));

vi.mock('../../util/fetchers', () => ({
  post: vi.fn().mockResolvedValue({ ok: true }),
}));

// ─── Fixtures ────────────────────────────────────────────────────────────────

const match: Match = {
  id: 1, name: 'Test Match', state: State.ON_GOING, password: '',
  playersNumber: 2, round: 0, phase: Phase.MOVING,
  finalScoreCalculated: false, matchCreatorId: 0
};

const players: Player[] = [
  { ...emptyPlayer, id: 1, name: 'Alice', color: 'RED' },
  { ...emptyPlayer, id: 2, name: 'Bob', color: 'BLUE' },
];

const currentPlayer: Player = players[0];

const msg1: Message = { id: 1, playerId: 1, playerName: 'Alice', matchId: 1, content: 'Hello!' };
const msg2: Message = { id: 2, playerId: 2, playerName: 'Bob', matchId: 1, content: 'Hi there!' };

// ─── Helpers ─────────────────────────────────────────────────────────────────

const captureSubscriptionHandler = () => {
  let handler: ((data: Message) => void) | null = null;

  vi.spyOn(WebSocketManager.prototype, 'addSubscription')
    .mockImplementation((_topic, h) => { handler = h as any; });

  return { getHandler: () => handler };
};

const renderChat = () =>
  render(<Chat match={match} players={players} currentPlayer={currentPlayer} />);

const openChat = () =>
  fireEvent.click(screen.getByText('Chat'));

// ─── Tests ───────────────────────────────────────────────────────────────────

describe('Chat', () => {

  beforeEach(() => {
    vi.clearAllMocks();
    mockClientInstance.activate.mockReset();
    mockClientInstance.deactivate.mockReset();
  });

  // ── Visibility ─────────────────────────────────────────────────────────────

  describe('Visibility', () => {
    test('Toggles chat panel', () => {
      renderChat();

      expect(screen.queryByText('Match Chat')).not.toBeInTheDocument();

      openChat();
      expect(screen.getByText('Match Chat')).toBeInTheDocument();

      openChat();
      expect(screen.queryByText('Match Chat')).not.toBeInTheDocument();
    });
  });

  // ── Messages ───────────────────────────────────────────────────────────────

  describe('Messages', () => {

    test('Accumulates messages received while chat is hidden', async () => {
      const { getHandler } = captureSubscriptionHandler();
      renderChat(); // Chat still closed
      
      await waitFor(() => expect(getHandler()).not.toBeNull());
      act(() => getHandler()!(msg1)); // Message with closed chat arrives
      
      openChat(); // Open chat and expect message to be there
      expect(screen.getByText('Hello!')).toBeInTheDocument();
    });

    test('Renders nothing when there are no messages', () => {
      renderChat();
      openChat();

      expect(screen.queryByText('Hello!')).not.toBeInTheDocument();
    });

    test('Renders multiple messages correctly', async () => {
      const { getHandler } = captureSubscriptionHandler();

      renderChat();
      openChat();

      await waitFor(() => expect(getHandler()).not.toBeNull());

      act(() => {
        getHandler()!(msg1);
        getHandler()!(msg2);
      });

      expect(screen.getByText('Hello!')).toBeInTheDocument();
      expect(screen.getByText('Hi there!')).toBeInTheDocument();
    });

    test('Applies correct CSS class based on message ownership', async () => {
      const { getHandler } = captureSubscriptionHandler();

      renderChat();
      openChat();

      await waitFor(() => expect(getHandler()).not.toBeNull());

      act(() => {
        getHandler()!(msg1);
        getHandler()!(msg2);
      });

      const ownMsg = screen.getByText('Hello!').closest('.chat-message');
      const otherMsg = screen.getByText('Hi there!').closest('.chat-message');

      expect(ownMsg).toHaveClass('own-message');
      expect(otherMsg).not.toHaveClass('own-message');
    });

    test('Uses ColorToRgb for known players and fallback for unknown', async () => {
      const { getHandler } = captureSubscriptionHandler();

      vi.spyOn(colorParser, 'ColorToRgb').mockReturnValue('rgb(255, 0, 0)');

      renderChat();
      openChat();

      await waitFor(() => expect(getHandler()).not.toBeNull());

      const unknownMsg: Message = {
        id: 3, playerId: 99, playerName: 'Ghost', matchId: 1, content: 'Boo!'
      };

      act(() => {
        getHandler()!(msg1);
        getHandler()!(unknownMsg);
      });

      expect(screen.getByText('Alice')).toHaveStyle({ color: 'rgb(255, 0, 0)' });
      expect(screen.getByText('Ghost')).toHaveStyle({ color: '#000000' });
    });
  });

  // ── Scroll ─────────────────────────────────────────────────────────────────

  describe('Scroll', () => {
    test('Scrolls to bottom when message arrives', async () => {
      const { getHandler } = captureSubscriptionHandler();

      renderChat();
      openChat();

      const container = document.querySelector('.chat-messages')!;
      Object.defineProperty(container, 'scrollHeight', { value: 500 });
      Object.defineProperty(container, 'scrollTop', { value: 0, writable: true });

      await waitFor(() => expect(getHandler()).not.toBeNull());

      act(() => getHandler()!(msg1));

      expect(container.scrollTop).toBe(500);
    });

    test('Does not crash if ref is null', async () => {
      const { getHandler } = captureSubscriptionHandler();

      renderChat();
      openChat();

      await waitFor(() => expect(getHandler()).not.toBeNull());

      // Simula ref null eliminando el nodo
      document.querySelector('.chat-messages')?.remove();

      expect(() => {
        act(() => getHandler()!(msg1));
      }).not.toThrow();
    });
  });

  // ── Send message ───────────────────────────────────────────────────────────

  describe('handleSendMessage', () => {

    test('Sends message and clears input', async () => {
      renderChat();
      openChat();

      const input = screen.getByPlaceholderText('Write a message...');

      fireEvent.change(input, { target: { value: 'Test message' } });
      fireEvent.submit(input.closest('form')!);

      await waitFor(() => {
        expect(fetchersModule.post).toHaveBeenCalled();
        expect(input).toHaveValue('');
      });
    });

    test('Does not send empty message', () => {
      renderChat();
      openChat();

      fireEvent.change(screen.getByPlaceholderText('Write a message...'), {
        target: { value: '   ' },
      });

      fireEvent.submit(screen.getByRole('button', { name: /Send/i }).closest('form')!);

      expect(fetchersModule.post).not.toHaveBeenCalled();
    });

    test('Handles post failure gracefully', async () => {
      vi.mocked(fetchersModule.post).mockRejectedValueOnce(new Error('fail'));

      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      renderChat();
      openChat();

      fireEvent.change(screen.getByPlaceholderText('Write a message...'), {
        target: { value: 'Hello' },
      });

      fireEvent.submit(screen.getByRole('button', { name: /Send/i }).closest('form')!);

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalled();
      });

      consoleSpy.mockRestore();
    });
  });

  // ── WebSocket ──────────────────────────────────────────────────────────────

  describe('WebSocket lifecycle', () => {

    test('Activates and deactivates correctly', async () => {
      const { unmount } = renderChat();

      await waitFor(() => expect(mockClientInstance.activate).toHaveBeenCalled());

      unmount();

      await waitFor(() => expect(mockClientInstance.deactivate).toHaveBeenCalled());
    });

    test('Subscribes to match topic and re-subscribes on id change', () => {
      const spy = vi.spyOn(WebSocketManager.prototype, 'addSubscription');

      const { rerender } = renderChat();

      expect(spy).toHaveBeenCalledWith(`chat/${match.id}`, expect.any(Function));

      const newMatch = { ...match, id: 2 };

      rerender(<Chat match={newMatch} players={players} currentPlayer={currentPlayer} />);

      expect(spy).toHaveBeenCalledWith(`chat/${newMatch.id}`, expect.any(Function));
    });
  });
});