import { describe, it, expect, vi, beforeEach } from 'vitest';
import { navigateToUnauthorized } from './DoNotHackMyAppHelper';

describe('DoNotHackMyAppHelper', () => {
    const navigate = vi.fn();

    beforeEach(() => {
        navigate.mockClear();
    });

    it('uses checkAuth function if provided', () => {
        const checkAuth = vi.fn().mockReturnValue(true);
        navigateToUnauthorized(checkAuth, { id: 1, username: 'user' }, 'ADMIN', navigate);
        expect(checkAuth).toHaveBeenCalledWith('ADMIN');
        expect(navigate).not.toHaveBeenCalled();
    });

    it('redirects if checkAuth function returns false', () => {
        const checkAuth = vi.fn().mockReturnValue(false);
        navigateToUnauthorized(checkAuth, { id: 1, username: 'user' }, 'ADMIN', navigate);
        expect(navigate).toHaveBeenCalledWith('/do-not-hack-my-app');
    });

    it('uses checkAuthDefault if checkAuth is undefined', () => {
        // Alice has roles ['PLAYER']
        const alice = { id: 1, username: 'Alice', roles: ['PLAYER'] };
        
        // Attempting to access ADMIN area
        navigateToUnauthorized(undefined, alice as any, 'ADMIN', navigate);
        expect(navigate).toHaveBeenCalledWith('/do-not-hack-my-app');
    });

    it('allows access if checkAuthDefault returns true', () => {
        const admin = { id: 1, username: 'Admin', roles: ['ADMIN'] };
        navigateToUnauthorized(undefined, admin as any, 'ADMIN', navigate);
        expect(navigate).not.toHaveBeenCalled();
    });

    it('allows access if checkAuth is true', () => {
        navigateToUnauthorized(true, { id: 1 } as any, 'ADMIN', navigate);
        expect(navigate).not.toHaveBeenCalled();
    });

    it('redirects to custom URL', () => {
        const checkAuth = () => false;
        navigateToUnauthorized(checkAuth, { id: 1 } as any, 'ADMIN', navigate, '/custom-redirect');
        expect(navigate).toHaveBeenCalledWith('/custom-redirect');
    });
});
