import { ColorToRgb } from "../lobby/ColorParser.ts";
import { patch } from "../util/fetchers.ts";
import '../static/css/game/game.css'
import MatchTile, { emptyTile } from '../interfaces/MatchTile.ts'
import Player from '../interfaces/Player.ts'
import Match from "../interfaces/Match.ts";
import SalmonMatch from "../interfaces/SalmonMatch.ts";
import { JSX } from "react";
import { Phase } from "../interfaces/Phase.ts";
import ConfirmModal from "../util/ConfirmModal.tsx";
import { ValidMove } from "./calculateValidMoves.ts";
import Coordinate from "../interfaces/Coordinate.ts";

export const GRID_WIDTH = 3;
export const GRID_HEIGHT = 6;
export const coordToIndex = (x: number, y: number) => { return (GRID_HEIGHT - 1 - y) * GRID_WIDTH + x; };
export type GridCell = {tile: MatchTile, salmons: SalmonMatch[]};

export function isFull (tile: MatchTile) {
    if (tile.capacity === 0) return false;
    return tile.salmonsNumber >= tile.capacity;
}

export const handleTileClick = (tile:MatchTile, myPlayer:Player, match:Match, setSelectedTile: (tile: MatchTile | null) => void, setSelectedSalmon: (salmon: SalmonMatch | null) => void) => {
    if (myPlayer.id === match.actualPlayerId && match.phase === Phase.TILES) {
        setSelectedTile(tile);
        setSelectedSalmon(null);
    }
}

export const handleSalmonClick = (salmon: SalmonMatch, myPlayer: Player | null, match: Match, setSelectedSalmon: (salmon: SalmonMatch | null) => void) => {
    if (myPlayer?.id === match.actualPlayerId && myPlayer?.id === salmon.playerId && match.phase === Phase.MOVING)
        setSelectedSalmon(salmon);
    else
        setSelectedSalmon(null);
}

export const changephase = async(match:Match, jwt:string, myPlayerId:number) => {
    try {
        const responseChangePhase = await patch(`/api/v1/matches/${match.id}/changephase/${myPlayerId}`, jwt)
        if (!responseChangePhase.ok) {
            const errorData = await responseChangePhase.json();
            alert(errorData.error || errorData.message);
            console.log("Error changing phase.", responseChangePhase.status);
        }            
    } catch (error:any) {
        console.log("Error changing phase from catch.", error)
        throw error.message;
    }
};

export const passTurn = async (match:Match, jwt:string, myPlayer:Player) => {
    await patch(`/api/v1/players/${myPlayer.id}/energy`, jwt);
    await changephase(match, jwt, myPlayer.id);
};

export const updateTilePosition = async (tile:MatchTile, x:number, y:number, jwt:string, actualPlayerId:number|undefined, myPlayerId:number, match:Match) => {
    if (match.phase === Phase.TILES && actualPlayerId === myPlayerId) {
        try {
        const response = await patch(`/api/v1/matchTiles/${tile.id}`, jwt, { x, y, orientation: tile.orientation });
        if (!response.ok) {
            const errorData = await response.json();
            alert(errorData.message);
            console.log("Error positioning tile:", response.status);
        }
        return response;
    } catch (error) {
        console.error('Error updating tile position:', error);
        throw error; 
    }
    }
    else alert("Now it's salmon moving phase!")
};

export interface HandleUpdateTilePositionConfig {jwt: string; index: number; selectedSalmon: SalmonMatch | null;
    setSelectedTile: (tile: MatchTile | null) => void; selectedTile: MatchTile | null; match: Match; 
    actualPlayerId: number | undefined; myPlayerId: number; matchTilesNoCoordinate?: MatchTile[];
}

export async function handleUpdateTilePosition(config: HandleUpdateTilePositionConfig) {
    const {
        jwt, index, selectedSalmon, setSelectedTile, selectedTile, match, actualPlayerId, myPlayerId, matchTilesNoCoordinate
    } = config;

    const x = index % GRID_WIDTH;
    const y = GRID_HEIGHT - 1 - Math.floor(index / GRID_WIDTH);
    let updateTilePositionResponse;
    if (selectedSalmon === null) {
        const tileToPlace = selectedTile || (matchTilesNoCoordinate && matchTilesNoCoordinate.length > 0 ? matchTilesNoCoordinate[0] : null);
        if (tileToPlace) {
            updateTilePositionResponse = await updateTilePosition(tileToPlace, x, y, jwt, actualPlayerId, myPlayerId, match);
        }
        setSelectedTile(null);
        if (updateTilePositionResponse != null && updateTilePositionResponse.ok && match.phase === Phase.TILES) await changephase(match, jwt, myPlayerId);
    }
};

