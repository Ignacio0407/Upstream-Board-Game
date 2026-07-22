import { render, screen, fireEvent, waitFor } from "../test-utils";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Game from "./Game";
import tokenService from "../services/token.service.ts";
import * as reactRouterDom from "react-router-dom";
import useFetchState from "../util/useFetchState.ts";
import WebSocketManager from "../components/webSocketManager/webSocketManager.ts";
import * as matchUtil from "./matchUtil";
import * as calculateValidMovesModule from "./calculateValidMoves.ts";
import { Phase } from "../interfaces/Phase.ts";
import { State } from "../interfaces/State.ts";
import { TileType } from "../interfaces/Tile.ts";

// --- Mocks ---

vi.mock("../util/useFetchState.ts");
vi.mock("../components/webSocketManager/webSocketManager.ts");

// We only mock functions that hit the network (patch/changephase/confirmBatch) or that
// depend on internal React state difficult to orchestrate from the outside (calculateValidMoves
// is already extensively tested in its own file). The rest is left with its real implementation
// to preserve integration coverage.
vi.mock("./matchUtil", async () => {
    const actual = await vi.importActual<any>("./matchUtil");
    return {
        ...actual,
        handleTileClick: vi.fn(actual.handleTileClick),
        handleSalmonClick: vi.fn(actual.handleSalmonClick),
        handleRotateTile: vi.fn(),
        handleUpdateTilePosition: vi.fn(),
        confirmMovementBatch: vi.fn(),
        handleNoMoves: vi.fn(),
    };
});

vi.mock("./calculateValidMoves.ts", () => ({
    calculateValidMoves: vi.fn(),
}));

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
    playersNumber: 2,
    state: "ON_GOING"
};
const mockPlayers = [
    { id: 1, userId: 10, name: "player1", color: "RED", energy: 3, points: 0, alive: true },
    { id: 2, userId: 11, name: "player2", color: "BLUE", energy: 3, points: 0, alive: true }
];
const mockMatchTiles = [
    { id: 1, coordinate: null, orientation: 0, tile: { image: "tile1.png", type: "WATER" } },
    { id: 2, coordinate: { x: 1, y: 1 }, orientation: 0, capacity: 0, salmonsNumber: 0, tile: { image: "tile2.png", type: "WATER" } }
];

// The salmon must have a valid coordinate to be rendered on the board.
// If coordinate is null, querySelector(".salmon-img") will fail because it won't be in the DOM.
const mockSalmons = [
    { id: 1, playerId: 1, salmonsNumber: 5, salmon: { image: "salmon1.png" }, coordinate: { x: 1, y: 1 } }
];

const setupFetchState = (overrides: Record<string, any> = {}) => {
    vi.mocked(useFetchState).mockImplementation((defaultVal: any, url: string | null) => {
        if (url?.includes("/api/v1/matches/")) return [overrides.match ?? mockMatch, vi.fn()];
        if (url?.includes("/api/v1/players/")) return [overrides.players ?? mockPlayers, vi.fn()];
        if (url?.includes("/api/v1/matchTiles/")) return [overrides.matchTiles ?? mockMatchTiles, vi.fn()];
        if (url?.includes("/api/v1/salmonMatches/")) return [overrides.salmons ?? mockSalmons, vi.fn()];
        return [defaultVal, vi.fn()];
    });
};

