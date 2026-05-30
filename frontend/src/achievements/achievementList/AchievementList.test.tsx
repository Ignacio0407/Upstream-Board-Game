import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import AchievementList from "./AchievementList";
import { MemoryRouter } from "react-router-dom";
import jwt_decode from "jwt-decode";
import { get } from "../../util/fetchers";
import tokenService from "../../services/token.service";
import { emptyUser } from "../../interfaces/User";

// ── Mocks ─────────────────────────────────────────────────────────────────────

vi.mock("../../util/deleteFromList",     () => ({ default: vi.fn() }));
vi.mock("../../util/deleteSomeFromList", () => ({ default: vi.fn() }));

vi.mock("../../components/ListGenerator/ListGenerator", () => ({
  default: ({ data, createButton, editCondition, deleteCondition, customRender }: any) => (
    <div data-testid="list-generator">
      {createButton  && <div data-testid="create-button"  />}
      {editCondition && <div data-testid="edit-condition" />}
      {deleteCondition && <div data-testid="delete-condition" />}
      {data && data.map((a: any) => (
        <div key={a.id} data-testid={`achievement-row-${a.id}`}>
          {customRender(a)}
        </div>
      ))}
    </div>
  ),
}));

// ── Helpers ───────────────────────────────────────────────────────────────────

function renderComponent() {
  return render(<MemoryRouter><AchievementList /></MemoryRouter>);
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe("AchievementList", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("mock-jwt");
    vi.mocked(tokenService.getUser).mockReturnValue({...emptyUser, id: 1, username: "testuser", roles: ["PLAYER"] });
  });

  // ── 1. Basic rendering ──────────────────────────────────────────────────

  it("renders the player card with user stats", async () => {
    renderComponent();
    await waitFor(() => {
        expect(screen.getByText("testuser")).toBeInTheDocument();
        expect(screen.getByText(/total wins/i)).toBeInTheDocument();
        expect(screen.getByText(/total games/i)).toBeInTheDocument();
        expect(screen.getByText(/total points/i)).toBeInTheDocument();
    });
  });

  it("renders the Achievements title", () => {
    renderComponent();
    expect(screen.getByRole("heading", { name: /achievements/i })).toBeInTheDocument();
  });

  // ── 2. Achievements fetch ──────────────────────────────────

  it("fetches and renders achievements on mount", async () => {
    renderComponent();
    await waitFor(() => {
        expect(screen.getByText("First Win")).toBeInTheDocument();
        expect(screen.getByText("No Badge")).toBeInTheDocument();
    });
  });

  // ── 3. JWT present → getRolesFromJWT is called ───────────────────────────

  it("decodes JWT when token is present", () => {
    renderComponent();
    expect(jwt_decode).toHaveBeenCalledWith("mock-jwt");
  });

  // ── 4. JWT ausent → getRolesFromJWT is NOT called ─────────────────────────

  it("does not decode JWT when token is empty", () => {
    vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("");
    renderComponent();
    expect(jwt_decode).not.toHaveBeenCalled();
  });

  // ── 5. JWT invalid → silence catch, does not explode ────────────────────────

  it("handles invalid JWT without crashing", () => {
    vi.mocked(jwt_decode).mockImplementation(() => { throw new Error("Invalid token"); });
    expect(() => renderComponent()).not.toThrow();
  });

  // ── 6. renderAchievement — achievement unlocked → class "achieved" ─────────────

  it("applies 'achieved' class to achievements the user has completed", async () => {
    renderComponent();
    // id=1 is in userAchievements → must have class "achieved"
    await waitFor(() => {
        const achievedCell = screen.getByText("First Win").closest("td");
        expect(achievedCell).toHaveClass("achieved");
    });
  });

  // ── 7. renderAchievement — achievement locked → class "text-center table-cell"

  it("applies default class to achievements not yet completed", async () => {
    renderComponent();
    // id=2 is NOT in userAchievements
    await waitFor(() => {
        const notAchievedCell = screen.getByText("No Badge").closest("td");
        expect(notAchievedCell).toHaveClass("text-center", "table-cell");
    });
  });

  // ── 8. renderAchievement — badgeImage present ────────────────────────────

  it("renders the badge image when badgeImage is provided", async () => {
    renderComponent();
    await waitFor(() => {
        const img = screen.getByAltText("First Win");
        expect(img).toHaveAttribute("src", "/img/badge1.png");
    });
  });

  // ── 9. renderAchievement — badgeImage missing → imgnotfound ───────────────

  it("renders fallback image when badgeImage is null", async () => {
    renderComponent();
    await waitFor(() => {
        const img = screen.getByAltText("No Badge");
        expect(img).toHaveAttribute("src", "/images/achievements/notFoundImage.png");
    });
  });

  // ── 10. Roles ADMIN → admin buttons visible ───────────────────────────

  it("shows admin controls when user role is ADMIN", async () => {
    vi.mocked(tokenService.getUser).mockReturnValue({...emptyUser, id: 1, username: "admin", roles: ["ADMIN"] });
    renderComponent();
    await waitFor(() => {
        expect(screen.getByTestId("create-button")).toBeInTheDocument();
        expect(screen.getByTestId("edit-condition")).toBeInTheDocument();
        expect(screen.getByTestId("delete-condition")).toBeInTheDocument();
    });
  });

  // ── 11. Roles PLAYER → admin buttons hidden ───────────────────────────

  it("hides admin controls when user role is PLAYER", async () => {
    renderComponent(); // default: roles: ["PLAYER"]
    await waitFor(() => {
        expect(screen.queryByTestId("create-button")).not.toBeInTheDocument();
        expect(screen.queryByTestId("edit-condition")).not.toBeInTheDocument();
        expect(screen.queryByTestId("delete-condition")).not.toBeInTheDocument();
    });
  });
});