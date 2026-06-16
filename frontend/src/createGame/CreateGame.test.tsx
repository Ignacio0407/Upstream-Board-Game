import { vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import CreateGame from "./CreateGame";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import * as reactRouterDom from "react-router-dom";
import { post } from "../util/fetchers.ts";

vi.mock("../services/token.service", () => ({
  default: {
    getLocalAccessToken: vi.fn(() => "mock-jwt-token"),
    getUser: vi.fn(() => ({ id: 1 })),
  }
}));

vi.mock("../util/fetchers.ts", () => ({
  post: vi.fn(),
}));

vi.mock("../util/useFetchState", () => ({
  default: vi.fn(() => [[], vi.fn()])
}));

vi.mock('sockjs-client', () => ({ default: vi.fn() }));
vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn().mockImplementation(() => ({
    activate: vi.fn(),
    publish: vi.fn(),
    deactivate: vi.fn(),
  })),
}));

vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useNavigate: vi.fn(),
    };
});

describe("CreateGame Component", () => {
  const mockNavigate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
    
    vi.spyOn(console, 'error').mockImplementation(() => {});
    vi.spyOn(window, 'alert').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // --- Rendering and navigation tests ---

  test("renders form elements correctly", () => {
    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Create/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Cancel/i })).toBeInTheDocument();
  });

  test("cancel button navigates back to dashboard", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    const cancelButton = screen.getByRole("button", { name: /Cancel/i });
    await user.click(cancelButton);
    expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
  });

  // --- Submit Integration Tests ---

  test("submits form successfully and navigates to waiting room (Happy Path)", async () => {
    const user = userEvent.setup();
  
    vi.mocked(post).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ id: 42 })
    } as any);

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    const nameInput = screen.getByLabelText(/Name/i);
    const createButton = screen.getByRole("button", { name: /Create/i });

    await user.type(nameInput, "My Awesome Game");
    await user.click(createButton);

    await waitFor(() => {
      expect(post).toHaveBeenCalledWith(
        '/api/v1/matches/matchCreator/1', 
        'mock-jwt-token', 
        { name: 'My Awesome Game', password: '' }
      );
      expect(mockNavigate).toHaveBeenCalledWith("/matches/42/waiting");
    });
  });

  // --- Error Handling Tests (Lines 32-38) ---

  test("handles API business logic error (response.ok is false)", async () => {
    const user = userEvent.setup();
    
    vi.mocked(post).mockResolvedValueOnce({
      ok: false,
      text: async () => "Match name already exists"
    } as any);

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/Name/i), "Conflict Game");
    await user.click(screen.getByRole("button", { name: /Create/i }));

    await waitFor(() => {
      expect(console.error).toHaveBeenCalledWith('Error al crear la partida:', expect.any(Error));
      expect(window.alert).toHaveBeenCalledWith('Error al crear la partida: Match name already exists');
      expect(mockNavigate).not.toHaveBeenCalled(); // No debe navegar
    });
  });

  test("handles API error with empty text fallback (Unknown error)", async () => {
    const user = userEvent.setup();
 
    vi.mocked(post).mockResolvedValueOnce({
      ok: false,
      text: async () => "" 
    } as any);

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/Name/i), "Unknown Game");
    await user.click(screen.getByRole("button", { name: /Create/i }));

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith('Error al crear la partida: Unknown error');
    });
  });

  test("handles catastrophic network failure (throw Error)", async () => {
    const user = userEvent.setup();
    
    vi.mocked(post).mockRejectedValueOnce(new Error("Network Error"));

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText(/Name/i), "Network Fail Game");
    await user.click(screen.getByRole("button", { name: /Create/i }));

    await waitFor(() => {
      expect(console.error).toHaveBeenCalledWith('Error al crear la partida:', expect.any(Error));
      expect(window.alert).toHaveBeenCalledWith('Error al crear la partida: Network Error');
    });
  });
});