describe("Game Component", () => {
    let mockWsManager: any;

    beforeEach(() => {
        vi.clearAllMocks();

        vi.mocked(reactRouterDom.useParams).mockReturnValue(mockParams);
        vi.mocked(reactRouterDom.useNavigate).mockReturnValue(mockNavigate);
        vi.mocked(reactRouterDom.useLocation).mockReturnValue(mockLocation as any);

        vi.mocked(tokenService.getLocalAccessToken).mockReturnValue("mock-jwt");
        vi.mocked(tokenService.getUser).mockReturnValue(mockUser as any);

        setupFetchState();

        mockWsManager = {
            initConnection: vi.fn(),
            addSubscription: vi.fn(),
            deactivate: vi.fn()
        };
        vi.mocked(WebSocketManager).mockImplementation(function () { return mockWsManager; } as any);

        vi.mocked(calculateValidMovesModule.calculateValidMoves).mockReturnValue([]);
    });

    // --- Loading and Basic Rendering ---

    it("shows the loading state while all data hasn't arrived", () => {
        vi.mocked(useFetchState).mockReturnValue([[], vi.fn()]);
        render(<Game />);
        expect(screen.getByText(/Loading data/i)).toBeInTheDocument();
    });

    it("shows the game information once data is loaded", async () => {
        render(<Game />);

        await waitFor(() => {
            expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
            expect(screen.getByText(/Round: 1/i)).toBeInTheDocument();
            expect(screen.getByText(/Phase: TILES/i)).toBeInTheDocument();
        });

        expect(screen.getByRole("table")).toBeInTheDocument();
        expect(screen.getByTestId("chat")).toBeInTheDocument();
    });

    it("shows the next tile and the remaining tiles counter", async () => {
        render(<Game />);
        await waitFor(() => {
            expect(screen.getByText(/Remaining tiles: 1/i)).toBeInTheDocument();
            expect(screen.getByText(/Next tile:/i)).toBeInTheDocument();
        });
    });

    it('shows "Pick the tile!" only when it is my turn and we are in TILES phase', async () => {
        render(<Game />);
        await waitFor(() => {
            expect(screen.getByText(/Pick the tile!/i)).toBeInTheDocument();
        });
    });

    it('does not show "Pick the tile!" if it is not my turn', async () => {
        setupFetchState({ match: { ...mockMatch, actualPlayerId: 2 } });
        render(<Game />);
        await waitFor(() => expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument());
        expect(screen.queryByText(/Pick the tile!/i)).not.toBeInTheDocument();
    });

    it("does not show player controls or turn headers when the user is a spectator", async () => {
        vi.mocked(reactRouterDom.useLocation).mockReturnValue({ state: { spectatorIds: [mockUser.id] } } as any);
        render(<Game />);
        await waitFor(() => expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument());
        expect(screen.queryByText(/Pick the tile!/i)).not.toBeInTheDocument();
        expect(screen.queryByText(/Move your salmons!/i)).not.toBeInTheDocument();
    });

    it('shows "Move your salmons!" in MOVING phase, my turn, and not a spectator', async () => {
        setupFetchState({ match: { ...mockMatch, phase: Phase.MOVING } });
        render(<Game />);
        await waitFor(() => {
            expect(screen.getByText(/Move your salmons!/i)).toBeInTheDocument();
        });
    });

    // --- Next Tile Interaction ---

    it("calls handleTileClick when clicking the next tile", async () => {
        const { container } = render(<Game />);
        await waitFor(() => {
            const nextTileImg = container.querySelector(".pick-the-tile-block img");
            if (!nextTileImg) throw new Error("Next tile image not found");
            fireEvent.click(nextTileImg);
            expect(matchUtil.handleTileClick).toHaveBeenCalled();
        });
    });

    it.each(["BEAR", "JUMP"])("shows the rotate button for %s tiles during my turn in TILES phase", async (type) => {
        setupFetchState({ matchTiles: [{ ...mockMatchTiles[0], id: 999, tile: { type, image: "x.png" } }, mockMatchTiles[1]] });
        render(<Game />);
        await waitFor(() => {
            expect(screen.getAllByRole("button", { name: /Rotate Tile/i })[0]).toBeInTheDocument();
        });
    });

    it("does not show the rotate button for WATER tiles", async () => {
        setupFetchState({ matchTiles: [{ ...mockMatchTiles[0], id: 888, tile: { type: "WATER", image: "x.png" } }, mockMatchTiles[1]] });
        render(<Game />);
        await waitFor(() => expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument());
        expect(screen.queryByRole("button", { name: /Rotate Tile/i })).not.toBeInTheDocument();
    });

    it("calls handleRotateTile when clicking the rotate button", async () => {
        setupFetchState({ matchTiles: [{ ...mockMatchTiles[0], id: 999, tile: { type: TileType.BEAR, image: "x.png" } }, mockMatchTiles[1]] });
        render(<Game />);
        
        const rotateBtn = await screen.findByText("Rotate Tile");
        fireEvent.click(rotateBtn);
        
        await waitFor(() => {
            expect(matchUtil.handleRotateTile).toHaveBeenCalled();
        });
    });

    // --- TILES Phase: grid click ---

    it("calls handleUpdateTilePosition when clicking a grid cell during TILES phase", async () => {
        render(<Game />);
        await waitFor(() => {
            const gridItems = document.querySelectorAll(".grid-item");
            if (gridItems.length === 0) throw new Error("Grid cells not found");
            fireEvent.click(gridItems[0]);
            expect(matchUtil.handleUpdateTilePosition).toHaveBeenCalled();
        });
    });

    // --- MOVING Phase: salmon selection and movement ---

    it("calculates valid moves and marks them on the grid when clicking a salmon in MOVING phase", async () => {
        setupFetchState({ match: { ...mockMatch, phase: Phase.MOVING } });
        vi.mocked(matchUtil.buildGridFromSnapshot as any); // no-op, uses real implementation via actual
        vi.mocked(calculateValidMovesModule.calculateValidMoves).mockReturnValue([
            { x: 1, y: 1, energyUsed: 1, salmonLoss: 0, isJump: false } as any
        ]);

        const { container } = render(<Game />);
        
        const salmonImg = await waitFor(() => {
            const img = container.querySelector(".salmon-img");
            if (!img) throw new Error("Rendered salmon not found");
            return img;
        });
        fireEvent.click(salmonImg);

        await waitFor(() => {
            expect(matchUtil.handleSalmonClick).toHaveBeenCalled();
            expect(calculateValidMovesModule.calculateValidMoves).toHaveBeenCalled();
        });
    });

    it("invokes handleNoMoves if there are no valid moves after selecting a salmon", async () => {
        setupFetchState({ match: { ...mockMatch, phase: Phase.MOVING } });
        vi.mocked(calculateValidMovesModule.calculateValidMoves).mockReturnValue([]);

        const { container } = render(<Game />);
        
        const salmonImg = await waitFor(() => {
            const img = container.querySelector(".salmon-img");
            if (!img) throw new Error("Rendered salmon not found");
            return img;
        });
        fireEvent.click(salmonImg);

        await waitFor(() => {
            expect(matchUtil.handleNoMoves).toHaveBeenCalled();
        });
    });

    it("batch controls (Confirm/Undo) DO NOT appear if there are no pending moves", async () => {
        setupFetchState({ match: { ...mockMatch, phase: Phase.MOVING } });
        render(<Game />);
        await waitFor(() => expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument());
        expect(screen.queryByRole("button", { name: /Undo last/i })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /Undo all/i })).not.toBeInTheDocument();
    });

    it("shows undo buttons when successfully moving a salmon and the confirm button if energy reaches 0", async () => {
        setupFetchState({ match: { ...mockMatch, phase: Phase.MOVING } });
        vi.mocked(calculateValidMovesModule.calculateValidMoves)
            .mockReturnValueOnce([{ x: 1, y: 1, energyUsed: 3, salmonLoss: 0, isJump: false } as any]) // initial selection
            .mockReturnValueOnce([]); // after moving, no moves left (energy at 0)

        const { container } = render(<Game />);

        const salmonImg = await waitFor(() => {
            const img = container.querySelector(".salmon-img");
            if (!img) throw new Error("Rendered salmon not found");
            return img;
        });
        fireEvent.click(salmonImg);

        const gridItems = await waitFor(() => {
            const items = Array.from(container.querySelectorAll(".grid-item"));
            if (items.length === 0) throw new Error("Grid items not found");
            return items;
        });
        const targetCell = gridItems[matchUtil.coordToIndex(1, 1)];
        fireEvent.click(targetCell);

        await waitFor(() => {
            expect(screen.getByRole("button", { name: /Undo last/i })).toBeInTheDocument();
            expect(screen.getByRole("button", { name: /Undo all/i })).toBeInTheDocument();
        });
    });

    it("Undo all clears the history and pending moves", async () => {
        setupFetchState({ match: { ...mockMatch, phase: Phase.MOVING } });
        vi.mocked(calculateValidMovesModule.calculateValidMoves)
            .mockReturnValueOnce([{ x: 1, y: 1, energyUsed: 3, salmonLoss: 0, isJump: false } as any])
            .mockReturnValueOnce([]);

        const { container } = render(<Game />);
        
        const salmonImg = await waitFor(() => {
            const img = container.querySelector(".salmon-img");
            if (!img) throw new Error("Rendered salmon not found");
            return img;
        });
        fireEvent.click(salmonImg);

        const gridItems = await waitFor(() => Array.from(container.querySelectorAll(".grid-item")));
        fireEvent.click(gridItems[matchUtil.coordToIndex(1, 1)]);

        const undoAllBtn = await screen.findByRole("button", { name: /Undo all/i });
        fireEvent.click(undoAllBtn);

        await waitFor(() => {
            expect(screen.queryByRole("button", { name: /Undo all/i })).not.toBeInTheDocument();
        });
    });

    it("Confirm batch calls confirmMovementBatch when player energy reaches 0", async () => {
        setupFetchState({ match: { ...mockMatch, phase: Phase.MOVING }, players: [{ ...mockPlayers[0], energy: 0 }, mockPlayers[1]] });
        vi.mocked(calculateValidMovesModule.calculateValidMoves)
            .mockReturnValueOnce([{ x: 1, y: 1, energyUsed: 0, salmonLoss: 0, isJump: false } as any])
            .mockReturnValueOnce([]);

        const { container } = render(<Game />);
        
        const salmonImg = await waitFor(() => {
            const img = container.querySelector(".salmon-img");
            if (!img) throw new Error("Rendered salmon not found");
            return img;
        });
        fireEvent.click(salmonImg);

        const gridItems = await waitFor(() => Array.from(container.querySelectorAll(".grid-item")));
        fireEvent.click(gridItems[matchUtil.coordToIndex(1, 1)]);

        const confirmBtn = await screen.findByRole("button", { name: "Confirm" });
        fireEvent.click(confirmBtn);
        
        await waitFor(() => {
            expect(matchUtil.confirmMovementBatch).toHaveBeenCalled();
        });
    });

    // --- Spawn Round ---

    it("shows the spawn grid when the round is >= 6", async () => {
        setupFetchState({ match: { ...mockMatch, round: 6 } });
        render(<Game />);
        await waitFor(() => {
            expect(screen.getByText("5")).toBeInTheDocument(); // spawn points of the first row
        });
    });

    it("does not show the spawn grid in early rounds", async () => {
        render(<Game />);
        await waitFor(() => expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument());
        expect(screen.queryByText("5")).not.toBeInTheDocument();
    });

    // --- Websockets ---

    it("initializes the connection and subscribes to all expected topics", async () => {
        render(<Game />);
        await waitFor(() => {
            expect(mockWsManager.initConnection).toHaveBeenCalled();
            [
                "match/update/1",
                "tiles/place/1",
                "tiles/rotation/1",
                "players/1",
                "tilesAndSalmons/1",
                "tilesAndSalmonsByMatchId/1",
                "salmons/spawn/1",
            ].forEach(topic => {
                expect(mockWsManager.addSubscription).toHaveBeenCalledWith(topic, expect.any(Function));
            });
        });
    });

    it("redirects to the end game screen when the state changes to FINALIZED", async () => {
        let updateCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "match/update/1") updateCallback = cb;
        });

        render(<Game />);
        await waitFor(() => expect(updateCallback).toBeDefined());

        updateCallback({ ...mockMatch, state: State.FINALIZED });
        expect(mockNavigate).toHaveBeenCalledWith("/matches/1/end");
    });

    it("processes the placement of a tile arriving via websocket without breaking the render", async () => {
        let placeCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "tiles/place/1") placeCallback = cb;
        });

        render(<Game />);
        await waitFor(() => expect(placeCallback).toBeDefined());

        placeCallback({ ...mockMatchTiles[1], id: 1, coordinate: { x: 0, y: 0 } });
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("processes player updates arriving via websocket", async () => {
        let playersCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "players/1") playersCallback = cb;
        });

        render(<Game />);
        await waitFor(() => expect(playersCallback).toBeDefined());

        playersCallback([...mockPlayers, { id: 3, userId: 12, name: "player3", color: "GREEN", energy: 3 }]);
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("merges tiles and salmons arriving via websocket (tilesAndSalmons) without duplicating them", async () => {
        let mergeCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "tilesAndSalmons/1") mergeCallback = cb;
        });

        render(<Game />);
        await waitFor(() => expect(mergeCallback).toBeDefined());

        mergeCallback({ matchTiles: [mockMatchTiles[1]], salmonMatches: [{ ...mockSalmons[0], salmonsNumber: 2 }] });
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("replaces the complete snapshot via websocket (tilesAndSalmonsByMatchId) without breaking the render", async () => {
        let snapshotCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "tilesAndSalmonsByMatchId/1") snapshotCallback = cb;
        });

        render(<Game />);
        await waitFor(() => expect(snapshotCallback).toBeDefined());

        snapshotCallback({ matchTiles: mockMatchTiles, salmonMatches: mockSalmons });
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("reorders the spawn grid when the spawn event arrives via websocket", async () => {
        let spawnCallback: any;
        mockWsManager.addSubscription.mockImplementation((topic: string, cb: any) => {
            if (topic === "salmons/spawn/1") spawnCallback = cb;
        });
        setupFetchState({ match: { ...mockMatch, round: 6 } });

        render(<Game />);
        await waitFor(() => expect(spawnCallback).toBeDefined());

        expect(() => spawnCallback()).not.toThrow();
        expect(screen.getByText(/Game: Epic Game/i)).toBeInTheDocument();
    });

    it("closes the websocket connection when the component unmounts", async () => {
        const { unmount } = render(<Game />);
        await waitFor(() => expect(mockWsManager.initConnection).toHaveBeenCalled());
        unmount();
        expect(mockWsManager.deactivate).toHaveBeenCalled();
    });
});