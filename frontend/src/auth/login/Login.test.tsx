
import { render, screen, waitFor, act } from "../../test-utils";
import userEvent from "@testing-library/user-event";
import {
  describe,
  it,
  expect,
  vi,
  beforeEach,
  afterEach,
} from "vitest";
import Login from "./Login";
import tokenService from "../../services/token.service.ts";

vi.mock("../../services/token.service.ts");

describe("Login Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    vi.stubGlobal("fetch", vi.fn());

    Object.defineProperty(window, "location", {
      configurable: true,
      writable: true,
      value: { href: "http://localhost/login" },
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("performs successful login and redirects to dashboard", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: async () => ({
        token: "mock-jwt",
        username: "testuser",
      }),
    } as Response);

    const user = userEvent.setup();
    render(<Login />);

    const usernameInput = screen.getByLabelText(/Username/i);
    const passwordInput = screen.getByLabelText(/Password/i);

    await user.type(usernameInput, "testuser");
    await user.type(passwordInput, "password123");

    await user.click(screen.getByRole("button", { name: /^Login$/i }));

    await waitFor(() => {
      expect(tokenService.setUser).toHaveBeenCalledWith(
        expect.objectContaining({
          token: "mock-jwt",
          username: "testuser",
        })
      );

      expect(tokenService.updateLocalAccessToken).toHaveBeenCalledWith(
        "mock-jwt"
      );

      expect(window.location.href).toBe("/dashboard");
    });
  });

  it("shows error message on failed login", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({}),
    } as Response);

    const user = userEvent.setup();
    render(<Login />);

    await user.type(screen.getByLabelText(/Username/i), "wronguser");
    await user.type(screen.getByLabelText(/Password/i), "wrongpass");

    await user.click(screen.getByRole("button", { name: /^Login$/i }));

    expect(
      await screen.findByText(/Invalid login attempt/i)
    ).toBeInTheDocument();

    expect(window.location.href).not.toBe("/dashboard");
  });

  it("shows fallback error message when fetch rejects with non-Error value", async () => {
    vi.mocked(fetch).mockRejectedValue("raw string error");

    const user = userEvent.setup();
    render(<Login />);

    await user.type(screen.getByLabelText(/Username/i), "wronguser");
    await user.type(screen.getByLabelText(/Password/i), "wrongpass");

    await user.click(screen.getByRole("button", { name: /^Login$/i }));

    expect(
      await screen.findByText(/Error in login/i)
    ).toBeInTheDocument();
  });
});