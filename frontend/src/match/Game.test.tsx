import { render, screen, fireEvent, waitFor, act } from "../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Game from "./Game";
import tokenService from "../services/token.service.ts";
import * as reactRouterDom from "react-router-dom";
import useFetchState from "../util/useFetchState.ts";
import WebSocketManager from "../util/webSocketManager.ts";
import * as matchUtil from "./matchUtil.tsx";
import { Phase } from "../interfaces/Phase.ts";
import { State } from "../interfaces/State.ts";

// --- Mocks ---

vi.mock("../util/useFetchState.ts");
vi.mock("../util/webSocketManager.ts");

// Reduce mocking: only mock side-effect functions that hit the API
vi.mock("./matchUtil.tsx", async () => {
    const actual = await vi.importActual<any>("./matchUtil.tsx");
    return {
        ...actual,
        handleTileClick: vi.fn(actual.handleTileClick),
        handleSalmonClick: vi.fn(actual.handleSalmonClick),
        handleRotateTile: vi.fn(), // API call
        handleGridClick: vi.fn(), // API call
        // Explicitly mock these if type issues arise with vi.mocked(...).mockReturnValue
        buildGridFromSnapshot: vi.fn(() => new Array(18).fill(null).map(() => ({ tile: null, salmons: [] }))),
        passTurn: vi.fn(() => <button data-testid="pass-turn">Pass Turn</button>),
    };
});

// Mock Chat component
vi.mock("../components/chat/Chat.tsx", () => ({ default: () => <div data-testid="chat">Chat</div> }));

const mockNavigate = vi.fn();
const mockParams = { matchId: "1" };
const mockLocation = { state: { spectatorIds: [] } };

const mockUser = { id: 10, username: "player1" };
const mockMatch = {
    id: 1,
    name: "Epic Game",
    round: 1,
    phase: Phase.TILES,
    actualPlayerId: 1,
    state: State.ON_GOING
};
const mockPlayers = [
    { id: 1, userId: 10, name: "player1", color: "RED" },
    { id: 2, userId: 11, name: "player2", color: "BLUE" }
];
const mockMatchTiles = [
    { id: 1, coordinate: null, tile: { image: "tile1.png", type: "WATER" } },
    { id: 2, coordinate: { x: 1, y: 1 }, tile: { image: "tile2.png", type: "WATER" } }
];
const mockSalmons = [
    { id: 1, playerId: 1, salmonsNumber: 5, salmon: { image: "salmon1.png" }, coordinate: null }
];

