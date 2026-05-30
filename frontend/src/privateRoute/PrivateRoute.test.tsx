import { render, screen, waitFor } from "../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import PrivateRoute from "./PrivateRoute";
import tokenService from "../services/token.service.ts";

vi.mock("../services/token.service.ts");
vi.mock("../auth/login/Login.tsx", () => ({ default: () => <div data-testid="login-page">Login Page</div> }));

describe("PrivateRoute Component", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("renders children when token is valid", async () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("valid-jwt");

        render(<PrivateRoute><div>Protected Content</div></PrivateRoute>);

        expect(screen.getByText(/Loading.../i)).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByText("Protected Content")).toBeInTheDocument();
        });
    });

    it("renders Login component when token is invalid", async () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("invalid-jwt");

        render(<PrivateRoute><div>Protected Content</div></PrivateRoute>);

        await waitFor(() => {
            expect(screen.getByTestId("login-page")).toBeInTheDocument();
        });
    });

    it("renders Login component when no token is present", async () => {
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("");

        render(<PrivateRoute><div>Protected Content</div></PrivateRoute>);

        await waitFor(() => {
            expect(screen.getByTestId("login-page")).toBeInTheDocument();
        });
    });
});
