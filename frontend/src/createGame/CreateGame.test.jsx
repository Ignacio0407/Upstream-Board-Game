import { render, screen } from "@testing-library/react";
import CreateGame from "./CreateGame"
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

jest.mock("../services/token.service", () => ({
  getLocalAccessToken: jest.fn(() => "mock-jwt-token"),
  getUser: jest.fn(() => ({ id: 1 })),
}));

jest.mock("../util/useFetchState", () => jest.fn(() => [[], jest.fn()]));

jest.mock('sockjs-client', () => jest.fn());
jest.mock('@stomp/stompjs', () => ({
  Client: jest.fn().mockImplementation(() => ({
    activate: jest.fn(),
    publish: jest.fn(),
    deactivate: jest.fn(),
  })),
}));

describe("CreateGame Component", () => {
  test("renders form elements correctly", () => {
    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Crear/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Cancelar/i })).toBeInTheDocument();
  });

  test("validates and submits form", async () => {
    const user = userEvent.setup();
    const mockNavigate = jest.fn();
    jest.spyOn(require("react-router-dom"), "useNavigate").mockReturnValue(mockNavigate);

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    const nameInput = screen.getByLabelText(/Name/i);
    const createButton = screen.getByRole("button", { name: /Crear/i });

    // Input validation
    await user.type(nameInput, "Test Game");
    expect(nameInput).toHaveValue("Test Game");

    // Submit form
    await user.click(createButton);
    expect(mockNavigate).not.toHaveBeenCalled(); // Mock fetch behavior for further testing
  });

  test("cancel button navigates back", async () => {
    const user = userEvent.setup();
    const mockNavigate = jest.fn();
    jest.spyOn(require("react-router-dom"), "useNavigate").mockReturnValue(mockNavigate);

    render(
      <MemoryRouter>
        <CreateGame />
      </MemoryRouter>
    );

    const cancelButton = screen.getByRole("button", { name: /Cancelar/i });
    await user.click(cancelButton);
    expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
  });
});