export const handleRotateTile = async (tile: MatchTile, jwt: string) => {
    const currentOrientation = tile.orientation ?? 0;
    const newOrientation = (currentOrientation + 1) % 6;
    try {
        const response = await patch(`/api/v1/matchTiles/${tile.id}/rotate`, jwt, { rotation: newOrientation });
        if (response && !response.ok) {
            console.error('Error rotating tile:', new Error('Error rotating tile'));
        }
    } catch (error) {
        console.error('Error rotating tile:', error);
    }
};

export const getRotationStyle = (tile:MatchTile) => {
    if (!tile)
        return {};
    return {
        transform: `rotate(${tile.orientation * 60}deg)` // If orientation goes from 0 to 6, rotates in 60 degrees increments
    };
};

export const generatePlayerList = (players:Player[], actualPlayerId:number) => {
    return players.map((p) => (
        <tr key={p.id} className={`table-row ${p.id === actualPlayerId ? 'table-row-active' : ''}`}>
            <td className="table-cell player-cell-name">{p.name}</td>
            <td className="table-cell player-cell-color" style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                <div className="player-color-circle" style={{ backgroundColor: `${ColorToRgb(p.color)}` }}></div>
            </td>   
            <td className="table-cell player-cell-points">{p.points}</td>
            <td className="table-cell player-cell-alive">
                {p.alive 
                    ? <i className="fa fa-check-circle" style={{ color: 'green' }}></i> 
                    : <i className="fa fa-times-circle" style={{ color: 'red' }}></i>}
            </td>
            <td className="table-cell player-cell-energy">
                {p.energy > 0 
                    ? Array.from({ length: p.energy }).map((_, i) => (
                        <i key={`energy-${p.id}-${i}`} className="fa fa-bolt" style={{ color: '#FFD700', marginRight: '4px' }}></i>
                    )) 
                    : <i className="fa fa-tired" style={{ color: '#FF8888' }}></i>}
            </td>
        </tr>
    ));
};

export const renderPlayerList = (playerList:JSX.Element[]) => {
    return <table className="users-table">
                <thead>
                    <tr>
                        <th className="table-header">Name</th>
                        <th className="table-header">Color</th>
                        <th className="table-header">Points</th>
                        <th className="table-header">Alive</th>
                        <th className="table-header">Energy</th>
                    </tr>
                </thead>
                <tbody>
                    {playerList}
                </tbody>
            </table>
}

export function buildGridFromSnapshot(tiles: MatchTile[], salmons: SalmonMatch[]): GridCell[] {
    const grid: GridCell[] = new Array(18).fill(null).map(() => ({
        tile: emptyTile,
        salmons: [],
    }));
    tiles.forEach(tile => {
        if (!tile.coordinate) return;
        const { x, y } = tile.coordinate;
        if (y < 0 || y >= GRID_HEIGHT) return;
        const index = coordToIndex(x, y);
        grid[index].tile = tile;
    });
    salmons.forEach(salmon => {
        if (!salmon.coordinate) return;
        const { x, y } = salmon.coordinate;
        if (y < 0 || y >= GRID_HEIGHT) return;
        const index = coordToIndex(x, y);
        grid[index].salmons.push(salmon);
    });
    return grid;
}

export const updateGridWithTile = (prevGrid: GridCell[], updatedTile: MatchTile): GridCell[] => {
  const { x, y } = updatedTile.coordinate;
  const index = coordToIndex(x, y);
  const newGrid = [...prevGrid];
  newGrid[index] = {
    ...newGrid[index],
    tile: updatedTile,
  };
  return newGrid;
};

export interface HistoryState { salmons: SalmonMatch[], matchTiles: MatchTile[], energy: number };
export type PendingMove = { salmonId: number, x: number, y: number, enterSpawn?: boolean };
export interface ConfirmMovementBatchConfig {pendingMoves: PendingMove[]; jwt: string; match: Match;
    myPlayerId: number; initialState: HistoryState; setConfirmBatchIsSubmitting: (isSubmitting: boolean) => void;
    restoreSnapshot: (state: HistoryState) => void; clearPath: () => void;
}

