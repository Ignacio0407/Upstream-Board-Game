import { describe, it, expect, vi, beforeEach } from 'vitest';
import deleteFromList from './deleteFromList';
import tokenService from '../services/token.service';
import * as fetchers from './fetchers';
import getDeleteAlertsOrModal from './getDeleteAlertsOrModal';

vi.mock('../services/token.service');
vi.mock('./fetchers');
vi.mock('./getDeleteAlertsOrModal');

describe('deleteFromList', () => {
    const setState = vi.fn();
    const setAlerts = vi.fn();
    const setMessage = vi.fn();
    const setVisible = vi.fn();
    const state = [{ id: 1 }, { id: 2 }];
    const alerts: any[] = [];

    beforeEach(() => {
        vi.clearAllMocks();
        globalThis.confirm = vi.fn();
        globalThis.alert = vi.fn();
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue('jwt');
    });

    it('does nothing if user cancels confirmation', () => {
        vi.mocked(globalThis.confirm).mockReturnValue(false);
        deleteFromList('/url', 1, [state, setState], [alerts, setAlerts]);
        expect(fetchers.deleteEntity).not.toHaveBeenCalled();
    });

    it('calls deleteEntity and updates state on success', async () => {
        vi.mocked(globalThis.confirm).mockReturnValue(true);
        vi.mocked(fetchers.deleteEntity).mockResolvedValue({ status: 200, text: async () => '' } as any);

        deleteFromList('/url', 1, [state, setState], [alerts, setAlerts]);

        await vi.waitFor(() => {
            expect(fetchers.deleteEntity).toHaveBeenCalledWith('/url', 'jwt', 1);
            expect(setState).toHaveBeenCalled();
            const filteredState = setState.mock.calls[0][0];
            expect(filteredState).toEqual([{ id: 2 }]);
        });
    });

    it('handles filtered options', async () => {
        vi.mocked(globalThis.confirm).mockReturnValue(true);
        vi.mocked(fetchers.deleteEntity).mockResolvedValue({ status: 200, text: async () => '' } as any);
        const setFiltered = vi.fn();
        const filtered = [{ id: 1 }];

        deleteFromList('/url', 1, [state, setState], [alerts, setAlerts], undefined, undefined, { filtered, setFiltered });

        await vi.waitFor(() => {
            expect(setFiltered).toHaveBeenCalled();
        });
    });

    it('calls getDeleteAlertsOrModal if response has text', async () => {
        vi.mocked(globalThis.confirm).mockReturnValue(true);
        vi.mocked(fetchers.deleteEntity).mockResolvedValue({ status: 200, text: async () => '{"msg": "deleted"}' } as any);

        deleteFromList('/url', 1, [state, setState], [alerts, setAlerts], setMessage, setVisible);

        await vi.waitFor(() => {
            expect(getDeleteAlertsOrModal).toHaveBeenCalled();
        });
    });

    it('alerts error on failure', async () => {
        vi.mocked(globalThis.confirm).mockReturnValue(true);
        vi.mocked(fetchers.deleteEntity).mockRejectedValue(new Error('Fail'));

        deleteFromList('/url', 1, [state, setState], [alerts, setAlerts]);

        await vi.waitFor(() => {
            expect(globalThis.alert).toHaveBeenCalledWith('Error deleting entity');
        });
    });
});
