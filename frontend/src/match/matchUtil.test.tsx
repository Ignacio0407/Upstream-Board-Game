import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as matchUtil from './matchUtil';
import { Phase } from '../interfaces/Phase';
import { emptySimpleTile, TileType } from '../interfaces/Tile';
import { emptyTile } from '../interfaces/MatchTile';
import * as fetchers from '../util/fetchers';

// Mock fetchers
vi.mock('../util/fetchers', () => ({
    get: vi.fn(),
    patch: vi.fn(),
    put: vi.fn(),
}));

// Mock ColorParser
vi.mock('../util/ColorParser', () => ({
    ColorToRgb: vi.fn((color) => `rgb-${color}`),
}));

describe('matchUtil', () => {

    beforeEach(() => {
        vi.clearAllMocks();
        globalThis.alert = vi.fn();
        globalThis.confirm = vi.fn();
    });

    describe('coordToIndex', () => {
        it('calculates index correctly', () => {
            expect(matchUtil.coordToIndex(0, 5)).toBe(0);
            expect(matchUtil.coordToIndex(2, 5)).toBe(2);
            expect(matchUtil.coordToIndex(0, 0)).toBe(15);
            expect(matchUtil.coordToIndex(2, 0)).toBe(17);
            expect(matchUtil.coordToIndex(1, 3)).toBe(7);
        });
    });

    describe('isFull', () => {
        const mockTile = { ...emptyTile, capacity: 2, salmonsNumber: 2, tile: { type: TileType.WATER } };

        it('returns false if capacity is 0', () => {
            const tile = { ...mockTile, capacity: 0 };
            expect(matchUtil.isFull(2, tile as any)).toBe(false);
        });

        it('returns true if salmonsNumber equals capacity for WATER tile', () => {
            expect(matchUtil.isFull(2, mockTile as any)).toBe(true);
        });

        it('returns true for ROCK tile with > 2 players and capacity == salmonsNumber - 1', () => {
            const rockTile = { ...mockTile, tile: { type: TileType.ROCK }, capacity: 1, salmonsNumber: 2 };
            expect(matchUtil.isFull(3, rockTile as any)).toBe(true);
        });

        it('returns false if not full', () => {
            const tile = { ...mockTile, salmonsNumber: 1 };
            expect(matchUtil.isFull(2, tile as any)).toBe(false);
        });
    });

    describe('handleTileClick', () => {
        const setSelectedTile = vi.fn();
        const setSelectedSalmon = vi.fn();
        const mockPlayer = { id: 1 };
        const mockMatch = { actualPlayerId: 1, phase: Phase.TILES };
        const mockTile = { id: 100 };

        it('sets selected tile when it is player turn and tile phase', () => {
            matchUtil.handleTileClick(mockTile as any, mockPlayer as any, mockMatch as any, setSelectedTile, setSelectedSalmon);
            expect(setSelectedTile).toHaveBeenCalledWith(mockTile);
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });

        it('does nothing if not player turn', () => {
            const otherMatch = { ...mockMatch, actualPlayerId: 2 };
            matchUtil.handleTileClick(mockTile as any, mockPlayer as any, otherMatch as any, setSelectedTile, setSelectedSalmon);
            expect(setSelectedTile).not.toHaveBeenCalled();
        });

        it('does nothing if not tile phase', () => {
            const otherMatch = { ...mockMatch, phase: Phase.MOVING };
            matchUtil.handleTileClick(mockTile as any, mockPlayer as any, otherMatch as any, setSelectedTile, setSelectedSalmon);
            expect(setSelectedTile).not.toHaveBeenCalled();
        });
    });

    describe('handleSalmonClick', () => {
        const setSelectedSalmon = vi.fn();
        const mockPlayer = { id: 1 };
        const mockMatch = { actualPlayerId: 1, phase: Phase.MOVING };
        const mockSalmon = { id: 50, playerId: 1 };

        it('sets selected salmon when it is player turn and moving phase and salmon belongs to player', () => {
            matchUtil.handleSalmonClick(mockSalmon as any, mockPlayer as any, mockMatch as any, setSelectedSalmon);
            expect(setSelectedSalmon).toHaveBeenCalledWith(mockSalmon);
        });

        it('does nothing if salmon does not belong to player', () => {
            const otherSalmon = { ...mockSalmon, playerId: 2 };
            matchUtil.handleSalmonClick(otherSalmon as any, mockPlayer as any, mockMatch as any, setSelectedSalmon);
            expect(setSelectedSalmon).not.toHaveBeenCalled();
        });
    });

    describe('updateSalmonPosition', () => {
        const mockSalmon = { id: 1 };
        const mockMatch = { phase: Phase.MOVING };
        const jwt = 'jwt';

        it('calls patch when it is moving phase and player turn', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.updateSalmonPosition(mockSalmon as any, 1, 1, jwt, mockMatch as any, 1, 1);
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/salmonMatches/coordinate/1', jwt, { x: 1, y: 1 });
        });

        it('alerts if not moving phase or not player turn', async () => {
            const otherMatch = { phase: Phase.TILES };
            await matchUtil.updateSalmonPosition(mockSalmon as any, 1, 1, jwt, otherMatch as any, 1, 1);
            expect(globalThis.alert).toHaveBeenCalledWith("Now it's tiles placing phase!");
            
            vi.clearAllMocks();
            await matchUtil.updateSalmonPosition(mockSalmon as any, 1, 1, jwt, mockMatch as any, 1, 2);
            expect(globalThis.alert).toHaveBeenCalledWith("Now it's tiles placing phase!");
        });

        it('alerts error message if response is not ok and no specific error provided', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ message: 'General Error' }) } as any);
            await matchUtil.updateSalmonPosition(mockSalmon as any, 1, 1, jwt, mockMatch as any, 1, 1);
            expect(globalThis.alert).toHaveBeenCalledWith('General Error');
        });

        it('alerts error if response has specific error field', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ error: 'Detailed Error' }) } as any);
            await matchUtil.updateSalmonPosition(mockSalmon as any, 1, 1, jwt, mockMatch as any, 1, 1);
            expect(globalThis.alert).toHaveBeenCalledWith('Detailed Error');
        });

        it('throws error on catch block', async () => {
            vi.mocked(fetchers.patch).mockRejectedValue(new Error('Network Fail'));
            await expect(matchUtil.updateSalmonPosition(mockSalmon as any, 1, 1, jwt, mockMatch as any, 1, 1)).rejects.toBe('Network Fail');
        });
    });

    describe('changephase', () => {
        it('calls patch to change phase', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.changephase({ id: 1 } as any, 'jwt', 1);
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matches/1/changephase/1', 'jwt');
        });

        it('alerts error on failure', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ error: 'Phase Error' }) } as any);
            await matchUtil.changephase({ id: 1 } as any, 'jwt', 1);
            expect(globalThis.alert).toHaveBeenCalledWith('Phase Error');
        });

        it('throws on catch', async () => {
            vi.mocked(fetchers.patch).mockRejectedValue(new Error('Catch Error'));
            await expect(matchUtil.changephase({ id: 1 } as any, 'jwt', 1)).rejects.toBe('Catch Error');
        });
    });

    describe('enterSpawn', () => {
        it('calls patch to enter spawn', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.enterSpawn({ id: 5 } as any, 'jwt');
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/salmonMatches/enterSpawn/5', 'jwt');
        });

        it('alerts error on failure', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ error: 'Spawn Error' }) } as any);
            await matchUtil.enterSpawn({ id: 5 } as any, 'jwt');
            expect(globalThis.alert).toHaveBeenCalledWith('Spawn Error');
        });

        it('throws on catch', async () => {
            vi.mocked(fetchers.patch).mockRejectedValue(new Error('Spawn Catch'));
            await expect(matchUtil.enterSpawn({ id: 5 } as any, 'jwt')).rejects.toBe('Spawn Catch');
        });
    });

    describe('updateTilePosition', () => {
        const mockTile = { id: 1 };
        const mockMatch = { phase: Phase.TILES };

        it('calls patch to update tile position', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.updateTilePosition(mockTile as any, 1, 1, 'jwt', 1, 1, mockMatch as any);
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matchTiles/1', 'jwt', { actualPlayerId: 1, myPlayerId: 1, x: 1, y: 1 });
        });

        it('alerts if not tiles phase', async () => {
            const otherMatch = { phase: Phase.MOVING };
            await matchUtil.updateTilePosition(mockTile as any, 1, 1, 'jwt', 1, 1, otherMatch as any);
            expect(globalThis.alert).toHaveBeenCalledWith("Now it's salmon moving phase!");
        });

        it('alerts and throws on failure', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ message: 'Tile Error' }) } as any);
            await matchUtil.updateTilePosition(mockTile as any, 1, 1, 'jwt', 1, 1, mockMatch as any);
            expect(globalThis.alert).toHaveBeenCalledWith('Tile Error');
        });

        it('logs and rethrows on catch', async () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
            vi.mocked(fetchers.patch).mockRejectedValue(new Error('Tile Catch'));
            await expect(matchUtil.updateTilePosition(mockTile as any, 1, 1, 'jwt', 1, 1, mockMatch as any))
                .rejects.toThrow('Tile Catch');
            expect(consoleSpy).toHaveBeenCalled();
            consoleSpy.mockRestore();
        });
    });

    describe('handleGridClick', () => {
        const setSelectedTile = vi.fn();
        const setSelectedSalmon = vi.fn();
        const mockMatch = { id: 1, phase: Phase.TILES, playersNumber: 2, round: 1 };
        const grid = new Array(18).fill(null).map(() => ({ tile: emptyTile, salmons: [] }));

        it('handles tile placement', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            const selectedTile = { id: 1 };
            await matchUtil.handleGridClick('jwt', 0, null, setSelectedTile, selectedTile as any, setSelectedSalmon, mockMatch as any, 1, 1, grid);
            expect(setSelectedTile).toHaveBeenCalledWith(null);
            expect(fetchers.patch).toHaveBeenCalled();
        });

        it('handles spawning entrance when round >= 6', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            const selectedSalmon = { id: 1 };
            const lateMatch = { ...mockMatch, phase: Phase.MOVING, round: 6 };
            const index = matchUtil.coordToIndex(1, 5); // x=1, y=5
            
            await matchUtil.handleGridClick('jwt', index, selectedSalmon as any, setSelectedTile, null, setSelectedSalmon, lateMatch as any, 1, 1, grid);
            
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/salmonMatches/enterSpawn/1', 'jwt');
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });

        it('handles normal movement when not full and not spawning', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            const selectedSalmon = { id: 1 };
            const movingMatch = { ...mockMatch, phase: Phase.MOVING };
            
            await matchUtil.handleGridClick('jwt', 0, selectedSalmon as any, setSelectedTile, null, setSelectedSalmon, movingMatch as any, 1, 1, grid);
            
            expect(fetchers.patch).toHaveBeenCalled();
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });

        it('handles jumping when tile is full', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            vi.mocked(globalThis.confirm).mockReturnValue(true);
            const selectedSalmon = { id: 1 };
            const movingMatch = { ...mockMatch, phase: Phase.MOVING };
            const fullGrid = [...grid];
            fullGrid[0] = { tile: { ...emptyTile, capacity: 1, salmonsNumber: 1, tile: { ...emptySimpleTile, type: TileType.WATER } }, salmons: [] };
            
            await matchUtil.handleGridClick('jwt', 0, selectedSalmon as any, setSelectedTile, null, setSelectedSalmon, movingMatch as any, 1, 1, fullGrid);
            expect(globalThis.confirm).toHaveBeenCalled();
            expect(fetchers.patch).toHaveBeenCalled();
        });

        it('does nothing when selectedSalmon is null and no tile is selected', async () => {
            await matchUtil.handleGridClick('jwt', 0, null, setSelectedTile, null, setSelectedSalmon, mockMatch as any, 1, 1, grid);
            expect(fetchers.patch).not.toHaveBeenCalled();
        });

        it('does nothing when jumping is cancelled by user', async () => {
            vi.mocked(globalThis.confirm).mockReturnValue(false);
            const selectedSalmon = { id: 1 };
            const movingMatch = { ...mockMatch, phase: Phase.MOVING };
            const fullGrid = [...grid];
            fullGrid[0] = { tile: { ...emptyTile, capacity: 1, salmonsNumber: 1, tile: { ...emptySimpleTile, type: TileType.WATER } }, salmons: [] };
            
            await matchUtil.handleGridClick('jwt', 0, selectedSalmon as any, setSelectedTile, null, setSelectedSalmon, movingMatch as any, 1, 1, fullGrid);
            expect(globalThis.confirm).toHaveBeenCalled();
            expect(fetchers.patch).not.toHaveBeenCalled();
        });

        it('calls changephase when salmon movement is successful in moving phase', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            const selectedSalmon = { id: 1 };
            const movingMatch = { ...mockMatch, phase: Phase.MOVING };
            
            await matchUtil.handleGridClick('jwt', 0, selectedSalmon as any, setSelectedTile, null, setSelectedSalmon, movingMatch as any, 1, 1, grid);
            
            expect(fetchers.patch).toHaveBeenCalledTimes(2); // One for updateSalmonPosition, one for changephase
        });

        it('calls changephase when spawn entrance is successful in moving phase', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            const selectedSalmon = { id: 1 };
            const lateMatch = { ...mockMatch, phase: Phase.MOVING, round: 6 };
            const index = matchUtil.coordToIndex(1, 5);
            
            await matchUtil.handleGridClick('jwt', index, selectedSalmon as any, setSelectedTile, null, setSelectedSalmon, lateMatch as any, 1, 1, grid);
            
            expect(fetchers.patch).toHaveBeenCalledTimes(2); // One for enterSpawn, one for changephase
        });

        it('does not call patch or changephase if tile position is clicked but phase is not TILES', async () => {
            const selectedTile = { id: 1 };
            const movingMatch = { ...mockMatch, phase: Phase.MOVING };
            await matchUtil.handleGridClick('jwt', 0, null, setSelectedTile, selectedTile as any, setSelectedSalmon, movingMatch as any, 1, 1, grid);
            expect(fetchers.patch).not.toHaveBeenCalled();
            expect(globalThis.alert).toHaveBeenCalledWith("Now it's salmon moving phase!");
        });
    });

    describe('handleRotateTile', () => {
        it('calls patch to rotate tile', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.handleRotateTile({ id: 1, orientation: 0 } as any, 'jwt');
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matchTiles/1/rotation', 'jwt', 1);
        });

        it('logs internal error on failure (response not ok)', async () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false } as any);
            await matchUtil.handleRotateTile({ id: 1, orientation: 0 } as any, 'jwt');
            expect(consoleSpy).toHaveBeenCalledWith('Error rotating tile:', expect.any(Error));
            consoleSpy.mockRestore();
        });

        it('logs error on catch (network error)', async () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
            vi.mocked(fetchers.patch).mockRejectedValue(new Error('Rotate Catch'));
            await matchUtil.handleRotateTile({ id: 1, orientation: 0 } as any, 'jwt');
            expect(consoleSpy).toHaveBeenCalledWith('Error rotating tile:', expect.any(Error));
            consoleSpy.mockRestore();
        });
    });

    describe('getRotationStyle', () => {
        it('returns correct transform style', () => {
            const tile = { ...emptyTile, orientation: 2 };
            expect(matchUtil.getRotationStyle(tile as any)).toEqual({ transform: 'rotate(120deg)' });
        });

        it('returns empty object if tile is null', () => {
            expect(matchUtil.getRotationStyle(null as any)).toEqual({});
        });
    });

    describe('calculateSalmonPosition', () => {
        it('returns positions for 1, 2, 3, 4, 5 salmons', () => {
            [1, 2, 3, 4, 5].forEach(num => {
                const pos = matchUtil.calculateSalmonPosition(0, num);
                expect(pos.left).toBeDefined();
                expect(pos.top).toBeDefined();
            });
        });

        it('returns default for unknown amount', () => {
            const pos = matchUtil.calculateSalmonPosition(0, 10);
            expect(pos.left).toBe('85px');
        });
    });

    describe('buildGridFromSnapshot', () => {
        it('builds a grid correctly', () => {
            const tiles = [{ ...emptyTile, coordinate: { x: 1, y: 1 } }];
            const salmons = [{ id: 1, coordinate: { x: 1, y: 1 } }];
            const grid = matchUtil.buildGridFromSnapshot(tiles as any, salmons as any);
            const index = matchUtil.coordToIndex(1, 1);
            expect(grid[index].tile).toBeDefined();
            expect(grid[index].salmons).toHaveLength(1);
        });

        it('skips tiles with missing coordinates', () => {
            const tiles = [{ ...emptyTile, coordinate: null }];
            const grid = matchUtil.buildGridFromSnapshot(tiles as any, []);
            expect(grid.every(cell => cell.tile === emptyTile)).toBe(true);
        });

        it('skips tiles with out-of-bounds coordinates', () => {
            const tiles = [{ ...emptyTile, coordinate: { x: 0, y: 10 } }];
            const grid = matchUtil.buildGridFromSnapshot(tiles as any, []);
            expect(grid.every(cell => cell.tile === emptyTile)).toBe(true);
        });

        it('skips salmons with missing coordinates', () => {
            const salmons = [{ id: 1, coordinate: null }];
            const grid = matchUtil.buildGridFromSnapshot([], salmons as any);
            expect(grid.every(cell => cell.salmons.length === 0)).toBe(true);
        });

        it('skips salmons with out-of-bounds coordinates', () => {
            const salmons = [{ id: 1, coordinate: { x: 0, y: 10 } }];
            const grid = matchUtil.buildGridFromSnapshot([], salmons as any);
            expect(grid.every(cell => cell.salmons.length === 0)).toBe(true);
        });
    });

    describe('Grid Update Functions', () => {
        const baseGrid = new Array(18).fill(null).map(() => ({ tile: emptyTile, salmons: [] }));

        it('updateGridWithMovement', () => {
            const salmon = { id: 1, salmonsNumber: 1 };
            const myTile = { ...emptyTile, coordinate: { x: 0, y: 0 } };
            const toTravel = { ...emptyTile, coordinate: { x: 1, y: 1 } };
            const grid = matchUtil.updateGridWithMovement(baseGrid, salmon as any, myTile as any, toTravel as any);
            
            expect(grid[matchUtil.coordToIndex(0,0)].salmons).toHaveLength(0);
            expect(grid[matchUtil.coordToIndex(1,1)].salmons).toHaveLength(1);
        });

        it('updateGridWithTile', () => {
            const updatedTile = { ...emptyTile, id: 99, coordinate: { x: 2, y: 2 } };
            const grid = matchUtil.updateGridWithTile(baseGrid, updatedTile as any);
            expect(grid[matchUtil.coordToIndex(2,2)].tile.id).toBe(99);
        });

        it('updateGridWithSalmons', () => {
            const cellIndex = matchUtil.coordToIndex(0,0);
            const grid = [...baseGrid] as any[]
            grid[cellIndex] = { ...grid[cellIndex], salmons: [{ id: 1, salmonsNumber: 1 } ] };
            
            const updatedSalmons = [{ id: 1, salmonsNumber: 2 }];
            const newGrid = matchUtil.updateGridWithSalmons(grid, updatedSalmons as any);
            expect(newGrid[cellIndex].salmons[0].salmonsNumber).toBe(2);
        });

        it('updateGridWithSalmons returns original cell if no changes', () => {
            const cellIndex = matchUtil.coordToIndex(0,0);
            const salmon = { id: 1, salmonsNumber: 1 };
            const grid = [...baseGrid] as any[]
            grid[cellIndex] = { ...grid[cellIndex], salmons: [{ id: 1, salmonsNumber: 1 }] };
            
            const newGrid = matchUtil.updateGridWithSalmons(grid, [salmon as any]);
            expect(newGrid[cellIndex]).toStrictEqual(grid[cellIndex]);
        });

        it('updateGridWithMovementFromSea', () => {
            const salmon = { id: 1, salmonsNumber: 1 };
            const toTravel = { ...emptyTile, coordinate: { x: 1, y: 1 } };
            const grid = matchUtil.updateGridWithMovementFromSea(baseGrid, salmon as any, toTravel as any);
            expect(grid[matchUtil.coordToIndex(1,1)].salmons).toHaveLength(1);
        });

        it('updateGridWithMovementFromSea does nothing if salmonsNumber is 0', () => {
            const salmon = { id: 1, salmonsNumber: 0 };
            const toTravel = { ...emptyTile, coordinate: { x: 1, y: 1 } };
            const grid = matchUtil.updateGridWithMovementFromSea(baseGrid, salmon as any, toTravel as any);
            expect(grid[matchUtil.coordToIndex(1,1)].salmons).toHaveLength(0);
        });

        it('updateGridWithHerons', () => {
            const cellIndex = matchUtil.coordToIndex(0,0);
            const grid = [...baseGrid] as any[]
            grid[cellIndex] = { ...grid[cellIndex], salmons: [{ id: 1, salmonsNumber: 1 } ] };
            
            const updatedTiles = [{ ...emptyTile, coordinate: { x: 0, y: 0 } }];
            const updatedSalmons = [{ id: 1, salmonsNumber: 0 }];
            const newGrid = matchUtil.updateGridWithHerons(grid, updatedTiles as any, updatedSalmons as any, [1]);
            expect(newGrid[cellIndex].salmons).toHaveLength(0);
        });

        it('updateGridWithHerons handles missing tile coordinates', () => {
            const updatedTiles = [{ ...emptyTile, coordinate: null }];
            const newGrid = matchUtil.updateGridWithHerons(baseGrid, updatedTiles as any, []);
            expect(newGrid).toEqual(baseGrid);
        });
    });

    describe('Player List Rendering', () => {
        const players = [
            { id: 1, name: 'Alice', color: 'RED', points: 10, alive: true, energy: 2 },
            { id: 2, name: 'Bob', color: 'BLUE', points: 5, alive: false, energy: 0 }
        ];

        it('generatePlayerList returns correct JSX', () => {
            const list = matchUtil.generatePlayerList(players as any, 1);
            expect(list).toHaveLength(2);
            render(<table><tbody>{list}</tbody></table>);
            expect(screen.getByText('Alice')).toBeInTheDocument();
            expect(screen.getByText('Bob')).toBeInTheDocument();
        });

        it('renderPlayerList wraps list in table', () => {
            const list = matchUtil.generatePlayerList(players as any, 1);
            render(matchUtil.renderPlayerList(list));
            expect(screen.getByRole('table')).toBeInTheDocument();
            expect(screen.getByText('Name')).toBeInTheDocument();
        });
    });

    describe('fetchPlayers', () => {
        it('calls get and setPlayers', async () => {
            const setPlayers = vi.fn();
            vi.mocked(fetchers.get).mockResolvedValue({ ok: true, json: async () => [{ id: 1 }] } as any);
            await matchUtil.fetchPlayers({ id: 1 } as any, 'jwt', setPlayers);
            expect(fetchers.get).toHaveBeenCalledWith('/api/v1/players/match/1', 'jwt');
            expect(setPlayers).toHaveBeenCalledWith([{ id: 1 }]);
        });
    });

    describe('passTurn', () => {
        it('renders a button that calls put and changephase', async () => {
            vi.mocked(fetchers.put).mockResolvedValue({ ok: true } as any);
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            
            const myPlayer = { id: 1, energy: 3 };
            const match = { id: 1 };
            
            render(matchUtil.passTurn(match as any, 'jwt', myPlayer as any));
            const button = screen.getByRole('button', { name: /Pass Turn/i });
            
            fireEvent.click(button);
            
            expect(myPlayer.energy).toBe(2);
            expect(fetchers.put).toHaveBeenCalledWith('/api/v1/players/1', 'jwt', myPlayer);
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matches/1/changephase/1', 'jwt');
        });
    });
});
