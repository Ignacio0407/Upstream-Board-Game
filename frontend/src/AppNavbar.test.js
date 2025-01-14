import AppNavbar from "./AppNavbar";
import { render, screen, fireEvent } from "./test-utils";
import tokenService from './services/token.service';

jest.mock('./services/token.service');
jest.mock("jwt-decode", () => jest.fn().mockReturnValue({ authorities: ["PLAYER"], sub: "user123" }));

describe('AppNavbar', () => {

    // Test para verificar que se renderizan correctamente los enlaces públicos cuando no hay JWT
    test('renders public links when not authenticated', () => {
        tokenService.getLocalAccessToken.mockReturnValue(null); // No hay JWT

        render(<AppNavbar />);

        // Verificar que los enlaces de "Pricing Plans", "Register", y "Login" se renderizan correctamente
        const linkPlansElement = screen.getByRole('link', { name: 'Pricing Plans' });
        expect(linkPlansElement).toBeInTheDocument();

        const linkRegisterElement = screen.getByRole('link', { name: 'Register' });
        expect(linkRegisterElement).toBeInTheDocument();

        const linkLoginElement = screen.getByRole('link', { name: 'Login' });
        expect(linkLoginElement).toBeInTheDocument();
    });

    // Test para verificar que se renderizan correctamente los enlaces para un jugador autenticado
    test('renders player links when authenticated', () => {
        tokenService.getLocalAccessToken.mockReturnValue('fake-jwt'); // Hay un JWT simulado

        render(<AppNavbar />);

        // Verificar que los enlaces del jugador (como "Achievements" y "Developers") se renderizan correctamente
        const linkAchievementsElement = screen.getByRole('link', { name: 'Achievements' });
        expect(linkAchievementsElement).toBeInTheDocument();

        const linkDevelopersElement = screen.getByRole('link', { name: 'Developers' });
        expect(linkDevelopersElement).toBeInTheDocument();
    });

    // Test para verificar que se renderizan los enlaces de un admin cuando tiene el rol adecuado
    test('renders admin links when admin role is present', () => {
        tokenService.getLocalAccessToken.mockReturnValue('fake-jwt-admin');
        jest.mock("jwt-decode", () => jest.fn().mockReturnValue({ authorities: ["ADMIN"], sub: "admin123" }));

        render(<AppNavbar />);

        // Verificar que los enlaces del admin (como "Users", "Developers", "Achievements" y "Docs") se renderizan
        const linkUsersElement = screen.getByRole('link', { name: 'Users' });
        expect(linkUsersElement).toBeInTheDocument();

        const linkDevelopersElement = screen.getByRole('link', { name: 'Developers' });
        expect(linkDevelopersElement).toBeInTheDocument();

        const linkAchievementsElement = screen.getByRole('link', { name: 'Achievements' });
        expect(linkAchievementsElement).toBeInTheDocument();

        const linkDocsElement = screen.getByRole('link', { name: 'Docs' });
        expect(linkDocsElement).toBeInTheDocument();
    });

    // Test para verificar que el enlace de "Logout" aparece cuando el usuario está autenticado
    test('renders logout link when authenticated', () => {
        tokenService.getLocalAccessToken.mockReturnValue('fake-jwt');

        render(<AppNavbar />);

        const linkLogoutElement = screen.getByRole('link', { name: 'Logout' });
        expect(linkLogoutElement).toBeInTheDocument();
    });

    // Test para verificar que el método unlockAchievement es llamado correctamente
    test('calls unlockAchievement on click', async () => {
        tokenService.getLocalAccessToken.mockReturnValue('fake-jwt');
        jest.mock("jwt-decode", () => jest.fn().mockReturnValue({ authorities: ["PLAYER"], sub: "user123" }));

        const mockUnlockAchievement = jest.fn();
        render(<AppNavbar />);

        const unlockLink = screen.getByRole('link', { name: 'Rules' });
        fireEvent.click(unlockLink);

        // Asegurarse de que la función de desbloqueo haya sido llamada
        expect(mockUnlockAchievement).toHaveBeenCalled();
    });

});