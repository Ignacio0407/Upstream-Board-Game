import { render, screen, waitFor, act } from "../../test-utils";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Register from "./Register";
import tokenService from "../../services/token.service.ts";

// --- Mocks ---
vi.mock("../../services/token.service.ts");

describe("Register Component", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        // Mock window.location.href
        Object.defineProperty(window, 'location', {
            configurable: true,
            value: { href: 'http://localhost/register' },
        });
    });

    it("performs successful registration and login", async () => {
        const user = userEvent.setup();
        render(<Register />);
        
        const usernameInput = screen.getByLabelText(/Username/i);
        const passwordInput = screen.getByLabelText(/Password/i);
        const firstNameInput = screen.getByLabelText(/First Name/i);
        const lastNameInput = screen.getByLabelText(/Last Name/i);

        await user.type(usernameInput, "newuser");
        await user.type(passwordInput, "password123");
        await user.type(firstNameInput, "First");
        await user.type(lastNameInput, "Last");
        
        // Select role
        await user.click(screen.getByRole("button", { name: /Player/i }));

        await act(async () => {
            await user.click(screen.getByRole("button", { name: /Register/i }));
        });

        await waitFor(() => {
            expect(tokenService.setUser).toHaveBeenCalledWith(expect.objectContaining({
                token: "mock-jwt",
                username: "newuser"
            }));
            expect(window.location.href).toBe("/dashboard");
        });
    });

    it("shows error if registration fails", async () => {
        const user = userEvent.setup();
        render(<Register />);
        
        const usernameInput = screen.getByLabelText(/Username/i);
        const passwordInput = screen.getByLabelText(/Password/i);
        const firstNameInput = screen.getByLabelText(/First Name/i);
        const lastNameInput = screen.getByLabelText(/Last Name/i);

        await user.type(usernameInput, "existinguser");
        await user.type(passwordInput, "password123");
        await user.type(firstNameInput, "First");
        await user.type(lastNameInput, "Last");
        
        await act(async () => {
            await user.click(screen.getByRole("button", { name: /Register/i }));
        });

        await waitFor(() => {
            expect(screen.getByText(/User already exists/i)).toBeInTheDocument();
        });
    });

    it("allows selecting Admin role and handles login failure after registration", async () => {
        const user = userEvent.setup();
        render(<Register />);
        
        const usernameInput = screen.getByLabelText(/Username/i);
        const passwordInput = screen.getByLabelText(/Password/i);
        const firstNameInput = screen.getByLabelText(/First Name/i);
        const lastNameInput = screen.getByLabelText(/Last Name/i);

        await user.type(usernameInput, "adminuser");
        await user.type(passwordInput, "password123");
        await user.type(firstNameInput, "Admin");
        await user.type(lastNameInput, "User");
        
        // Select Admin role
        await user.click(screen.getByRole("button", { name: /^Admin$/i }));

        // Mock signup success, signin failure
        global.fetch = vi.fn().mockImplementation(async (url) => {
            if (String(url).includes("/signup")) {
                return { ok: true, json: async () => ({}) };
            }
            return { ok: false, status: 401, json: async () => ({ message: "Login failed" }) };
        });

        await act(async () => {
            await user.click(screen.getByRole("button", { name: /Register/i }));
        });

        await waitFor(() => {
            expect(screen.getByText(/Login failed after registration/i)).toBeInTheDocument();
        });
    });
});
