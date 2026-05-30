import { vi } from "vitest";
import { render, screen } from "@testing-library/react";
import CreateGame from "./CreateGame"
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

import * as reactRouterDom from "react-router-dom";

vi.mock("../services/token.service", () => ({
  default: {
    getLocalAccessToken: vi.fn(() => "mock-jwt-token"),
    getUser: vi.fn(() => ({ id: 1 })),
  }
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
  });

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

  test("validates and submits form", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    const nameInput = screen.getByLabelText(/Name/i);
    const createButton = screen.getByRole("button", { name: /Create/i });

    // Input validation
    await user.type(nameInput, "Test Game");
    expect(nameInput).toHaveValue("Test Game");

    // Submit form
    await user.click(createButton);
    expect(mockNavigate).not.toHaveBeenCalled(); // Mock fetch behavior for further testing
  });

  test("cancel button navigates back", async () => {
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
});