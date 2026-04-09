import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import AchievementEdit from "./AchievementEdit";
import { put } from "../../util/fetchers";
import { Metric } from "../../interfaces/Achievement";

// ── Mocks ─────────────────────────────────────────────────────────────────────

vi.mock("../../services/token.service", () => ({
  default: {
    getLocalAccessToken: vi.fn(() => "mock-jwt"),
    getUser: vi.fn(() => ({ id: 1, username: "admin", roles: ["ADMIN"] })),
  },
}));

vi.mock("../../util/fetchers", () => ({ put: vi.fn() }));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return { ...actual, useNavigate: vi.fn(() => mockNavigate) };
});

// Capture onSubmit to call it in tests
let capturedOnSubmit: (data: any) => Promise<void>;
vi.mock("../../components/Form/EditFormGenerator", () => ({
  EditFormGenerator: vi.fn(({ onSubmit, entityToEdit, selectOptions, actualUser, requiredAuth }) => {
    capturedOnSubmit = onSubmit;
    return (
      <div data-testid="edit-form-generator">
        <span data-testid="entity-id">{entityToEdit?.id ?? "no-entity"}</span>
        <span data-testid="select-metric">{selectOptions?.metric?.join(",")}</span>
        <span data-testid="required-auth">{requiredAuth}</span>
        <span data-testid="actual-user">{actualUser?.username}</span>
      </div>
    );
  }),
}));

// ── Helpers ───────────────────────────────────────────────────────────────────

const mockAchievement = {id: 42, name: "Speed Run", description: "Finish fast", badgeImage: "/img/badge.png", threshold: 10,
    metric: Metric.VICTORIES};

function renderWithState(locationState: any) {
  return render(
    <MemoryRouter initialEntries={[{ pathname: "/achievements/42", state: locationState }]}>
      <Routes>
        <Route path="/achievements/:id" element={<AchievementEdit />} />
      </Routes>
    </MemoryRouter>
  );
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe("AchievementEdit", () => {

  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(window, "alert").mockImplementation(() => {});
  });

  // ── 1. Basic rendering ──────────────────────────────────────────────────

  it("Renders the page title", () => {
    renderWithState({ achievement: mockAchievement });
    expect(screen.getByRole("heading", { name: /edit achievement/i })).toBeInTheDocument();
  });

  // ── 2. Props passed to EditFormGenerator ──────────────────────────────────

  it("Passes achievement, selectOptions, requiredAuth and actualUser to EditFormGenerator", () => {
    renderWithState({ achievement: mockAchievement });

    expect(screen.getByTestId("entity-id")).toHaveTextContent("42");
    expect(screen.getByTestId("required-auth")).toHaveTextContent("ADMIN");
    expect(screen.getByTestId("actual-user")).toHaveTextContent("admin");

    // All enum Metric values are present
    const metricValues = Object.values(Metric).join(",");
    expect(screen.getByTestId("select-metric")).toHaveTextContent(metricValues);
  });

  // ── 3. state?.achievement undefined ───────────────────────────────────────

  it("Renders without crashing when state has no achievement", () => {
    renderWithState(null);
    expect(screen.getByTestId("entity-id")).toHaveTextContent("no-entity");
  });

  // ── 4. onSubmit — happy path → navigate ───────────────────────────────────

  it("Calls put and navigates to /achievements on successful submit", async () => {
    const updatedAchievement = { ...mockAchievement, name: "Updated" };
    vi.mocked(put).mockResolvedValue({
      json: vi.fn().mockResolvedValue(updatedAchievement),
    } as any);

    renderWithState({ achievement: mockAchievement });
    await capturedOnSubmit(mockAchievement);

    await waitFor(() => {
      expect(put).toHaveBeenCalledWith(
        `/api/v1/achievements/${mockAchievement.id}`,
        "mock-jwt",
        mockAchievement
      );
      expect(mockNavigate).toHaveBeenCalledWith("/achievements", {
        state: { updated: updatedAchievement },
      });
    });
  });

  // ── 5. onSubmit — error is of instance Error → alert with message ──────────

  it("alerts error.message when put rejects with an Error instance", async () => {
    vi.mocked(put).mockRejectedValue(new Error("Server unavailable"));

    renderWithState({ achievement: mockAchievement });
    await capturedOnSubmit(mockAchievement);

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith("Server unavailable");
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  // ── 6. onSubmit — error is NOT of instance Error → alert with String(error) ─

  it("alerts String(error) when put rejects with a non-Error value", async () => {
    vi.mocked(put).mockRejectedValue("timeout");

    renderWithState({ achievement: mockAchievement });
    await capturedOnSubmit(mockAchievement);

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith("timeout");
    });
  });
});