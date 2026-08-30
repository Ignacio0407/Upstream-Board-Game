import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as matchUtil from './matchUtil';
import { Phase } from '../interfaces/Phase';
import Tile, { TileType } from '../interfaces/Tile';
import MatchTile, { emptyTile } from '../interfaces/MatchTile';
import * as fetchers from '../util/fetchers';
import ConfirmModal from '../util/ConfirmModal';
import Match from '../interfaces/Match';
import SalmonMatch from '../interfaces/SalmonMatch';

// --- Mocks ---
vi.mock('../util/fetchers', () => ({
    get: vi.fn(),
    patch: vi.fn(),
    put: vi.fn(),
}));

vi.mock('../lobby/ColorParser', () => ({
    ColorToRgb: vi.fn((color: string) => `rgb-${color}`),
}));

vi.mock('../util/ConfirmModal', () => ({
    default: vi.fn(),
}));

describe('matchUtil', () => {

    beforeEach(() => {
        vi.clearAllMocks();
        globalThis.alert = vi.fn();
        vi.spyOn(console, 'log').mockImplementation(() => {});
    });

    describe('coordToIndex', () => {
        it('calculates the index correctly at corners and center', () => {
            expect(matchUtil.coordToIndex(0, 5)).toBe(0);
            expect(matchUtil.coordToIndex(2, 5)).toBe(2);
            expect(matchUtil.coordToIndex(0, 0)).toBe(15);
            expect(matchUtil.coordToIndex(2, 0)).toBe(17);
            expect(matchUtil.coordToIndex(1, 3)).toBe(7);
        });
    });

    describe('isFull', () => {
        const mockTile = { ...emptyTile, capacity: 2, salmonsNumber: 2, tile: { type: TileType.WATER } };

        it('returns false if capacity is 0, even if salmonsNumber matches', () => {
            const tile = { ...mockTile, capacity: 0, salmonsNumber: 0 };
            expect(matchUtil.isFull(2, tile as any)).toBe(false);
        });

        it('returns true if salmonsNumber === capacity in a non-ROCK tile', () => {
            expect(matchUtil.isFull(2, mockTile as any)).toBe(true);
        });

        it('in ROCK with more than 2 players, it is considered full when capacity === salmonsNumber - 1', () => {
            const rockTile = { ...mockTile, tile: { type: TileType.ROCK }, capacity: 1, salmonsNumber: 2 };
            expect(matchUtil.isFull(3, rockTile as any)).toBe(true);
        });

        it('in ROCK with exactly 2 players, the general rule applies (not the ROCK rule)', () => {
            // playersNumber > 2 is strictly greater than 2, so with 2 players it falls into the "else" block
            const rockTile = { ...mockTile, tile: { type: TileType.ROCK }, capacity: 1, salmonsNumber: 2 };
            expect(matchUtil.isFull(2, rockTile as any)).toBe(false);
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

        it('selects the tile and deselects the salmon when it is my turn and TILES phase', () => {
            matchUtil.handleTileClick(mockTile as any, mockPlayer as any, mockMatch as any, setSelectedTile, setSelectedSalmon);
            expect(setSelectedTile).toHaveBeenCalledWith(mockTile);
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });

        it('does nothing if it is not my turn', () => {
            const otherMatch = { ...mockMatch, actualPlayerId: 2 };
            matchUtil.handleTileClick(mockTile as any, mockPlayer as any, otherMatch as any, setSelectedTile, setSelectedSalmon);
            expect(setSelectedTile).not.toHaveBeenCalled();
            expect(setSelectedSalmon).not.toHaveBeenCalled();
        });

        it('does nothing if the phase is not TILES', () => {
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

        it('selects the salmon if it is my turn, MOVING phase, and the salmon is mine', () => {
            matchUtil.handleSalmonClick(mockSalmon as any, mockPlayer as any, mockMatch as any, setSelectedSalmon);
            expect(setSelectedSalmon).toHaveBeenCalledWith(mockSalmon);
        });

        it('deselects (null) if the salmon is not mine', () => {
            const otherSalmon = { ...mockSalmon, playerId: 2 };
            matchUtil.handleSalmonClick(otherSalmon as any, mockPlayer as any, mockMatch as any, setSelectedSalmon);
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });

        it('deselects (null) if myPlayer is null', () => {
            matchUtil.handleSalmonClick(mockSalmon as any, null, mockMatch as any, setSelectedSalmon);
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });

        it('deselects (null) if it is not my turn', () => {
            const otherMatch = { ...mockMatch, actualPlayerId: 2 };
            matchUtil.handleSalmonClick(mockSalmon as any, mockPlayer as any, otherMatch as any, setSelectedSalmon);
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });

        it('deselects (null) if the phase is not MOVING', () => {
            const otherMatch = { ...mockMatch, phase: Phase.TILES };
            matchUtil.handleSalmonClick(mockSalmon as any, mockPlayer as any, otherMatch as any, setSelectedSalmon);
            expect(setSelectedSalmon).toHaveBeenCalledWith(null);
        });
    });

    describe('changephase', () => {
        it('calls patch to change phase', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.changephase({ id: 1 } as any, 'jwt', 1);
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matches/1/changephase/1', 'jwt');
        });

        it('alerts with errorData.error if provided', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ error: 'Phase Error' }) } as any);
            await matchUtil.changephase({ id: 1 } as any, 'jwt', 1);
            expect(globalThis.alert).toHaveBeenCalledWith('Phase Error');
        });

        it('alerts with errorData.message if there is no error', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ message: 'Phase Message' }) } as any);
            await matchUtil.changephase({ id: 1 } as any, 'jwt', 1);
            expect(globalThis.alert).toHaveBeenCalledWith('Phase Message');
        });

        it('rethrows the error message in the catch block', async () => {
            vi.mocked(fetchers.patch).mockRejectedValueOnce(new Error('Catch Error'));
            await expect(matchUtil.changephase({ id: 1 } as any, 'jwt', 1)).rejects.toBe('Catch Error');
        });
    });

    describe('passTurn', () => {
        it('calls patch to update energy and triggers changephase', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            const mockMatch = { id: 1 };
            const mockPlayer = { id: 5 };
            await matchUtil.passTurn(mockMatch as any, 'jwt', mockPlayer as any);
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/players/5/energy', 'jwt');
        });

        it('does not throw exceptions when invoked', () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            expect(() => matchUtil.passTurn({ id: 1 } as any, 'jwt', { id: 1 } as any)).not.toThrow();
        });
    });

    describe('updateTilePosition', () => {
        const mockTile = { id: 1 };
        const mockMatch = { phase: Phase.TILES };

        it('calls patch when phase is TILES and actualPlayerId and myPlayerId match', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.updateTilePosition(mockTile as MatchTile, 1, 1, 'jwt', 1, 1, mockMatch as Match);
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matchTiles/1', 'jwt', { x: 1, y: 1 });
        });

        it("alerts \"Now it's salmon moving phase!\" if the phase is not TILES", async () => {
            const otherMatch = { phase: Phase.MOVING };
            await matchUtil.updateTilePosition(mockTile as MatchTile, 1, 1, 'jwt', 1, 1, otherMatch as Match);
            expect(globalThis.alert).toHaveBeenCalledWith("Now it's salmon moving phase!");
            expect(fetchers.patch).not.toHaveBeenCalled();
        });

        it('alerts if actualPlayerId and myPlayerId do not match, even if the phase is TILES', async () => {
            await matchUtil.updateTilePosition(mockTile as MatchTile, 1, 1, 'jwt', 1, 2, mockMatch as Match);
            expect(globalThis.alert).toHaveBeenCalledWith("Now it's salmon moving phase!");
            expect(fetchers.patch).not.toHaveBeenCalled();
        });

        it('alerts with errorData.message if the response is not ok', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ message: 'Tile Error' }) } as any);
            await matchUtil.updateTilePosition(mockTile as MatchTile, 1, 1, 'jwt', 1, 1, mockMatch as Match);
            expect(globalThis.alert).toHaveBeenCalledWith('Tile Error');
        });

        it('logs the error to console and rethrows the exception in the catch block', async () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
            vi.mocked(fetchers.patch).mockRejectedValue(new Error('Tile Catch'));
            await expect(matchUtil.updateTilePosition(mockTile as MatchTile, 1, 1, 'jwt', 1, 1, mockMatch as Match))
                .rejects.toThrow('Tile Catch');
            expect(consoleSpy).toHaveBeenCalled();
            consoleSpy.mockRestore();
        });
    });

    describe('handleUpdateTilePosition', () => {
        const setSelectedTile = vi.fn();
        const mockMatch = { id: 1, phase: Phase.TILES };

        it('does nothing if a salmon is already selected (tile placement and movement are mutually exclusive)', async () => {
            const selectedSalmon = { id: 1 };
            await matchUtil.handleUpdateTilePosition({jwt: 'jwt', index: 0, selectedSalmon: selectedSalmon as SalmonMatch, 
                setSelectedTile, selectedTile: { id: 5 } as MatchTile, match: mockMatch as Match, actualPlayerId: 1, myPlayerId: 1});
            expect(fetchers.patch).not.toHaveBeenCalled();
            expect(setSelectedTile).not.toHaveBeenCalled();
        });

        it('only deselects the tile if no tile was passed as selected', async () => {
            await matchUtil.handleUpdateTilePosition({jwt: 'jwt', index: 0, selectedSalmon: null, setSelectedTile, 
                selectedTile: null, match: mockMatch as Match, actualPlayerId: 1, myPlayerId: 1});
            expect(fetchers.patch).not.toHaveBeenCalled();
            expect(setSelectedTile).toHaveBeenCalledWith(null);
        });

        it('places the tile, deselects it, and changes phase when the response is ok and the phase is TILES', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            const selectedTile = { id: 1 };
            await matchUtil.handleUpdateTilePosition({jwt: 'jwt', index: 0, selectedSalmon: null, 
                setSelectedTile, selectedTile: selectedTile as MatchTile, match: mockMatch as Match, actualPlayerId: 1, myPlayerId: 1});

            // One call to position the tile and another to change the phase
            expect(fetchers.patch).toHaveBeenCalledTimes(2);
            expect(fetchers.patch).toHaveBeenNthCalledWith(1, '/api/v1/matchTiles/1', 'jwt', { x: 0, y: 5 });
            expect(fetchers.patch).toHaveBeenNthCalledWith(2, '/api/v1/matches/1/changephase/1', 'jwt');
            expect(setSelectedTile).toHaveBeenCalledWith(null);
        });

        it('does not change phase if the tile placement response is not ok', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: false, json: async () => ({ message: 'err' }) } as any);
            const selectedTile = { id: 1 };
            await matchUtil.handleUpdateTilePosition({jwt: 'jwt', index: 0, selectedSalmon: null, setSelectedTile, 
                selectedTile: selectedTile as MatchTile, match: mockMatch as Match, actualPlayerId: 1, myPlayerId: 1});
            expect(fetchers.patch).toHaveBeenCalledTimes(1);
            expect(setSelectedTile).toHaveBeenCalledWith(null);
        });

        it('does not change phase if updateTilePosition does not execute the patch (different actualPlayerId)', async () => {
            const selectedTile = { id: 1 };
            await matchUtil.handleUpdateTilePosition({
                jwt: 'jwt', 
                index: 0, 
                selectedSalmon: null, 
                setSelectedTile, 
                selectedTile: selectedTile as any, 
                match: mockMatch as Match, 
                actualPlayerId: 1, 
                myPlayerId: 2
            });
            expect(fetchers.patch).not.toHaveBeenCalled();
            expect(globalThis.alert).toHaveBeenCalledWith("Now it's salmon moving phase!");
        });
    });

    describe('handleRotateTile', () => {
        it('calls patch to rotate the tile to the next orientation', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.handleRotateTile({ ...emptyTile, id: 1 }, 'jwt');
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matchTiles/1/rotate', 'jwt', { rotation: 1 });
        });

        it('the orientation wraps around (mod 6) when reaching 5', async () => {
            vi.mocked(fetchers.patch).mockResolvedValue({ ok: true } as any);
            await matchUtil.handleRotateTile({ ...emptyTile, id: 1, orientation: 5 } as any, 'jwt');
            expect(fetchers.patch).toHaveBeenCalledWith('/api/v1/matchTiles/1/rotate', 'jwt', { rotation: 0 });
        });

        it('logs the error to the console if the response is not ok', async () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
            vi.mocked(fetchers.patch).mockResolvedValueOnce({ ok: false } as any);
            await matchUtil.handleRotateTile(emptyTile, 'jwt');
            expect(consoleSpy).toHaveBeenCalledWith('Error rotating tile:', expect.any(Error));
            consoleSpy.mockRestore();
        });

        it('logs the error to the console if patch throws a network exception', async () => {
            const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
            vi.mocked(fetchers.patch).mockRejectedValueOnce(new Error('Rotate Catch'));
            await matchUtil.handleRotateTile({ id: 1, orientation: 0 } as any, 'jwt');
            expect(consoleSpy).toHaveBeenCalledWith('Error rotating tile:', expect.any(Error));
            consoleSpy.mockRestore();
        });
    });

    describe('getRotationStyle', () => {
        it('returns the correct transform (orientation * 60º)', () => {
            const tile = { ...emptyTile, orientation: 2 };
            expect(matchUtil.getRotationStyle(tile as any)).toEqual({ transform: 'rotate(120deg)' });
        });

        it('returns an empty object if the tile is null/undefined', () => {
            expect(matchUtil.getRotationStyle(null as any)).toEqual({});
        });
    });

    describe('generatePlayerList / renderPlayerList', () => {
        const players = [
            { id: 1, name: 'Alice', color: 'RED', points: 10, alive: true, energy: 2 },
            { id: 2, name: 'Bob', color: 'BLUE', points: 5, alive: false, energy: 0 },
        ];

        it('generates a row per player with name, points, and color', () => {
            const list = matchUtil.generatePlayerList(players as any, 1);
            expect(list).toHaveLength(2);
            render(<table><tbody>{list}</tbody></table>);
            expect(screen.getByText('Alice')).toBeInTheDocument();
            expect(screen.getByText('Bob')).toBeInTheDocument();
            expect(screen.getByText('10')).toBeInTheDocument();
        });

        it("marks the active player's row with the table-row-active class", () => {
            const list = matchUtil.generatePlayerList(players as any, 1);
            const { container } = render(<table><tbody>{list}</tbody></table>);
            const rows = container.querySelectorAll('tr');
            expect(rows[0].className).toContain('table-row-active');
            expect(rows[1].className).not.toContain('table-row-active');
        });

        it("shows the 'alive' or 'dead' icon based on the alive status", () => {
            const list = matchUtil.generatePlayerList(players as any, 1);
            const { container } = render(<table><tbody>{list}</tbody></table>);
            expect(container.querySelector('.fa-check-circle')).toBeInTheDocument();
            expect(container.querySelector('.fa-times-circle')).toBeInTheDocument();
        });

        it("shows a lightning bolt for each energy point, or the 'tired' icon if energy is 0", () => {
            const list = matchUtil.generatePlayerList(players as any, 1);
            const { container } = render(<table><tbody>{list}</tbody></table>);
            expect(container.querySelectorAll('.fa-bolt')).toHaveLength(2); // Alice: energy 2
            expect(container.querySelector('.fa-tired')).toBeInTheDocument(); // Bob: energy 0
        });

        it('renderPlayerList wraps the list in a table with headers', () => {
            const list = matchUtil.generatePlayerList(players as any, 1);
            render(matchUtil.renderPlayerList(list));
            expect(screen.getByRole('table')).toBeInTheDocument();
            expect(screen.getByText('Name')).toBeInTheDocument();
            expect(screen.getByText('Energy')).toBeInTheDocument();
        });
    });

    describe('buildGridFromSnapshot', () => {
        it('places tiles and salmons at the correct index according to their coordinate', () => {
            const tiles = [{ ...emptyTile, coordinate: { x: 1, y: 1 } }];
            const salmons = [{ id: 1, coordinate: { x: 1, y: 1 } }];
            const grid = matchUtil.buildGridFromSnapshot(tiles as any, salmons as any);
            const index = matchUtil.coordToIndex(1, 1);
            expect(grid[index].tile).toEqual(tiles[0]);
            expect(grid[index].salmons).toHaveLength(1);
        });

        it('ignores tiles without coordinates', () => {
            const tiles = [{ ...emptyTile, coordinate: null }];
            const grid = matchUtil.buildGridFromSnapshot(tiles as any, []);
            expect(grid.every(cell => cell.tile === emptyTile)).toBe(true);
        });

        it('ignores tiles with Y coordinate out of bounds (spawn/extended sea)', () => {
            const tiles = [{ ...emptyTile, coordinate: { x: 0, y: 10 } }];
            const grid = matchUtil.buildGridFromSnapshot(tiles as any, []);
            expect(grid.every(cell => cell.tile === emptyTile)).toBe(true);
        });

        it('ignores salmons without coordinates (in the sea/initial spawn)', () => {
            const salmons = [{ id: 1, coordinate: null }];
            const grid = matchUtil.buildGridFromSnapshot([], salmons as any);
            expect(grid.every(cell => cell.salmons.length === 0)).toBe(true);
        });

        it('ignores salmons with Y coordinate out of bounds', () => {
            const salmons = [{ id: 1, coordinate: { x: 0, y: 10 } }];
            const grid = matchUtil.buildGridFromSnapshot([], salmons as any);
            expect(grid.every(cell => cell.salmons.length === 0)).toBe(true);
        });
    });

    describe('updateGridWithTile', () => {
        it("updates only the cell corresponding to the tile's coordinate", () => {
            const baseGrid = new Array(18).fill(null).map(() => ({ tile: emptyTile, salmons: [] }));
            const updatedTile = { ...emptyTile, id: 99, coordinate: { x: 2, y: 2 } };
            const grid = matchUtil.updateGridWithTile(baseGrid as any, updatedTile as any);
            expect(grid[matchUtil.coordToIndex(2, 2)].tile.id).toBe(99);
            // The remaining cells should not be affected
            expect(grid[matchUtil.coordToIndex(0, 0)].tile).toBe(emptyTile);
        });
    });

    describe('confirmMovementBatch', () => {
        const match = { id: 1 };
        const myPlayerId = 1;
        const initialState = { salmons: [], matchTiles: [], energy: 3 };
        let setConfirmBatchIsSubmitting: any, restoreSnapshot: any, clearPath: any;

        beforeEach(() => {
            setConfirmBatchIsSubmitting = vi.fn();
            restoreSnapshot = vi.fn();
            clearPath = vi.fn();
        });

        it('happy path: saves the batch, clears the path, stops submitting, and changes phase', async () => {
            vi.mocked(fetchers.patch)
                .mockResolvedValueOnce({ ok: true } as any) // move-batch
                .mockResolvedValueOnce({ ok: true } as any); // changephase

            await matchUtil.confirmMovementBatch({pendingMoves: [{ salmonId: 1, x: 0, y: 0 }], jwt: 'jwt', match: match as Match,
                myPlayerId, initialState, setConfirmBatchIsSubmitting, restoreSnapshot, clearPath});

            expect(fetchers.patch).toHaveBeenNthCalledWith(1, '/api/v1/salmonMatches/move-batch/1', 'jwt', [{ salmonId: 1, x: 0, y: 0 }]);
            expect(clearPath).toHaveBeenCalledTimes(1);
            expect(setConfirmBatchIsSubmitting).toHaveBeenCalledWith(false);
            expect(fetchers.patch).toHaveBeenNthCalledWith(2, '/api/v1/matches/1/changephase/1', 'jwt');
            expect(restoreSnapshot).not.toHaveBeenCalled();
        });

        it('if batch submission fails (move-batch not ok), restores the initial snapshot and does not change phase', async () => {
            vi.mocked(fetchers.patch).mockResolvedValueOnce({ ok: false, text: async () => 'boom' } as any);

            await matchUtil.confirmMovementBatch({pendingMoves: [{ salmonId: 1, x: 0, y: 0 }], jwt: 'jwt', match: match as any,
                myPlayerId, initialState, setConfirmBatchIsSubmitting, restoreSnapshot, clearPath});

            expect(globalThis.alert).toHaveBeenCalledWith('Movement could not be processed. Restoring position...');
            expect(restoreSnapshot).toHaveBeenCalledWith(initialState);
            expect(clearPath).toHaveBeenCalledTimes(1);
            expect(setConfirmBatchIsSubmitting).toHaveBeenCalledWith(false);
            // Patch was called only once: the move-batch. changephase is never invoked.
            expect(fetchers.patch).toHaveBeenCalledTimes(1);
        });

        it('if the batch is saved but changephase fails, retries up to 3 times and recovers on the second attempt', async () => {
            vi.mocked(fetchers.patch)
                .mockResolvedValueOnce({ ok: true } as any)      // move-batch ok
                .mockRejectedValueOnce(new Error('net'))          // changephase initial attempt (throws to catch)
                .mockRejectedValueOnce(new Error('net'))          // changephase retry 1
                .mockResolvedValueOnce({ ok: true } as any);      // changephase retry 2 -> success

            vi.useFakeTimers();
            const promise = matchUtil.confirmMovementBatch({pendingMoves: [{ salmonId: 1, x: 0, y: 0 }], jwt: 'jwt', 
                match: match as any, myPlayerId, initialState, setConfirmBatchIsSubmitting, restoreSnapshot, clearPath});
            await vi.runAllTimersAsync();
            await promise;
            vi.useRealTimers();

            expect(restoreSnapshot).not.toHaveBeenCalled();
            expect(globalThis.alert).not.toHaveBeenCalled();
            expect(clearPath).toHaveBeenCalledTimes(2); // once in the try block, another in the catch block after exhausting the flow
        });

        it('if the batch is saved but changephase fails all 3 retries, alerts of persistent failure', async () => {
            vi.mocked(fetchers.patch)
                .mockResolvedValueOnce({ ok: true } as any) // move-batch ok
                .mockRejectedValueOnce(new Error('net'))    // changephase initial attempt
                .mockRejectedValueOnce(new Error('net'))    // retry 1
                .mockRejectedValueOnce(new Error('net'))    // retry 2
                .mockRejectedValueOnce(new Error('net'));   // retry 3

            vi.useFakeTimers();
            const promise = matchUtil.confirmMovementBatch({pendingMoves: [{ salmonId: 1, x: 0, y: 0 }], jwt: 'jwt',
                match: match as any, myPlayerId, initialState, setConfirmBatchIsSubmitting, restoreSnapshot, clearPath});
            await vi.runAllTimersAsync();
            await promise;
            vi.useRealTimers();

            expect(globalThis.alert).toHaveBeenCalledWith('Persistent network failure. Movement was saved, reload to continue.');
            expect(restoreSnapshot).not.toHaveBeenCalled();
        });
    });

    describe('handleNoMoves', () => {
        const myPlayer = { id: 1, energy: 0 };
        const match = { id: 1 };
        const setVisible = vi.fn();
        const handleConfirmBatch = vi.fn();

        it('if there is no history (first move without plays), attempts to pass the turn', () => {
            matchUtil.handleNoMoves(myPlayer as any, match as any, 'jwt', [], false, setVisible, handleConfirmBatch);
            // We cannot verify the effect of passTurn (see bug), but we can verify the modal doesn't open
            expect(ConfirmModal).not.toHaveBeenCalled();
        });

        it('if there is already a movement history, opens the confirmation modal instead of passing the turn directly', () => {
            matchUtil.handleNoMoves(myPlayer as any, match as any, 'jwt', [{ salmons: [], matchTiles: [], energy: 3 }], false, setVisible, handleConfirmBatch);
            expect(ConfirmModal).toHaveBeenCalledWith({ visible: false, setVisible, onConfirm: handleConfirmBatch });
        });
    });

    describe('calculateSalmonPosition', () => {
        it.each([1, 2, 3, 4, 5])('returns defined left/top for %i salmons in the same cell', (num) => {
            const pos = matchUtil.calculateSalmonPosition(0, num);
            expect(pos.left).toBeDefined();
            expect(pos.top).toBeDefined();
        });

        it('in the case of 2 salmons, index 0 and index 1 are on opposite sides', () => {
            const pos0 = matchUtil.calculateSalmonPosition(0, 2);
            const pos1 = matchUtil.calculateSalmonPosition(1, 2);
            expect(pos0.left).not.toBe(pos1.left);
            expect(pos0.top).toBe(pos1.top);
        });

        it('returns the default centered position for an unaccounted amount (6+)', () => {
            const pos = matchUtil.calculateSalmonPosition(0, 6);
            expect(pos).toEqual(matchUtil.calculateSalmonPosition(0, 1));
        });
    });
});