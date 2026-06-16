import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import UserListAdmin from "./UserListAdmin";
import { BrowserRouter } from "react-router-dom";
import * as fetchers from "../../util/fetchers";

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock("../../services/token.service.ts", () => ({
  default: {
    getLocalAccessToken: vi.fn(() => "mock-jwt"),
    getUser: vi.fn(() => ({ id: 1, username: "admin", authority: { authority: "ADMIN" } })),
  },
}));

vi.mock("../../util/fetchers", () => ({
    get: vi.fn(),
    patch: vi.fn(),
    deleteEntity: vi.fn()
}));

// Mock reactstrap components to render content simply for tests
vi.mock("reactstrap", async () => {
    const actual = await vi.importActual("reactstrap") as any;
    return {
        ...actual,
        Modal: ({ isOpen, children }: any) => (isOpen ? <div data-testid="mock-modal">{children}</div> : null),
        ModalBody: ({ children }: any) => <div data-testid="modal-body">{children}</div>,
        Fade: ({ in: isOpen, children }: any) => (isOpen ? <div>{children}</div> : null),
    };
});

// ── Wrapper ──────────────────────────────────────────────────────────────────

const renderWithRouter = (ui: React.ReactElement) => {
  return render(ui, { wrapper: BrowserRouter });
};

// ── Tests ────────────────────────────────────────────────────────────────────

describe("UserListAdmin", () => {

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("Renders the page title", async () => {
    vi.mocked(fetchers.get).mockResolvedValue({
        ok: true,
        json: async () => []
    } as any);

    renderWithRouter(<UserListAdmin />);
    
    expect(screen.getByRole("heading", { name: /users/i })).toBeInTheDocument();
    
    await waitFor(() => {
        expect(fetchers.get).toHaveBeenCalled();
    });
  });

  it("Renders user list when users are fetched", async () => {
    vi.mocked(fetchers.get).mockResolvedValue({
        ok: true,
        json: async () => [
            { id: 1, name: "Alice", authority: "USER" },
            { id: 2, name: "Bob", authority: "ADMIN" }
        ]
    } as any);

    renderWithRouter(<UserListAdmin />);
    
    expect(await screen.findByText("Alice")).toBeInTheDocument();
    expect(await screen.findByText("Bob")).toBeInTheDocument();
  });

  it("Does NOT render list rows when users array is empty", async () => {
    vi.mocked(fetchers.get).mockResolvedValue({
        ok: true,
        json: async () => []
    } as any);

    renderWithRouter(<UserListAdmin />);
    
    await waitFor(() => {
        expect(screen.queryByText("Alice")).not.toBeInTheDocument();
    });
  });

  it("Calls patch and updates the user authority on success", async () => {
    vi.mocked(fetchers.get).mockResolvedValue({
        ok: true,
        json: async () => [
            { id: 1, name: "Alice", authority: "USER" }
        ]
    } as any);

    vi.mocked(fetchers.patch).mockResolvedValue({
        ok: true,
        json: async () => ({ id: 1, name: "Alice", authority: "ADMIN" })
    } as any);

    const user = userEvent.setup();
    renderWithRouter(<UserListAdmin />);

    const changeButtons = await screen.findAllByRole("button", { name: /change authority/i });
    await user.click(changeButtons[0]);

    await waitFor(() => {
        const rows = screen.getAllByRole("row");
        const aliceRow = rows.find(row => row.textContent?.includes("Alice"));
        expect(aliceRow?.textContent).toContain("ADMIN");
    }, { timeout: 3000 });
  });
});

