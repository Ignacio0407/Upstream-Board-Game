import { describe, it, expect, vi, beforeEach } from 'vitest';
import endGame from './endGame';
import * as fetchers from './fetchers';
import { State } from '../interfaces/State';

vi.mock('./fetchers');

describe('endGame', () => {
    const navigate = vi.fn();
    const setMatch = vi.fn();
    const user = { id: 1 };
    const jwt = 'jwt';
    const match = { id: 1, playersNumber: 2, matchCreatorId: 2, state: State.PLAYING };
    const players = [{ id: 10, userId: 1 }];
    const spectatorIds: number[] = [];

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('navigates to dashboard if user is a spectator', async () => {
        await endGame({ match, players, user, jwt, spectatorIds: [1], navigate, setMatch } as any);
        expect(navigate).toHaveBeenCalledWith('/dashboard');
    });

    it('decrements playersNumber and sets state to WAITING if not last player', async () => {
        vi.mocked(fetchers.deleteEntity).mockResolvedValue({ status: 200 } as any);
        vi.mocked(fetchers.put).mockResolvedValue({ status: 200 } as any);

        await endGame({ match, players, user, jwt, spectatorIds, navigate, setMatch } as any);

        expect(match.playersNumber).toBe(1);
        expect(match.state).toBe(State.WAITING);
        expect(fetchers.deleteEntity).toHaveBeenCalled();
        expect(fetchers.put).toHaveBeenCalled();
        expect(navigate).toHaveBeenCalledWith('/dashboard');
    });

    it('sets state to FINALIZED if user is creator', async () => {
        const creatorMatch = { ...match, matchCreatorId: 1 };
        vi.mocked(fetchers.deleteEntity).mockResolvedValue({ status: 200 } as any);
        vi.mocked(fetchers.put).mockResolvedValue({ status: 200 } as any);

        await endGame({ match: creatorMatch, players, user, jwt, spectatorIds, navigate, setMatch } as any);

        expect(creatorMatch.playersNumber).toBe(0);
        expect(creatorMatch.state).toBe(State.FINALIZED);
    });

    it('handles player not found', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
        await endGame({ match, players: [], user, jwt, spectatorIds, navigate, setMatch } as any);
        expect(consoleSpy).toHaveBeenCalledWith('Player ID not found.');
        consoleSpy.mockRestore();
    });

    it('logs error when deleteEntity fails', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
        vi.mocked(fetchers.deleteEntity).mockRejectedValue(new Error('Delete error'));

        await endGame({ match, players, user, jwt, spectatorIds, navigate, setMatch } as any);

        expect(consoleSpy).toHaveBeenCalledWith('Error exiting from match:', expect.any(Error));
        consoleSpy.mockRestore();
    });
});
