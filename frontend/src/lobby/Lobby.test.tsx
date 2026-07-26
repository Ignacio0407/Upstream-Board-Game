import { render, screen, fireEvent } from "../test-utils.tsx";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Lobby from "./Lobby.tsx";
import tokenService from "../services/token.service.ts";
import * as reactRouterDom from "react-router-dom";
import startGame from "./startGame.ts";
import endGame from "../util/endGame.ts";
import WebSocketManager from "../components/webSocketManager/webSocketManager.ts";
import { State } from "../interfaces/State.ts";
import useFetchState from "../util/useFetchState.ts";
import { emptyUser } from "../interfaces/User.ts";

// --- Mocks ---

vi.mock("../services/token.service.ts");
vi.mock("../util/useFetchState.ts");
vi.mock("./startGame.ts");
vi.mock("../util/endGame.ts");
const mockWsManager = {
    initConnection: vi.fn(),
    addSubscription: vi.fn(),
    deactivate: vi.fn()
};

vi.mock("../components/webSocketManager/webSocketManager.ts", () => ({
    default: vi.fn().mockImplementation(function() {
        return mockWsManager;
    })
}));
vi.mock("./ColorHandler.tsx", () => ({
    default: ({ onColorChanged }: any) => (
        <div data-testid="color-handler">
            <button onClick={onColorChanged}>Select Color</button>
        </div>
    )
}));

vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useParams: vi.fn(),
        useNavigate: vi.fn(),
        useLocation: vi.fn(),
    };
});

const mockNavigate = vi.fn();
const mockParams = { matchId: "1" };
const mockLocation = { state: { spectatorIds: [] } };

const mockMatch = {
    id: 1,
    name: "Test Match",
    password: "pwd",
    playersNumber: 2,
    matchCreatorId: 10,
    state: State.WAITING
};

const mockUser = { ...emptyUser, id: 10, username: "creator" };
const mockFinalUser = { id: 10, username: "creator" };
const mockPlayers = [
    { id: 1, userId: 10, name: "creator", color: "RED" },
    { id: 2, userId: 11, name: "player2", color: "BLUE" }
];

describe("Lobby Component", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        
        vi.mocked(reactRouterDom.useParams).mockReturnValue(mockParams);
        vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
        vi.mocked(reactRouterDom.useLocation).mockReturnValue(mockLocation as any);
        
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("mock-jwt");
        vi.mocked(tokenService.getUser).mockReturnValue(mockUser);

        // Robust mock for useFetchState based on URL
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/users/")) return [mockFinalUser, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        vi.mocked(WebSocketManager).mockImplementation(function() {
            return mockWsManager as any;
        });
    });

    it("renders match information correctly", () => {
        render(<Lobby />);
        expect(screen.getByText(/Room name: Test Match/i)).toBeInTheDocument();
        expect(screen.getByText(/Password: pwd/i)).toBeInTheDocument();
        expect(screen.getByText(/Players number: 2/i)).toBeInTheDocument();
    });

    it("renders the list of players", () => {
        render(<Lobby />);
        expect(screen.getByText("creator")).toBeInTheDocument();
        expect(screen.getByText("player2")).toBeInTheDocument();
    });

    it("shows ColorHandler if the user is not in the player list and not a spectator", () => {
        // Mock user not in players
        const outsider = { ...emptyUser, id: 99, username: "outsider" };
        vi.mocked(tokenService.getUser).mockReturnValue(outsider);
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/users/")) return [outsider, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Lobby />);
        expect(screen.getByTestId("color-handler")).toBeInTheDocument();
    });

    it("hides ColorHandler once a color is selected", async () => {
        const outsider = { ...emptyUser, id: 99, username: "outsider" };
        vi.mocked(tokenService.getUser).mockReturnValue(outsider);
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/users/")) return [outsider, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Lobby />);
        const selectButton = screen.getByText("Select Color");
        fireEvent.click(selectButton);
        expect(screen.queryByTestId("color-handler")).not.toBeInTheDocument();
    });

    it("shows 'Start' button only for the creator with enough players", () => {
        // User is creator (id 10)
        render(<Lobby />);
        expect(screen.getAllByRole("button", { name: /Start/i })[0]).toBeInTheDocument();
    });

    it("hides 'Start' button if the user is not the creator", () => {
        vi.mocked(tokenService.getUser).mockReturnValue({ ...emptyUser, id: 11, username: "player2" });
        render(<Lobby />);
        expect(screen.queryByRole("button", { name: /Start/i })).not.toBeInTheDocument();
    });

    it("calls startGame when 'Start' button is clicked", async () => {
        render(<Lobby />);
        const startButton = screen.getByText(/Start/i);
        fireEvent.click(startButton);
        expect(startGame).toHaveBeenCalled();
    });

    it("calls endGame when 'Exit' button is clicked", () => {
        render(<Lobby />);
        const exitButton = screen.getByText(/Exit/i);
        fireEvent.click(exitButton);
        expect(endGame).toHaveBeenCalled();
    });

    it("redirects to dashboard if match is finalized", () => {
    const finalizedMatch = { ...mockMatch, state: State.FINALIZED };
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [finalizedMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Lobby />);
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
    });

    it("sets up WebSocket subscriptions on mount", () => {
        render(<Lobby />);
        expect(mockWsManager.initConnection).toHaveBeenCalled();
        expect(mockWsManager.addSubscription).toHaveBeenCalledWith("match/update/1", expect.any(Function));
        expect(mockWsManager.addSubscription).toHaveBeenCalledWith("match/newPlayer/1", expect.any(Function));
        expect(mockWsManager.addSubscription).toHaveBeenCalledWith("match/start/1", expect.any(Function));
    });

    it("deactivates WebSocket on unmount", () => {
        const { unmount } = render(<Lobby />);
        unmount();
        expect(mockWsManager.deactivate).toHaveBeenCalled();
    });

    it("triggers websocket callbacks for match update, new player, and start", () => {
        render(<Lobby />);

        // Find subscriptions registered with mockWsManager.addSubscription
        const calls = mockWsManager.addSubscription.mock.calls;
        const updateCb = calls.find(c => c[0] === "match/update/1")?.[1];
        const newPlayerCb = calls.find(c => c[0] === "match/newPlayer/1")?.[1];
        const startCb = calls.find(c => c[0] === "match/start/1")?.[1];

        expect(updateCb).toBeDefined();
        expect(newPlayerCb).toBeDefined();
        expect(startCb).toBeDefined();

        updateCb({ ...mockMatch, name: "Updated Room" });
        newPlayerCb({ id: 3, userId: 12, name: "player3", color: "GREEN" });
        startCb();

        expect(mockNavigate).toHaveBeenCalledWith("/matches/1/game");
    });

    it("triggers startGame when Enter key is pressed on start container", () => {
        render(<Lobby />);
        const container = screen.getByRole("button", { name: /Start Exit/i });
        fireEvent.keyDown(container, { key: "Enter" });
        expect(startGame).toHaveBeenCalled();
    });
});

