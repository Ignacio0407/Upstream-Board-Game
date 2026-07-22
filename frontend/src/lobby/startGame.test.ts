import { describe, it, expect, vi, beforeEach } from 'vitest';
import startGame from './startGame';
import * as fetchers from '../util/fetchers.ts';

vi.mock('../util/fetchers.ts');

describe('startGame', () => {
    const navigate = vi.fn();
    const setLoading = vi.fn();
    const jwt = 'jwt';
    const matchId = 1;

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('sets loading, calls patch and post, then navigates', async () => {
        vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
        vi.mocked(fetchers.post).mockResolvedValue({ ok: true } as any);

        await startGame({ matchId, jwt, setLoading, navigate } as any);

        expect(setLoading).toHaveBeenCalledWith(true);
        expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matches/1/startGame', jwt);
        expect(fetchers.post).toHaveBeenCalledWith('/api/v1/matchTiles/createMatchTiles/1', jwt);
    });
});
