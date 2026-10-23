
import { render, screen, waitFor } from "../../test-utils";
import userEvent from "@testing-library/user-event";
import {
  describe,
  it,
  expect,
  vi,
  beforeEach,
  afterEach,
} from "vitest";
import Register from "./Register";
import tokenService from "../../services/token.service.ts";

vi.mock("../../services/token.service.ts");

describe("Register Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    vi.stubGlobal("fetch", vi.fn());

    Object.defineProperty(window, "location", {
      configurable: true,
      writable: true,
      value: { href: "http://localhost/register" },
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("performs successful registration and login", async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          token: "mock-jwt",
          username: "newuser",
        }),
      } as Response);

    const user = userEvent.setup();
    render(<Register />);

    await user.type(screen.getByLabelText(/Username/i), "newuser");
    await user.type(screen.getByLabelText(/Password/i), "password123");
    await user.type(screen.getByLabelText(/First Name/i), "First");
    await user.type(screen.getByLabelText(/Last Name/i), "Last");

    await user.click(screen.getByRole("button", { name: /^Player$/i }));
    await user.click(screen.getByRole("button", { name: /^Register$/i }));

    await waitFor(() => {
        expect(tokenService.setUser).toHaveBeenCalledWith(
            expect.objectContaining({
            token: "mock-jwt",
            username: "newuser",
            })
        );

        expect(tokenService.updateLocalAccessToken).toHaveBeenCalledWith(
            "mock-jwt"
        );

        expect(window.location.href).toBe("/dashboard");
    });
  });

  it("shows error if registration fails", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: false,
      status: 409,
      json: async () => ({
        message: "User already exists",
      }),
    } as Response);

    const user = userEvent.setup();
    render(<Register />);

    await user.type(screen.getByLabelText(/Username/i), "existinguser");
    await user.type(screen.getByLabelText(/Password/i), "password123");
    await user.type(screen.getByLabelText(/First Name/i), "First");
    await user.type(screen.getByLabelText(/Last Name/i), "Last");

    await user.click(screen.getByRole("button", { name: /^Register$/i }));

    expect(
      await screen.findByText(/User already exists/i)
    ).toBeInTheDocument();

    expect(fetch).toHaveBeenCalledTimes(1);
  });

  it("allows selecting Admin role and handles login failure after registration", async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      } as Response)
      .mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({
          message: "Login failed",
        }),
      } as Response);

    const user = userEvent.setup();
    render(<Register />);

    await user.type(screen.getByLabelText(/Username/i), "adminuser");
    await user.type(screen.getByLabelText(/Password/i), "password123");
    await user.type(screen.getByLabelText(/First Name/i), "Admin");
    await user.type(screen.getByLabelText(/Last Name/i), "User");

    await user.click(screen.getByRole("button", { name: /^Admin$/i }));
    await user.click(screen.getByRole("button", { name: /^Register$/i }));

    expect(
      await screen.findByText(/Login failed after registration/i)
    ).toBeInTheDocument();

    expect(fetch).toHaveBeenCalledTimes(2);
    expect(tokenService.setUser).not.toHaveBeenCalled();
  });
});