export async function confirmMovementBatch(config: ConfirmMovementBatchConfig) {
    const {
        pendingMoves, jwt, match, myPlayerId, initialState, setConfirmBatchIsSubmitting, restoreSnapshot, clearPath
    } = config;

    let moveBatchSuccess = false;
    try {
        const response = await patch(`/api/v1/salmonMatches/move-batch/${match.id}`, jwt, pendingMoves);
        if (!response.ok) throw new Error(await response.text());
        moveBatchSuccess = true;
        // If all goes well, path is cleared. The WebSocket will handle confirmation for everyone.
        clearPath();
        setConfirmBatchIsSubmitting(false);
        await changephase(match, jwt, myPlayerId);
    } catch (error: any) {
        console.error("Error in confirmation sequence:", error);
        // CASE A: Failed the first thing we sent. The backend is clean, we can do a safe rollback.
        if (!moveBatchSuccess) {
            alert("Movement could not be processed. Restoring position...");
            restoreSnapshot(initialState); 
        } 
        // CASE B: All was saved, but changePhase failed. We retry.
        else {
            const attemptChangePhase = async (remaining: number): Promise<void> => {
                try {
                    await changephase(match, jwt, myPlayerId);
                } catch (retryError) {
                    console.warn("Retry changephase failed:", retryError);
                    if (remaining <= 1) {
                        alert("Persistent network failure. Movement was saved, reload to continue.");
                    } else {
                        await new Promise(res => setTimeout(res, 1000));
                        await attemptChangePhase(remaining - 1);
                    }
                }
            };
            await attemptChangePhase(3);
        }
        clearPath();
        setConfirmBatchIsSubmitting(false);
    }
};

export const handleNoMoves = (myPlayer: Player, match: Match, jwt: string, history: any[], 
        visible: boolean, setVisible: (visible: boolean) => void, handleConfirmBatch: () => void) => {
    if (history.length === 0)
        void passTurn(match, jwt, myPlayer);
    else 
        ConfirmModal({ visible: visible, setVisible: setVisible, onConfirm: handleConfirmBatch});
};

export const calculateSalmonPosition = (index: number, totalSalmons: number) => {
  // We keep the original pixel values to act as our reference proportions
  const refTileWidth = 250;
  const refTileHeight = 217;
  const refSalmonWidth = 80;
  const refSalmonHeight = 77;

  // Center of the hexagon (adjusted to center the salmon)
  const centerX = refTileWidth / 2 - refSalmonWidth / 2;
  const centerY = refTileHeight / 2 - refSalmonHeight / 2;

  // Radius from hexagon center to salmons
  const radius = 60;

  let x: number, y: number;

  if (totalSalmons === 1) {
    x = centerX;
    y = centerY;
  } else {
    let angle = 0;

    switch (totalSalmons) {
      case 2: {
        // 2 salmons: one smoothly to the left, the other to the right, in the vertical center
        const offset = 40; // horizontal separation
        x = refTileWidth / 2 + (index === 0 ? -offset : +offset) - refSalmonWidth / 2;
        y = refTileHeight / 2 - refSalmonHeight / 2;
        break;
      }

      case 3: {
        // Triangle: angles 0°, 120°, 240° (starting from the top)
        angle = (index * 2 * Math.PI) / 3 - Math.PI / 2; // -PI/2 to start from the top
        x = refTileWidth / 2 + radius * Math.cos(angle) - refSalmonWidth / 2;
        y = refTileHeight / 2 + radius * Math.sin(angle) - refSalmonHeight / 2;
        break;
      }

      case 4: {
        // Square: angles 45°, 135°, 225°, 315° (rotated so it's not in a cross)
        angle = (index * 2 * Math.PI) / 4 + Math.PI / 4; // +PI/4 to start from a corner
        x = refTileWidth / 2 + radius * Math.cos(angle) - refSalmonWidth / 2;
        y = refTileHeight / 2 + radius * Math.sin(angle) - refSalmonHeight / 2;
        break;
      }

      case 5: {
        // Pentagon: 5 points, each 72°, starting from the top
        angle = (index * 2 * Math.PI) / 5 - Math.PI / 2; // start from the top
        x = refTileWidth / 2 + radius * Math.cos(angle) - refSalmonWidth / 2;
        y = refTileHeight / 2 + radius * Math.sin(angle) - refSalmonHeight / 2;
        break;
      }
      default: {
        x = centerX;
        y = centerY;
      }
    }
  }
  // Convert absolute pixel positions to percentages based on the reference dimensions
  const xPercent = (x / refTileWidth) * 100;
  const yPercent = (y / refTileHeight) * 100;
  return {
    left: `${xPercent}%`,
    top: `${yPercent}%`
  };

};

