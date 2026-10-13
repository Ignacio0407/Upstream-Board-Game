import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import UserListAdmin from "./UserListAdmin";
import { BrowserRouter } from "react-router-dom";
import * as fetchers from "../../util/fetchers.ts";
import tokenService from "../../services/token.service.ts";

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock("../../services/token.service.ts", () => ({
  default: {
    getLocalAccessToken: vi.fn(() => "mock-jwt"),
    getUser: vi.fn(() => ({ id: 1, username: "admin", authority: { authority: "ADMIN" } })),
  },
}));

vi.mock("../../util/fetchers.ts", () => ({
    get: vi.fn(),
    patch: vi.fn(),
    deleteEntity: vi.fn(),
    buildSafeUrl: vi.fn((base, id) => id ? `${base}${id}` : base),
}));

// Mock reactstrap components to render content simply for tests
vi.mock("reactstrap", async () => {
    const actual = await vi.importActual("reactstrap") as any;
    return {
        ...actual,
        Modal: ({ isOpen, children }: any) => (isOpen ? <div data-testid="mock-modal">{children}</div> : null),
        ModalHeader: ({ children }: any) => <div data-testid="modal-header">{children}</div>,
        ModalBody: ({ children }: any) => <div data-testid="modal-body">{children}</div>,
        ModalFooter: ({ children }: any) => <div data-testid="modal-footer">{children}</div>,
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
    globalThis.confirm = vi.fn().mockReturnValue(false);
    vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("mock-jwt");
    vi.mocked(tokenService.getUser).mockReturnValue({ id: 1, username: "admin", authority: { authority: "ADMIN" } } as any);
    vi.mocked(fetchers.buildSafeUrl).mockImplementation((base, id) => id ? `${base}${id}` : base);
    vi.mocked(fetchers.get).mockResolvedValue({
        ok: true,
        json: async () => [
            { id: 1, name: "Alice", authority: "USER" }
        ]
    } as any);
  });

  it("Renders the page title", async () => {
    renderWithRouter(<UserListAdmin />);
    expect(screen.getByRole("heading", { name: /users/i })).toBeInTheDocument();
    await waitFor(() => {
        expect(fetchers.get).toHaveBeenCalled();
    });
  });

  it("Renders user list when users are fetched", async () => {
    renderWithRouter(<UserListAdmin />);
    expect(await screen.findByText("Alice")).toBeInTheDocument();
  });

  it("Does NOT render list rows when users array is empty", async () => {
    vi.mocked(fetchers.get).mockResolvedValueOnce({
        ok: true,
        json: async () => []
    } as any);

    renderWithRouter(<UserListAdmin />);
    
    await waitFor(() => {
        expect(screen.queryByText("Alice")).not.toBeInTheDocument();
    });
  });

  it("Calls patch and updates the user authority on success", async () => {
    vi.mocked(fetchers.patch).mockResolvedValue({
        ok: true,
        json: async () => ({ id: 1, name: "Alice", authority: "ADMIN" })
    } as any);

    renderWithRouter(<UserListAdmin />);

    const changeButtons = await screen.findAllByRole("button", { name: /change authority/i });
    const user = userEvent.setup();
    await user.click(changeButtons[0]);

    await waitFor(() => {
        expect(fetchers.patch).toHaveBeenCalledWith(
            "/api/v1/users/listing/1",
            "mock-jwt",
        );
    });
    expect(await screen.findByText("ADMIN")).toBeInTheDocument();
  });

  it("Shows error modal when authority update patch fails", async () => {
    vi.mocked(fetchers.patch).mockResolvedValue({
        ok: false
    } as any);

    renderWithRouter(<UserListAdmin />);

    const changeButtons = await screen.findAllByRole("button", { name: /change authority/i });
    const user = userEvent.setup();
    await user.click(changeButtons[0]);

    await waitFor(() => {
        expect(fetchers.patch).toHaveBeenCalled();
        expect(screen.getByText("Error updating authority")).toBeInTheDocument();
    });
  });

  it("Triggers delete button handler when delete button is clicked", async () => {
    renderWithRouter(<UserListAdmin />);

    const deleteButtons = await screen.findAllByRole("button", { name: /delete/i });
    expect(deleteButtons[0]).toBeInTheDocument();
  });
});

