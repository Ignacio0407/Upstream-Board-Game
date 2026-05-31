import { describe, it, expect, vi, beforeEach } from 'vitest';
import { emptyUser } from '../interfaces/User';

describe('TokenService', () => {
    let tokenService: any;

    beforeEach(async () => {
        const mod = await vi.importActual<any>('./token.service');
        const TokenService = mod.TokenService;
        tokenService = new TokenService();
        localStorage.clear();
    });

    it('gets local access token', () => {
        localStorage.setItem('jwt', JSON.stringify('mock-token'));
        expect(tokenService.getLocalAccessToken()).toBe('mock-token');
    });

    it('returns empty string if no access token', () => {
        expect(tokenService.getLocalAccessToken()).toBe('');
    });

    it('updates local access token', () => {
        tokenService.updateLocalAccessToken('new-token');
        expect(localStorage.getItem('jwt')).toBe(JSON.stringify('new-token'));
    });

    it('gets local refresh token', () => {
        localStorage.setItem('user', JSON.stringify({ refreshToken: 'refresh' }));
        expect(tokenService.getLocalRefreshToken()).toBe('refresh');
    });

    it('gets user', () => {
        const user = { id: 1, username: 'test' };
        localStorage.setItem('user', JSON.stringify(user));
        expect(tokenService.getUser()).toEqual(user);
    });

    it('returns empty user if no user in localStorage', () => {
        expect(tokenService.getUser()).toEqual(emptyUser);
    });

    it('sets user', () => {
        const user = { id: 1, username: 'test' };
        tokenService.setUser(user as any);
        expect(localStorage.getItem('user')).toBe(JSON.stringify(user));
    });

    it('removes user and token', () => {
        localStorage.setItem('user', 'u');
        localStorage.setItem('jwt', 'j');
        tokenService.removeUser();
        expect(localStorage.getItem('user')).toBeNull();
        expect(localStorage.getItem('jwt')).toBeNull();
    });
});
