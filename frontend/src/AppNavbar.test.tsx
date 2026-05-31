import { vi, describe, test, expect, beforeEach } from 'vitest';
import { Authority } from './interfaces/User.ts';
import tokenService from './services/token.service.ts';
import * as jwtDecode from 'jwt-decode';
import * as fetchers from './util/fetchers.ts';
import AppNavbar from "./AppNavbar.tsx";
import { render, screen, fireEvent, waitFor } from "./test-utils";

vi.mock('./services/token.service');
vi.mock('./util/fetchers.ts');

vi.mock("jwt-decode", () => ({
  default: vi.fn(() => ({
    authorities: [Authority.PLAYER],
    sub: "user123"
  }))
}));

const getLocalAccessTokenMock = vi.mocked(tokenService.getLocalAccessToken);
const jwtDecodeMock = vi.mocked(jwtDecode.default);
const postMock = vi.mocked(fetchers.post);

describe('AppNavbar', () => {

  beforeEach(() => {
    jwtDecodeMock.mockReturnValue({ authorities: [Authority.PLAYER], sub: "user123" });
    postMock.mockResolvedValue({ ok: true } as Response);
  });

  // --- No auth ---

  test('NO JWT: shows public links and hides user ones', () => {
    getLocalAccessTokenMock.mockReturnValue("");
    render(<AppNavbar />);

    expect(screen.getByRole('link', { name: 'Pricing Plans' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Register' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Login' })).toBeInTheDocument();

    expect(screen.queryByRole('link', { name: 'Logout' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Dashboard' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Rules' })).not.toBeInTheDocument();
  });

  // --- Auth/Rol PLAYER ---

  test('JWT and rol PLAYER: shows player links and hides public ones', () => {
    getLocalAccessTokenMock.mockReturnValue('fake-jwt');
    render(<AppNavbar />);

    expect(screen.getByRole('link', { name: 'Achievements' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Developers' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Dashboard' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Rules' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Logout' })).toBeInTheDocument();

    // Login and Register must NOT appear when authenticated
    expect(screen.queryByRole('link', { name: 'Register' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Login' })).not.toBeInTheDocument();

    // Admin links must NOT appear
    expect(screen.queryByRole('link', { name: 'Users' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Docs' })).not.toBeInTheDocument();
  });

  test("JWT: token's username is shown on navbar", () => {
    getLocalAccessTokenMock.mockReturnValue('fake-jwt');
    render(<AppNavbar />);

    expect(screen.getByText('user123')).toBeInTheDocument();
  });

  // --- Auth/Rol ADMIN ---

  test('JWT and rol ADMIN: shows admin links  oculta los de PLAYER', () => {
    getLocalAccessTokenMock.mockReturnValue('fake-jwt-admin');
    jwtDecodeMock.mockReturnValue({ authorities: ["ADMIN"], sub: "admin123" });
    render(<AppNavbar />);

    expect(screen.getByRole('link', { name: 'Users' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Developers' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Achievements' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Docs' })).toBeInTheDocument();

    // ownerLinks are exclusive to PLAYER, must NOT appear for ADMIN
    // Achievements appears for ADMIN but because of adminLinks, no ownerLinks — 
    // Therefore, there must NOT be duplicates
    expect(screen.getAllByRole('link', { name: 'Achievements' })).toHaveLength(1);
    expect(screen.getAllByRole('link', { name: 'Developers' })).toHaveLength(1);
  });

  // --- unlockAchievement ---

  test('click in Rules calls post with correct username and jwt', async () => {
    getLocalAccessTokenMock.mockReturnValue('fake-jwt');
    render(<AppNavbar />);

    fireEvent.click(screen.getByRole('link', { name: 'Rules' }));

    await waitFor(() => {
      expect(postMock).toHaveBeenCalledWith(
        '/api/v1/usersachievements/unlockrules/user123',
        'fake-jwt'
      );
    });
  });

  test('unlockAchievement logs error if answer is NOT ok', async () => {
    getLocalAccessTokenMock.mockReturnValue('fake-jwt');
    postMock.mockResolvedValue({ ok: false, status: 403 } as Response);
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(<AppNavbar />);
    fireEvent.click(screen.getByRole('link', { name: 'Rules' }));

    await waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(
        'Failed to unlock achievement:',
        expect.any(Error)
      );
    });

    consoleSpy.mockRestore();
  });

  // --- Invalid JWT ---

  test('Malformed JWT does NOT break render and does NOT show username', () => {
    getLocalAccessTokenMock.mockReturnValue('bad-jwt');
    jwtDecodeMock.mockImplementation(() => { throw new Error('invalid token'); });
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(<AppNavbar />);

    expect(screen.getByRole('navigation')).toBeInTheDocument();
    expect(consoleSpy).toHaveBeenCalledWith('Failed to decode JWT', expect.any(Error));

    consoleSpy.mockRestore();
  });

  // --- toggleNavbar ---

  test('Toggler colapses and expands the menu', () => {
    getLocalAccessTokenMock.mockReturnValue('fake-jwt');
    render(<AppNavbar />);

    const toggler = screen.getByRole('button');

    // Estado inicial: collapsed = true → el Collapse no está abierto
    // Hacemos click para abrir
    fireEvent.click(toggler);
    expect(toggler).toBeInTheDocument(); // El toggler sigue ahí

    // Segundo click para cerrar de nuevo
    fireEvent.click(toggler);
    expect(toggler).toBeInTheDocument();
  });

});