// onGridClickWrapper Helpers

export const indexToCoords = (index: number) => ({ x: index % GRID_WIDTH, y: GRID_HEIGHT - 1 - Math.floor(index / GRID_WIDTH) });

export const setPlayerEnergy = (players: Player[], playerId: number, energy: number) => players.map(p => p.id === playerId ? { ...p, energy } : p);

const getSalmonImage = (image: string, salmonsNumber: number) => salmonsNumber === 1 && image.includes('2') ? image.replace('2', '1') : image;

export const buildUpdatedSalmon = (selectedSalmon: SalmonMatch, move: any, x: number, y: number) => {
    const newSalmonsNumber = Math.max(0, selectedSalmon.salmonsNumber - move.salmonLoss);
    const newImage = getSalmonImage(selectedSalmon.salmon.image, newSalmonsNumber);
    return { ...selectedSalmon, salmonsNumber: newSalmonsNumber, salmon: { ...selectedSalmon.salmon, image: newImage }, coordinate: { x, y } };
};

export const replaceSalmon = (salmons: SalmonMatch[], updatedSalmon: SalmonMatch) => salmons.map(s => s.id === updatedSalmon.id ? updatedSalmon : s).filter(s => s.salmonsNumber > 0);

const isPlayableCoordinate = (coordinate: Coordinate | null) => !coordinate || (coordinate.y < 20 && !(coordinate.x === 1 && coordinate.y === 5));

export const isActiveSalmonOf = (salmon: SalmonMatch, playerId: number) => salmon.playerId === playerId && salmon.salmonsNumber > 0 && isPlayableCoordinate(salmon.coordinate);

export const toNumberOr = (value: any, fallback: number) => value ? Number(value) : Number(fallback);

export const getExitCoords = (salmon: SalmonMatch) => ({ exitX: salmon.coordinate ? Number(salmon.coordinate.x) : null, exitY: salmon.coordinate ? Number(salmon.coordinate.y) : null });

const isSameCell = (tileX: number, tileY: number, cellX: number | null, cellY: number | null) => cellX !== null && cellY !== null && tileX === cellX && tileY === cellY;

const isJumpedCell = (tileX: number, tileY: number, move: ValidMove) => !!move.jumpedCoords && tileX === Number(move.jumpedCoords.x) && tileY === Number(move.jumpedCoords.y);

const isEagleKillZone = (tileX: number, tileY: number, finalX: number, finalY: number, move: ValidMove) => isSameCell(tileX, tileY, finalX, finalY) || isJumpedCell(tileX, tileY, move);

const killEagle = (tile: MatchTile) => {
    if (tile.tile?.type !== 'EAGLE') return;
    tile.tile = { ...tile.tile, type: 'WATER' as any };
    if (tile.tile.image) tile.tile.image = tile.tile.image.replace('eagle', 'water');
};

export const updateMatchTile = (t: MatchTile, finalX: number, finalY: number, exitX: number | null, exitY: number | null, move: ValidMove) => {
    const newT = { ...t };
    if (!newT.coordinate) return newT;
    const tileX = Number(newT.coordinate.x);
    const tileY = Number(newT.coordinate.y);
    // 1. Remove salmon from exit
    if (isSameCell(tileX, tileY, exitX, exitY)) newT.salmonsNumber = Number(newT.salmonsNumber) - 1;
    // 2. Add salmon to destination
    if (isSameCell(tileX, tileY, finalX, finalY)) newT.salmonsNumber = Number(newT.salmonsNumber) + 1;
    // 3. Kill eagle if it's the destination or the jumped square
    if (isEagleKillZone(tileX, tileY, finalX, finalY, move)) killEagle(newT);
    return newT;
};

const getBackendX = (move: any, selectedSalmon: any, x: number) => move.isJump && Math.abs(move.x - selectedSalmon.coordinate?.x) === 2 ? move.jumpedCoords!.x : x;

export const buildPendingMove = (salmonId: any, x: number, y: number, move: any, selectedSalmon: any, round: number) => {
    const backendY = move.isJump ? y - 1 : y;
    if (x === 1 && y === 5 && round >= 6) return { salmonId, x, y: backendY, enterSpawn: true };
    return { salmonId, x: getBackendX(move, selectedSalmon, x), y: backendY };
};