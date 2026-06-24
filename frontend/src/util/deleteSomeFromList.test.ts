import { describe, it, expect, vi, beforeEach } from 'vitest';
import deleteSomeFromList from './deleteSomeFromList';
import tokenService from '../services/token.service';
import * as fetchers from './fetchers';
import { navigateToUnauthorized } from '../components/DoNotHackMyApp/DoNotHackMyAppHelper';
import { Authority } from '../interfaces/User';

vi.mock('../services/token.service');
vi.mock('./fetchers');
vi.mock('../components/DoNotHackMyApp/DoNotHackMyAppHelper');

describe('deleteSomeFromList', () => {
    const setState = vi.fn();
    const navigate = vi.fn();
    const items = [{ id: 1, name: 'Item 1' }, { id: 2, name: 'Item 2' }];
    const state = [...items];

    beforeEach(() => {
        vi.clearAllMocks();
        globalThis.confirm = vi.fn();
        globalThis.alert = vi.fn();
        vi.mocked(tokenService.getUser).mockReturnValue({ id: 1 } as any);
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue('jwt');
    });

    it('calls navigateToUnauthorized if security is active', () => {
        deleteSomeFromList('/url', items, Authority.ADMIN, undefined, false, state, setState, navigate);
        expect(navigateToUnauthorized).toHaveBeenCalled();
    });

    it('does nothing if user cancels confirmation', () => {
        vi.mocked(globalThis.confirm).mockReturnValue(false);
        deleteSomeFromList('/url', items, Authority.ADMIN, undefined, true, state, setState, navigate);
        expect(fetchers.deleteEntity).not.toHaveBeenCalled();
    });

    it('calls deleteEntity for each item and updates state', async () => {
        vi.mocked(globalThis.confirm).mockReturnValue(true);
        vi.mocked(fetchers.deleteEntity).mockResolvedValue({ status: 200 } as any);

        deleteSomeFromList('/url', items, Authority.ADMIN, undefined, true, state, setState, navigate);

        await vi.waitFor(() => {
            expect(fetchers.deleteEntity).toHaveBeenCalledTimes(2);
            expect(setState).toHaveBeenCalled();
        });
    });

    it('alerts on failure', async () => {
        vi.mocked(globalThis.confirm).mockReturnValue(true);
        vi.mocked(fetchers.deleteEntity).mockRejectedValue(new Error('Fail'));

        deleteSomeFromList('/url', items, Authority.ADMIN, undefined, true, state, setState, navigate);

        await vi.waitFor(() => {
            expect(globalThis.alert).toHaveBeenCalledWith('Error deleting selected items');
        });
    });
});
