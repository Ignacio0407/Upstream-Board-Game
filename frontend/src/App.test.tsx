import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import App from "./App";
import tokenService from "./services/token.service.ts";
import jwt_decode from "jwt-decode";
import { MemoryRouter } from "react-router-dom";

// --- Mocks ---

// Overriding global mocks from setupTests.ts
vi.mock("jwt-decode", () => ({
    default: vi.fn(),
}));

vi.mock("./services/token.service.ts");

let shouldThrowError = false;

// Mocking sub-components to focus on App routing logic
vi.mock("./home/home.tsx", () => ({ default: () => {
    if (shouldThrowError) throw new Error("Test Error");
    return <div data-testid="home-page">Home</div>;
} }));
vi.mock("./auth/login/Login.tsx", () => ({ default: () => <div data-testid="login-page">Login</div> }));
vi.mock("./dashboard/Dashboard.tsx", () => ({ default: () => <div data-testid="dashboard-page">Dashboard</div> }));
vi.mock("./admin/users/UserListAdmin.tsx", () => ({ default: () => <div data-testid="admin-users-page">Admin Users</div> }));
vi.mock("./components/DoNotHackMyApp/DoNotHackMyApp.tsx", () => ({ default: () => <div>
    <p>Don' t Hack My App</p>
    <p>HACKER DETECTED</p>
</div> }));

describe("App Component Routing", () => {
    const originalFetch = global.fetch;

    beforeEach(() => {
        vi.clearAllMocks();
        shouldThrowError = false;
        // Mock fetch for PrivateRoute validation
        global.fetch = vi.fn().mockImplementation(() => 
            Promise.resolve({
                ok: true,
                json: () => Promise.resolve(true),
            })
        );
    });

    afterEach(() => {
        global.fetch = originalFetch;
    });

    it("renders Home page by default when not authenticated", () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("");
        
        render(
            <MemoryRouter initialEntries={["/"]}>
                <App />
            </MemoryRouter>
        );

        expect(screen.getByTestId("home-page")).toBeInTheDocument();
    });

    it("renders Login page when navigating to /login", () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("");
        
        render(
            <MemoryRouter initialEntries={["/login"]}>
                <App />
            </MemoryRouter>
        );

        expect(screen.getByTestId("login-page")).toBeInTheDocument();
    });

    it("renders Dashboard when authenticated as PLAYER and navigating to /dashboard", async () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("fake-jwt");
        vi.mocked(jwt_decode).mockReturnValue({ authorities: ["PLAYER"] });
        
        render(
            <MemoryRouter initialEntries={["/dashboard"]}>
                <App />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByTestId("dashboard-page")).toBeInTheDocument();
        });
    });

    it("renders Admin Users page when authenticated as ADMIN and navigating to /users", async () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("fake-jwt");
        vi.mocked(jwt_decode).mockReturnValue({ authorities: ["ADMIN"] });
        
        render(
            <MemoryRouter initialEntries={["/users"]}>
                <App />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByTestId("admin-users-page")).toBeInTheDocument();
        });
    });

    it("renders Security Shield (DoNotHackMyApp) when navigating to /do-not-hack-my-app", () => {
        render(
            <MemoryRouter initialEntries={["/do-not-hack-my-app"]}>
                <App />
            </MemoryRouter>
        );

        expect(screen.getByText(/Don' t Hack My App/i)).toBeInTheDocument();
        expect(screen.getByText(/HACKER DETECTED/i)).toBeInTheDocument();
    });

    it("shows error fallback when an error occurs", async () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("");
        shouldThrowError = true;
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

        render(
            <MemoryRouter initialEntries={["/"]}>
                <App />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByRole("alert")).toBeInTheDocument();
            expect(screen.getByText(/Something went wrong:/i)).toBeInTheDocument();
            expect(screen.getByText("Test Error")).toBeInTheDocument();
        });

        consoleSpy.mockRestore();
    });
});
