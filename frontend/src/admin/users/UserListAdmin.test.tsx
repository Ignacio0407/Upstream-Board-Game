import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import UserListAdmin from "./UserListAdmin";
import { patch } from "../../util/fetchers";
import useFetchState from "../../util/useFetchState";

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock("../../services/token.service", () => ({
  default: {
    getLocalAccessToken: vi.fn(() => "mock-jwt"),
    getUser: vi.fn(() => ({ id: 1, username: "admin" })),
  },
}));

// useFetchState: Know the users returned by the hook and control the setUsers function
const mockSetUsers = vi.fn();
vi.mock("../../util/useFetchState", () => ({ default: vi.fn(() => [[], mockSetUsers]) }));

vi.mock("../../util/deleteFromList", () => ({ default: vi.fn() }));

vi.mock("../../util/fetchers", () => ({ patch: vi.fn() }));

// getErrorModal: returns a testeable div when visible=true
vi.mock("../../util/getErrorModal", () => ({
  default: vi.fn((setVisible, visible, message) => visible ? <div data-testid="error-modal">{message}</div> : null),
}));

// ListGenerator: minimum stub that exposes the props for testing
vi.mock("../../components/ListGenerator/ListGenerator", () => ({
  default: vi.fn(({ data, editButton, deleteButton }) => (
    <div data-testid="list-generator">
      {data.map((u: any) => (
        <div key={u.id} data-testid={`user-row-${u.id}`}>
          <span>{u.name}</span>
          <span>{u.authority}</span>
          {editButton && editButton(u)}
          {deleteButton && deleteButton(u)}
        </div>
      ))}
    </div>
  )),
}));

// ── Helpers ──────────────────────────────────────────────────────────────────

const mockUsers = [ { id: 1, name: "Alice", authority: "USER" }, { id: 2, name: "Bob",   authority: "ADMIN" } ];

function setupUsers(users = mockUsers) {
  vi.mocked(useFetchState).mockReturnValue([users, mockSetUsers] as any);
}

// ── Tests ────────────────────────────────────────────────────────────────────

describe("UserListAdmin", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    // Default: list with users
    setupUsers();
  });

  // ── 1. Basic rendering ─────────────────────────────────────────────────

  it("Renders the page title", () => {
    render(<UserListAdmin />);
    expect(screen.getByRole("heading", { name: /users/i })).toBeInTheDocument();
  });

  // ── 2. Conditional users && <ListGenerator> ───────────────────────────────

  it("Renders ListGenerator when users array is non-empty", () => {
    render(<UserListAdmin />);
    expect(screen.getByTestId("list-generator")).toBeInTheDocument();
  });

  it("Does NOT render ListGenerator when users is an empty array (falsy-like via null)", () => {
    // useFetchState returns null → users condition is falsy
    vi.mocked(useFetchState).mockReturnValue([null as any, mockSetUsers] as any);
    render(<UserListAdmin />);
    expect(screen.queryByTestId("list-generator")).not.toBeInTheDocument();
  });

  // ── 3. updateUserAuthority — Success branch ───────────────────────────────────

  it("Calls patch and updates the user authority on success", async () => {
    const updatedUser = { id: 1, name: "Alice", authority: "ADMIN" };
    vi.mocked(patch).mockResolvedValue({
      json: vi.fn().mockResolvedValue(updatedUser),
    } as any);

    render(<UserListAdmin />);

    // Clicks "Change authority" in user with id=1
    await userEvent.click(screen.getAllByRole("button", { name: /change authority/i })[0]);

    await waitFor(() => {
      expect(patch).toHaveBeenCalledWith("/api/v1/users/listing/1", "mock-jwt");
    });

    // setUsers must have been called with the functional updater
    expect(mockSetUsers).toHaveBeenCalledTimes(1);
    // Verify the updater transforms the array correctly
    const updater = mockSetUsers.mock.calls[0][0];
    const result = updater(mockUsers);
    expect(result[0].authority).toBe("ADMIN"); // id=1 actualizado
    expect(result[1].authority).toBe("ADMIN"); // id=2 sin cambios
  });

  // ── 4. updateUserAuthority — Error branch ───────────────────────────────────

  it("sets error message and shows modal when patch throws", async () => {
    vi.mocked(patch).mockRejectedValue(new Error("Network error"));

    render(<UserListAdmin />);

    await userEvent.click(screen.getAllByRole("button", { name: /change authority/i })[0]);

    await waitFor(() => {
      expect(screen.getByTestId("error-modal")).toBeInTheDocument();
      expect(screen.getByTestId("error-modal")).toHaveTextContent("Error updating authority");
    });
  });

  // ── 5. Initial Error Modal ─────────────────────────────────────────────

  it("does not show error modal on initial render", () => {
    render(<UserListAdmin />);
    expect(screen.queryByTestId("error-modal")).not.toBeInTheDocument();
  });

  // ── 6. renderUser — Shown user data ────────────────────────────

  it("renders user name and authority via customRender", () => {
    render(<UserListAdmin />);
    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("USER")).toBeInTheDocument();
    expect(screen.getByText("Bob")).toBeInTheDocument();
    expect(screen.getByText("ADMIN")).toBeInTheDocument();
  });
});