import { ColorToRgb } from "../lobby/ColorParser.ts";
import { patch } from "../util/fetchers.ts";
import '../static/css/game/game.css'
import MatchTile, { emptyTile } from '../interfaces/MatchTile.ts'
import Player from '../interfaces/Player.ts'
import Match from "../interfaces/Match.ts";
import SalmonMatch from "../interfaces/SalmonMatch.ts";
import { JSX } from "react";
import { TileType } from "../interfaces/Tile.ts";
import { Phase } from "../interfaces/Phase.ts";
import ConfirmModal from "../util/ConfirmModal.tsx";

export const GRID_WIDTH = 3;
export const GRID_HEIGHT = 6;
export const coordToIndex = (x: number, y: number) => { return (GRID_HEIGHT - 1 - y) * GRID_WIDTH + x; };
export type GridCell = {tile: MatchTile, salmons: SalmonMatch[]};

export function isFull (playersNumber:number, tile:MatchTile) {
    if (tile.capacity === 0) return false;
    if (playersNumber > 2 && (tile.tile.type === TileType.ROCK)) return tile.capacity === (tile.salmonsNumber - 1);
    else return tile.capacity === tile.salmonsNumber; 
}

export const handleTileClick = (tile:MatchTile, myPlayer:Player, match:Match, setSelectedTile:Function, setSelectedSalmon:Function) => {
    if (myPlayer.id === match.actualPlayerId && match.phase === Phase.TILES) {
        setSelectedTile(tile);
        setSelectedSalmon(null);
    }
}

export const handleSalmonClick = (salmon: SalmonMatch, myPlayer: Player | null, match: Match, setSelectedSalmon: Function) => {
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
    changephase(match, jwt, myPlayer.id);
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

export async function handleUpdateTilePosition(jwt:string, index:number, selectedSalmon:SalmonMatch|null, setSelectedTile:Function, selectedTile:MatchTile|null, match:Match, actualPlayerId:number|undefined, myPlayerId:number, matchTilesNoCoordinate?: MatchTile[]) {
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
            <td className="table-cell" style={{ position: 'relative', padding: '20px' }}>{p.name}</td>
            <td className="table-cell" style={{ padding: '0', textAlign: 'center', verticalAlign: 'middle'}}>
                <div style={{ width: '30px', height: '30px', borderRadius: '50%', backgroundColor: `${ColorToRgb(p.color)}`, 
                border: '1px solid #000', display: 'inline-block'}}>
                    
                </div>
            </td>   
            <td className="table-cell" style={{ position: 'relative', padding: '20px', paddingLeft: '55px', paddingRight: '40px'}}>{p.points}</td>
            <td className="table-cell" style={{ position: 'relative', padding: '20px' }}>
                {p.alive 
                    ? <i className="fa fa-check-circle" style={{ color: 'green' }}></i> 
                    : <i className="fa fa-times-circle" style={{ color: 'red' }}></i>}
            </td>
            <td className="table-cell" style={{ position: 'relative', padding: '20px' }}>
                {p.energy > 0 
                    ? Array.from({ length: p.energy }).map((_, i) => (
                        <i key={i} className="fa fa-bolt" style={{ color: '#FFD700', marginRight: '5px' }}></i>
                    )) 
                    : <i className="fa fa-tired" style={{ color: '#FF8888' }}></i>}
            </td>
        </tr>
    ));
};

export const renderPlayerList = (playerList:JSX.Element[]) => {
    return <table className="users-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                    <tr>
                        <th className="table-header" style={{ textAlign: 'left', padding: '15px' }}>Name</th>
                        <th className="table-header" style={{ textAlign: 'left', padding: '15px' }}>Color</th>
                        <th className="table-header" style={{ textAlign: 'left', padding: '15px' }}>Points</th>
                        <th className="table-header" style={{ textAlign: 'left', padding: '15px' }}>Alive</th>
                        <th className="table-header" style={{ textAlign: 'left', padding: '15px' }}>Energy</th>
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

export type PendingMove = { salmonId: number, x: number, y: number, enterSpawn?: boolean };
// --- Send batch to backend and manage rollback ---
export async function confirmMovementBatch(pendingMoves: PendingMove[], jwt: string, match: Match, 
    myPlayerId: number, initialState: any, setConfirmBatchIsSubmitting: (isSubmitting: boolean) => void, restoreSnapshot: Function, clearPath: Function) {
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
            let retries = 3;
            while (retries > 0) {
                try {
                    await changephase(match, jwt, myPlayerId);
                    break;
                } catch (retryError) {
                    console.warn("Retry changephase failed:", retryError);
                    retries--;
                    if (retries === 0) alert("Persistent network failure. Movement was saved, reload to continue.");
                    else await new Promise(res => setTimeout(res, 1000));
                }
            }
        }
        clearPath();
        setConfirmBatchIsSubmitting(false);
    }
};

export const handleNoMoves = (myPlayer: Player, match: Match, jwt: string, history: any[], 
        visible: boolean, setVisible: (visible: boolean) => void, handleConfirmBatch: () => void) => {
    if (history.length === 0)
        passTurn(match, jwt, myPlayer);
    else 
        ConfirmModal({ visible: visible, setVisible: setVisible, onConfirm: handleConfirmBatch});
};

export const calculateSalmonPosition = (index:number, totalSalmons:number) => {
  const tileWidth = 250;
  const tileHeight = 217;
  const salmonWidth = 80;
  const salmonHeight = 77;

  // Center the hexagonous (adjusted to center the salmon)
  const centerX = tileWidth / 2 - salmonWidth / 2;
  const centerY = tileHeight / 2 - salmonHeight / 2;

  // Radius from hexagonous center to salmons
  const radius = 60;

  if (totalSalmons === 1) {
    return {
      left: `${centerX}px`,
      top: `${centerY}px`
    };
  }

  // For 2 or more, use different figures
  let angle = 0;
  let x:number, y:number;

  switch (totalSalmons) {
    case 2: {
      // 2 salmons: one smoothly to the left, the other to the right, in the vertical center
      const offset = 40; // horizontal separation
      x = tileWidth / 2 + (index === 0 ? -offset : +offset) - salmonWidth / 2;
      y = tileHeight / 2 - salmonHeight / 2;
      break;
    }

    case 3: {
      // Triangle: angles 0°, 120°, 240° (starting from the top)
      angle = (index * 2 * Math.PI) / 3 - Math.PI / 2; // -PI/2 to start from the top
      x = tileWidth / 2 + radius * Math.cos(angle) - salmonWidth / 2;
      y = tileHeight / 2 + radius * Math.sin(angle) - salmonHeight / 2;
      break;
    }

    case 4: {
      // Square: angles 45°, 135°, 225°, 315° (rotated so it's not in a cross)
      angle = (index * 2 * Math.PI) / 4 + Math.PI / 4; // +PI/4 to start from a corner
      x = tileWidth / 2 + radius * Math.cos(angle) - salmonWidth / 2;
      y = tileHeight / 2 + radius * Math.sin(angle) - salmonHeight / 2;
      break;
    }

    case 5: {
      // Pentagonous: 5 points, each 72°, starting from the top
      angle = (index * 2 * Math.PI) / 5 - Math.PI / 2; // start from the top
      x = tileWidth / 2 + radius * Math.cos(angle) - salmonWidth / 2;
      y = tileHeight / 2 + radius * Math.sin(angle) - salmonHeight / 2;
      break;
    }

    default: {
      x = centerX;
      y = centerY;
    }
  }

  return {
    left: `${x}px`,
    top: `${y}px`
  };
};