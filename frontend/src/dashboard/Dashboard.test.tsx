import { vi, describe, test, expect, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, within, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import React from 'react';
import Dashboard from './Dashboard';
import { State } from '../interfaces/State';
import tokenService from '../services/token.service';
import * as useFetchStateModule from '../util/useFetchState';
import * as deleteFromListModule from '../util/deleteFromList';
import { DashboardMatch } from '../interfaces/Match';
import type User from '../interfaces/User';
import { Authority } from '../interfaces/User';

// ─── Infrastructure mocks ─────────────────────────────────────────────────────

let capturedWsCallback: ((match: any) => void) | null = null;

vi.mock('../components/webSocketManager/webSocketManager.ts', () => ({
  default: vi.fn().mockImplementation(function () {
    return {
      initConnection: mockClientInstance.activate,
      deactivate: mockClientInstance.deactivate,
      addSubscription: vi.fn((destination: string, callback: (data: any) => void) => {
        capturedWsCallback = callback;
      }),
    };
  }),
}));

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

vi.mock('jwt-decode', () => ({
  default: vi.fn(() => ({ sub: 'testuser' })),
}));

vi.mock('../services/token.service', () => ({
  default: {
    getLocalAccessToken: vi.fn(() => 'mock-jwt'),
    getUser: vi.fn(() => ({ id: 1, roles: [Authority.PLAYER] } as User)),
  },
}));

vi.mock('../util/useFetchState');
vi.mock('../util/deleteFromList');

// SearchBar mock at top level — never inside a test
vi.mock('../components/SearchBar/SearchBar', () => ({
  default: ({ setFiltered, data }: { setFiltered: (d: DashboardMatch[]) => void; data: DashboardMatch[] }) => {
    return (
      <input
        placeholder="Search matches"
        data-testid="search-bar"
        onChange={(e) => {
          const value = e.target.value;
          if (value) {
            setFiltered(data.filter((m) => m.name.includes(value)));
          } else {
            setFiltered([]);
          }
        }}
      />
    );
  },
}));

const mockedNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockedNavigate };
});

// ─── Fixtures ─────────────────────────────────────────────────────────────────

const matchWaiting: DashboardMatch = {
  id: 1, name: 'Match Waiting', playersNumber: 2, state: State.WAITING, password: '',
};
const matchOnGoing: DashboardMatch = {
  id: 2, name: 'Match OnGoing', playersNumber: 4, state: State.ON_GOING, password: '',
};
const matchPrivate: DashboardMatch = {
  id: 3, name: 'Match Private', playersNumber: 2, state: State.WAITING, password: 'secret',
};
const matchFinished: DashboardMatch = {
  id: 4, name: 'Match Finished', playersNumber: 4, state: State.FINALIZED, password: '',
};

// ─── Helpers ──────────────────────────────────────────────────────────────────

const mockFetchState = (matches: DashboardMatch[]) => {
  vi.mocked(useFetchStateModule.default).mockReturnValue([matches, vi.fn()]);
};

const renderDashboard = () =>
  render(<MemoryRouter><Dashboard /></MemoryRouter>);

// ─── Tests ────────────────────────────────────────────────────────────────────

