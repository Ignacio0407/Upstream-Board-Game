import { render, screen, waitFor, act } from "../../test-utils";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Login from "./Login";
import tokenService from "../../services/token.service.ts";

// --- Mocks ---
vi.mock("../../services/token.service.ts");

describe("Login Component", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        // Mock window.location.href
        Object.defineProperty(window, 'location', {
            configurable: true,
            value: { href: 'http://localhost/login' },
        });
    });

    it("performs successful login and redirects to dashboard", async () => {
        const user = userEvent.setup();
        render(<Login />);
        
        const usernameInput = screen.getByLabelText(/Username/i);
        const passwordInput = screen.getByLabelText(/Password/i);

        await user.type(usernameInput, "testuser");
        await user.type(passwordInput, "password123");
        
        await act(async () => {
            await user.click(screen.getByRole("button", { name: /Login/i }));
        });

        await waitFor(() => {
            expect(tokenService.setUser).toHaveBeenCalledWith(expect.objectContaining({
                token: "mock-jwt",
                username: "testuser"
            }));
            expect(tokenService.updateLocalAccessToken).toHaveBeenCalledWith("mock-jwt");
            expect(window.location.href).toBe("/dashboard");
        });
    });

    it("shows error message on failed login", async () => {
        const user = userEvent.setup();
        render(<Login />);
        
        const usernameInput = screen.getByLabelText(/Username/i);
        const passwordInput = screen.getByLabelText(/Password/i);

        await user.type(usernameInput, "wronguser");
        await user.type(passwordInput, "wrongpass");
        
        await act(async () => {
            await user.click(screen.getByRole("button", { name: /Login/i }));
        });

        await waitFor(() => {
            expect(screen.getByText(/Invalid login attempt/i)).toBeInTheDocument();
            expect(window.location.href).not.toBe("/dashboard");
        });
    });
});
