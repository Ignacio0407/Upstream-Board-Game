import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { MemoryRouter } from "react-router-dom";
import AchievementCreate from "./AchievementCreate";
import { post } from "../../util/fetchers";
import { Metric } from "../../interfaces/Achievement";

// --- Mocks ---

vi.mock("../../services/token.service", () => ({
    default: {
        getLocalAccessToken: vi.fn(() => "mock-jwt"),
    },
}));

vi.mock("../../util/fetchers", () => ({ post: vi.fn() }));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return { ...actual, useNavigate: vi.fn(() => mockNavigate) };
});

let capturedOnSubmit: (data: any) => Promise<void>;
vi.mock("../../components/Form/EditFormGenerator", () => ({
    EditFormGenerator: vi.fn(({ onSubmit, entityToEdit, selectOptions }) => {
        capturedOnSubmit = onSubmit;
        return (
            <div data-testid="edit-form-generator">
                <span data-testid="entity-name">{entityToEdit?.name || "empty"}</span>
                <span data-testid="select-metric">{selectOptions?.metric?.join(",")}</span>
            </div>
        );
    }),
}));

describe("AchievementCreate Component", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(window, "alert").mockImplementation(() => {});
    });

    it("renders the page title and initial form state", () => {
        render(
            <MemoryRouter>
                <AchievementCreate />
            </MemoryRouter>
        );

        expect(screen.getByText(/Create Achievement/i)).toBeInTheDocument();
        expect(screen.getByTestId("entity-name")).toHaveTextContent("empty");
        
        const metricValues = Object.values(Metric).join(",");
        expect(screen.getByTestId("select-metric")).toHaveTextContent(metricValues);
    });

    it("calls post and navigates on successful submission", async () => {
        const newAchievement = { name: "First Achievement", metric: Metric.VICTORIES };
        vi.mocked(post).mockResolvedValue({
            json: async () => ({ ...newAchievement, id: 100 })
        } as Response);

        render(
            <MemoryRouter>
                <AchievementCreate />
            </MemoryRouter>
        );

        await capturedOnSubmit(newAchievement);

        await waitFor(() => {
            expect(post).toHaveBeenCalledWith("/api/v1/achievements", "mock-jwt", newAchievement);
            expect(mockNavigate).toHaveBeenCalledWith("/achievements", expect.objectContaining({
                state: { updated: expect.objectContaining({ id: 100 }) }
            }));
        });
    });

    it("shows an alert on submission error", async () => {
        vi.mocked(post).mockRejectedValue(new Error("Network Error"));

        render(
            <MemoryRouter>
                <AchievementCreate />
            </MemoryRouter>
        );

        await capturedOnSubmit({ name: "Error Test" });

        await waitFor(() => {
            expect(window.alert).toHaveBeenCalledWith("Network Error");
        });
    });
});