describe('Dashboard', () => {

  beforeEach(() => {
    vi.clearAllMocks();
    mockClientInstance.activate.mockReset();
    mockClientInstance.deactivate.mockReset();
    mockClientInstance.subscribe.mockReset();
    vi.mocked(tokenService.getLocalAccessToken).mockReturnValue('mock-jwt');
    vi.mocked(tokenService.getUser).mockReturnValue({ id: 1, roles: [Authority.PLAYER] } as User);
    // Always reset to empty so no previous matches bleed through
    mockFetchState([]);
  });

  // ── Base rendering ──────────────────────────────────────────────────────────

  describe('Base rendering', () => {
    test('Shows username extracted from JWT in the title', () => {
      renderDashboard();
      expect(screen.getByText(/Game Listing for testuser/i)).toBeInTheDocument();
    });

    test('Shows create match button', () => {
      renderDashboard();
      expect(screen.getByRole('link', { name: /Create Game/i })).toBeInTheDocument();
    });

    test('Shows table headers', () => {
      renderDashboard();
      ['Game', 'Players', 'State', 'Private', 'Join', 'Spectate'].forEach((header) => {
        expect(screen.getByText(header)).toBeInTheDocument();
      });
    });

    test('Table empty with no matches', () => {
      // mockFetchState([]) already called in beforeEach
      renderDashboard();
      expect(screen.queryByRole('button', { name: /Join game/i })).not.toBeInTheDocument();
    });
  });

  // ── Matrix match state × visible buttons ────────────────────────────────────

  describe('Matrix match state × visible buttons', () => {
    test.each([
      ['WAITING',  matchWaiting,  true,  true ],
      ['ON_GOING', matchOnGoing,  false, true ],
      ['FINISHED', matchFinished, false, false],
    ])('state %s: Join=%s Spectate=%s', (_, match, joinVisible, spectateVisible) => {
      mockFetchState([match]);
      renderDashboard();

      const row = screen.getByText(match.name).closest('tr')!;

      if (joinVisible) {
        expect(within(row).getByRole('button', { name: /Join game/i })).toBeInTheDocument();
      } else {
        expect(within(row).queryByRole('button', { name: /Join game/i })).not.toBeInTheDocument();
      }

      if (spectateVisible) {
        expect(within(row).getByRole('button', { name: /Spectate game/i })).toBeInTheDocument();
      } else {
        expect(within(row).queryByRole('button', { name: /Spectate game/i })).not.toBeInTheDocument();
      }
    });
  });

  // ── Private match indicator ─────────────────────────────────────────────────

  describe('Private Match indicator', () => {
    test('Match with password shows lock icon', () => {
      mockFetchState([matchPrivate]);
      renderDashboard();
      const row = screen.getByText(matchPrivate.name).closest('tr')!;
      expect(within(row).getByLabelText('private game')).toBeInTheDocument();
    });

    test('No password, no lock', () => {
      mockFetchState([matchWaiting]);
      renderDashboard();
      const row = screen.getByText(matchWaiting.name).closest('tr')!;
      expect(within(row).queryByLabelText('private game')).not.toBeInTheDocument();
    });
  });

  // ── join() ──────────────────────────────────────────────────────────────────

  describe('join()', () => {
    test('Public match: directly to Lobby', () => {
      mockFetchState([matchWaiting]);
      renderDashboard();
      fireEvent.click(screen.getByRole('button', { name: /Join game/i }));
      expect(mockedNavigate).toHaveBeenCalledWith(`/matches/${matchWaiting.id}/waiting`);
    });

    test('Private match: correct password navigates to Lobby', () => {
      mockFetchState([matchPrivate]);
      vi.spyOn(window, 'prompt').mockReturnValue('secret');
      renderDashboard();
      fireEvent.click(screen.getByRole('button', { name: /Join game/i }));
      expect(mockedNavigate).toHaveBeenCalledWith(`/matches/${matchPrivate.id}/waiting`);
    });

    test('Private match: wrong password shows alert and does NOT navigate', () => {
      mockFetchState([matchPrivate]);
      vi.spyOn(window, 'prompt').mockReturnValue('wrong');
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
      renderDashboard();
      fireEvent.click(screen.getByRole('button', { name: /Join game/i }));
      expect(alertSpy).toHaveBeenCalledWith('Incorrect password');
      expect(mockedNavigate).not.toHaveBeenCalled();
    });
  });

  // ── espectate() ─────────────────────────────────────────────────────────────

  describe('espectate()', () => {
    test('Browses to /game with id of user in spectatorIds', () => {
      mockFetchState([matchWaiting]);
      vi.mocked(tokenService.getUser).mockReturnValue({ id: 42, roles: [Authority.PLAYER] } as User);
      renderDashboard();
      fireEvent.click(screen.getByRole('button', { name: /Spectate game/i }));
      expect(mockedNavigate).toHaveBeenCalledWith(
        `/matches/${matchWaiting.id}/game`,
        { state: { spectatorIds: [42] } }
      );
    });
  });

  // ── Role-based access control ───────────────────────────────────────────────

  describe('Security🛡️: Role Control Access', () => {
    test('ADMIN: shows Delete button in each row', () => {
      vi.mocked(tokenService.getUser).mockReturnValue({ id: 1, roles: [Authority.ADMIN] } as User);
      mockFetchState([matchWaiting, matchOnGoing]);
      renderDashboard();
      expect(screen.getAllByRole('button', { name: /Delete/i })).toHaveLength(2);
    });

    test('PLAYER: NO Delete buttons at all', () => {
      mockFetchState([matchWaiting, matchOnGoing]);
      renderDashboard();
      expect(screen.queryByRole('button', { name: /Delete/i })).not.toBeInTheDocument();
    });

    test('ADMIN: click on Delete calls deleteFromList with correct URL', () => {
      vi.mocked(tokenService.getUser).mockReturnValue({ id: 1, roles: [Authority.ADMIN] } as User);
      mockFetchState([matchWaiting]);
      renderDashboard();
      fireEvent.click(screen.getByRole('button', { name: /Delete/i }));
      expect(vi.mocked(deleteFromListModule.default)).toHaveBeenCalledWith(
        `/api/v1/matches`,
        matchWaiting.id,
        expect.any(Array),
        expect.any(Array)
      );
    });
  });

  // ── SearchBar filtering ─────────────────────────────────────────────────────

  describe('SearchBar filtering', () => {
    test('When filtered is empty renders all matches', () => {
      mockFetchState([matchWaiting, matchOnGoing]);
      renderDashboard();
      expect(screen.getByText(matchWaiting.name)).toBeInTheDocument();
      expect(screen.getByText(matchOnGoing.name)).toBeInTheDocument();
    });

    test('When filtered has data renders only filtered matches', () => {
      mockFetchState([matchWaiting, matchOnGoing]);
      renderDashboard();

      // Type in the search bar to trigger setFiltered
      fireEvent.change(screen.getByTestId('search-bar'), {
        target: { value: 'Waiting' },
      });

      expect(screen.getByText(matchWaiting.name)).toBeInTheDocument();
      expect(screen.queryByText(matchOnGoing.name)).not.toBeInTheDocument();
    });
  });

  // ── No JWT ──────────────────────────────────────────────────────────────────

  test('No JWT: empty username in title', () => {
    vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("");
    renderDashboard();
    expect(screen.getByText(/Game Listing for\s*$/i)).toBeInTheDocument();
  });

  // ── WebSocket ───────────────────────────────────────────────────────────────

  describe('WebSocket', () => {
    test('WebSocket connection initializes upon component mount', async () => {
      renderDashboard();
      await waitFor(() => {
        expect(mockClientInstance.activate).toHaveBeenCalled();
      });
    });

    test('WebSocket disconnects upon component unmount', async () => {
      const { unmount } = renderDashboard();
      unmount();
      await waitFor(() => {
        expect(mockClientInstance.deactivate).toHaveBeenCalled();
      });
    });

    test('WebSocket callback replaces match with the same id', async () => {
      vi.mocked(useFetchStateModule.default).mockImplementation((() => {
        const [matches, setMatches] = React.useState<DashboardMatch[]>([matchWaiting]);
        return [matches, setMatches];
      }) as any);

      renderDashboard();

      await waitFor(() => expect(capturedWsCallback).not.toBeNull());

      act(() => {
        capturedWsCallback!({ ...matchWaiting, state: State.ON_GOING });
      });

      await waitFor(() => {
        expect(screen.queryByRole('button', { name: /Join game/i })).not.toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Spectate game/i })).toBeInTheDocument();
      });
    });
  });
});