describe("Game Component", () => {
    let mockWsManager: any;

    beforeEach(() => {
        vi.clearAllMocks();
        
        vi.mocked(reactRouterDom.useParams).mockReturnValue(mockParams);
        vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
        vi.mocked(reactRouterDom.useLocation).mockReturnValue(mockLocation as any);
        
        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("mock-jwt");
        vi.mocked(tokenService.getUser).mockReturnValue(mockUser as any);

        // Robust mock for useFetchState
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            if (url?.includes("/api/v1/matchTiles/")) return [mockMatchTiles, vi.fn()];
            if (url?.includes("/api/v1/salmonMatches/")) return [mockSalmons, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        mockWsManager = {
            initConnection: vi.fn(),
            addSubscription: vi.fn(),
            deactivate: vi.fn()
        };
        vi.mocked(WebSocketManager).mockImplementation(function() { return mockWsManager; } as any);
        
        vi.mocked(matchUtil.buildGridFromSnapshot).mockReturnValue(new Array(18).fill(null).map(() => ({ tile: {} as any, salmons: [] as any[] })));
    });

    it("renders loading state initially if data is not loaded", () => {
        vi.mocked(useFetchState).mockReturnValue([[], vi.fn()]);
        render(<Game />);
        expect(screen.getByText(/Loading data/i)).toBeInTheDocument();
    });

    it("renders game information when data is loaded", async () => {
        render(<Game />);
        
        await waitFor(() => {
            expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
            expect(screen.getByText(/Round: 1/i)).toBeInTheDocument();
            expect(screen.getByText(/Phase: TILES/i)).toBeInTheDocument();
        });
        
        expect(screen.getByRole("table")).toBeInTheDocument(); // Using actual table now
        expect(screen.getByTestId("pass-turn")).toBeInTheDocument();
        expect(screen.getByTestId("chat")).toBeInTheDocument();
    });

    it("renders next tile and remaining tiles count", async () => {
        render(<Game />);
        
        await waitFor(() => {
            expect(screen.getByText(/Remaining tiles: 1/i)).toBeInTheDocument();
            expect(screen.getByText(/Next tile:/i)).toBeInTheDocument();
        });
    });

    it("calls handleTileClick when next tile is clicked", async () => {
        const { container } = render(<Game />);
        
        await waitFor(() => {
            const nextTileImg = container.querySelector(".pick-the-tile-block img");
            if (!nextTileImg) throw new Error("Next tile image not found");
            fireEvent.click(nextTileImg);
            expect(matchUtil.handleTileClick).toHaveBeenCalled();
        });
    });

    it("sets up WebSocket subscriptions on mount", async () => {
        render(<Game />);
        
        await waitFor(() => {
            expect(mockWsManager.initConnection).toHaveBeenCalled();
            expect(mockWsManager.addSubscription).toHaveBeenCalledWith("match/update/1", expect.any(Function));
            expect(mockWsManager.addSubscription).toHaveBeenCalledWith(`movingSalmon/1`, expect.any(Function));
        });
    });

    it("redirects to end game when match state becomes FINALIZED via WebSocket", async () => {
        let updateCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "match/update/1") updateCallback = cb;
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(updateCallback).toBeDefined();
        });

        updateCallback({ ...mockMatch, state: State.FINALIZED });
        
        expect(mockNavigate).toHaveBeenCalledWith("/matches/1/end");
    });

    it("renders spawning grid when round >= 6", async () => {
        const lateMatch = { ...mockMatch, round: 6 };
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [lateMatch, vi.fn()];
            return [defaultVal, vi.fn()]; // Simplified for this test
        });
        // Need to ensure players etc are also returned to avoid loading state
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [lateMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            if (url?.includes("/api/v1/matchTiles/")) return [mockMatchTiles, vi.fn()];
            if (url?.includes("/api/v1/salmonMatches/")) return [mockSalmons, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Game />);
        
        await waitFor(() => {
            // grid3 is the spawning grid
            expect(screen.getByText("5")).toBeInTheDocument(); // Spawn points
        });
    });

    it("hides interactive elements when user is a spectator", async () => {
        vi.mocked(reactRouterDom.useLocation).mockReturnValue({
            state: { spectatorIds: [mockUser.id] }
        } as any);

        const bearTile = { ...mockMatchTiles[0], tile: { type: "BEAR", image: "bear.png" } };
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matchTiles/")) return [[bearTile], vi.fn()];
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            if (url?.includes("/api/v1/salmonMatches/")) return [mockSalmons, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(screen.queryByText(/Pick the tile!/i)).not.toBeInTheDocument();
            expect(screen.queryByText(/Move your salmons!/i)).not.toBeInTheDocument();
            expect(screen.queryByRole("button", { name: /Rotate Tile/i })).not.toBeInTheDocument();
            expect(screen.queryByTestId("pass-turn")).not.toBeInTheDocument(); // passTurn usually returns a button
        });
    });

    it("shows rotate button for BEAR tiles during player turn", async () => {
        const bearTile = { ...mockMatchTiles[0], id: 999, tile: { type: "BEAR", image: "bear.png" } };
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matchTiles/")) return [[bearTile], vi.fn()];
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            if (url?.includes("/api/v1/salmonMatches/")) return [mockSalmons, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Game />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /Rotate Tile/i })).toBeInTheDocument();
        });
    });

    it("hides rotate button for WATER tiles", async () => {
        const waterTile = { ...mockMatchTiles[0], id: 888, tile: { type: "WATER", image: "water.png" } };
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matchTiles/")) return [[waterTile], vi.fn()];
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            if (url?.includes("/api/v1/salmonMatches/")) return [mockSalmons, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Game />);
        await waitFor(() => {
            expect(screen.queryByRole("button", { name: /Rotate Tile/i })).not.toBeInTheDocument();
        });
    });

    it("shows rotate button for JUMP tiles during player turn", async () => {
        const jumpTile = { ...mockMatchTiles[0], id: 777, tile: { type: "JUMP", image: "jump.png" } };
        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matchTiles/")) return [[jumpTile], vi.fn()];
            if (url?.includes("/api/v1/matches/")) return [mockMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            if (url?.includes("/api/v1/salmonMatches/")) return [mockSalmons, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        render(<Game />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /Rotate Tile/i })).toBeInTheDocument();
        });
    });

    it("handles WebSocket subscription for movingSalmonFromSea", async () => {
        let movingSeaCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "movingSalmonFromSea/1") movingSeaCallback = cb;
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(movingSeaCallback).toBeDefined();
        });

        movingSeaCallback({ salmon: mockSalmons[0], toTravel: mockMatchTiles[1] });
        // Verify it doesn't crash and triggers a re-render
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("handles WebSocket subscription for heronsUpdated", async () => {
        let heronsCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "heronsUpdated/1") heronsCallback = cb;
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(heronsCallback).toBeDefined();
        });

        heronsCallback({ matchTiles: [], salmonMatches: [], deletedSalmonIds: [1] });
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("handles WebSocket subscription for players update", async () => {
        let playersCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "players/1") playersCallback = cb;
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(playersCallback).toBeDefined();
        });

        const newPlayers = [...mockPlayers, { id: 3, userId: 12, name: "player3", color: "GREEN" }];
        playersCallback(newPlayers);
        
        // Internal state updates, we verify no crash
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("handles WebSocket subscription for salmon/spawn/enter", async () => {
        let spawnEnterCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "salmon/spawn/enter/1") spawnEnterCallback = cb;
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(spawnEnterCallback).toBeDefined();
        });

        spawnEnterCallback({ salmonMatch: mockSalmons[0], matchTile: mockMatchTiles[1] });
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("handles WebSocket subscription for herons", async () => {
        let heronsCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "herons/1") heronsCallback = cb;
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(heronsCallback).toBeDefined();
        });

        heronsCallback([{ id: 1, salmonsNumber: 3, salmon: { image: "salmon1.png" } }]);
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("renders spawning grid with salmons when round >= 6", async () => {
        const lateMatch = { ...mockMatch, round: 6 };
        const spawnedSalmons = [
            { id: 100, playerId: 1, salmonsNumber: 1, salmon: { image: "spawned.png" }, coordinate: { x: 0, y: 21 } }
        ];

        vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
            if (url?.includes("/api/v1/matches/")) return [lateMatch, vi.fn()];
            if (url?.includes("/api/v1/players/")) return [mockPlayers, vi.fn()];
            if (url?.includes("/api/v1/matchTiles/")) return [mockMatchTiles, vi.fn()];
            if (url?.includes("/api/v1/salmonMatches/")) return [spawnedSalmons, vi.fn()];
            return [defaultVal, vi.fn()];
        });

        const { container } = render(<Game />);
        
        await waitFor(() => {
            expect(screen.getByText("5")).toBeInTheDocument();
            const images = container.querySelectorAll("img");
            const spawnedImg = Array.from(images).find(img => img.getAttribute("src") === "spawned.png");
            expect(spawnedImg).toBeDefined();
        });
    });

    it("calls handleGridClick when a grid item is clicked", async () => {
        render(<Game />);
        
        await waitFor(() => {
            const gridItems = document.querySelectorAll(".grid-item");
            if (gridItems.length === 0) throw new Error("Grid items not found");
            fireEvent.click(gridItems[0]);
            expect(matchUtil.handleGridClick).toHaveBeenCalled();
        });
    });

    it("calls handleSalmonClick when a salmon image in the grid is clicked", async () => {
        // We need to mock buildGridFromSnapshot to return a cell with a salmon
        vi.mocked(matchUtil.buildGridFromSnapshot).mockReturnValue([
            {
                tile: mockMatchTiles[1],
                salmons: [mockSalmons[0]]
            },
            ...new Array(17).fill(null).map(() => ({ tile: {} as any, salmons: [] as any[] }))
        ]);

        const { container } = render(<Game />);
        
        await waitFor(() => {
            const salmonImg = container.querySelector(".salmon-img");
            if (!salmonImg) throw new Error("Salmon image not found");
            fireEvent.click(salmonImg);
            expect(matchUtil.handleSalmonClick).toHaveBeenCalled();
        });
    });

    it("handles WebSocket subscription for salmons/spawn movement", async () => {
        let spawnMoveCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "salmons/spawn/1") spawnMoveCallback = cb;
        });

        render(<Game />);
        
        await waitFor(() => {
            expect(spawnMoveCallback).toBeDefined();
        });

        spawnMoveCallback(); // Triggers the gridSpawn rotation logic
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });
});

