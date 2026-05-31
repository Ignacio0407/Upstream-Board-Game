import { render, screen, fireEvent, waitFor } from "../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import EndGame from "./EndGame";
import tokenService from "../services/token.service.ts";
import * as reactRouterDom from "react-router-dom";
import { get, patch } from "../util/fetchers.ts";

// --- Mocks ---

vi.mock("../services/token.service.ts");
vi.mock("../util/fetchers.ts");

vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useParams: vi.fn(),
        useNavigate: vi.fn(),
    };
});

const mockNavigate = vi.fn();
const mockParams = { matchId: "1" };

const mockPlayers = [
    { name: "Winner", points: 100 },
    { name: "Second", points: 80 },
    { name: "Third", points: 60 },
    { name: "Fourth", points: 40 }
];

describe("EndGame Component", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        
        vi.mocked(reactRouterDom.useParams).mockReturnValue(mockParams);
        vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
        
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("mock-jwt");

        vi.mocked(patch).mockResolvedValue({ ok: true } as Response);
        vi.mocked(get).mockResolvedValue({
            ok: true,
            json: async () => mockPlayers
        } as Response);
    });

    it("finalizes score and fetches players on mount", async () => {
        render(<EndGame />);
        
        await waitFor(() => {
            expect(patch).toHaveBeenCalledWith("/api/v1/matches/finalscore/1", "mock-jwt");
            expect(get).toHaveBeenCalledWith("/api/v1/players/match/1", "mock-jwt");
        });
    });

    it("renders the score title and player list", async () => {
        render(<EndGame />);
        
        expect(screen.getByText(/SCORE/i)).toBeInTheDocument();
        
        await waitFor(() => {
            expect(screen.getByText(/Winner/i)).toBeInTheDocument();
            expect(screen.getByText(/100/i)).toBeInTheDocument();
            expect(screen.getByText(/Second/i)).toBeInTheDocument();
            expect(screen.getByText(/80/i)).toBeInTheDocument();
        });
    });

    it("applies correct podium styles based on rank", async () => {
        render(<EndGame />);
        
        await waitFor(() => {
            const winnerDiv = screen.getByText(/Winner/i).closest(".endgame-players");
            const secondDiv = screen.getByText(/Second/i).closest(".endgame-players");
            const thirdDiv = screen.getByText(/Third/i).closest(".endgame-players");
            const fourthDiv = screen.getByText(/Fourth/i).closest(".endgame-players");

            expect(winnerDiv).toHaveStyle({ backgroundColor: 'rgba(255, 215, 0, 0.4)' }); // Gold
            expect(secondDiv).toHaveStyle({ backgroundColor: 'rgba(101, 101, 101, 0.4)' }); // Silver
            expect(thirdDiv).toHaveStyle({ backgroundColor: 'rgba(205, 127, 50, 0.4)' }); // Bronze
            expect(fourthDiv).toHaveStyle({ backgroundColor: 'rgba(255, 255, 255, 0.4)' }); // Default
        });
    });

    it("navigates to dashboard when Exit button is clicked", async () => {
        render(<EndGame />);
        
        const exitButton = screen.getByRole("button", { name: /Exit/i });
        fireEvent.click(exitButton);
        
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
    });
});
