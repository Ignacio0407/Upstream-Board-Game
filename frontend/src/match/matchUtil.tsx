import { ColorToRgb } from "../util/ColorParser.ts";
import { get, patch, put } from "../util/fetchers.ts";
import '../static/css/game/game.css'
import MatchTile, { emptyTile } from '../interfaces/MatchTile.ts'
import Player from '../interfaces/Player.ts'
import Match from "../interfaces/Match.ts";
import SalmonMatch from "../interfaces/SalmonMatch.ts";
import { JSX } from "react";
import { TileType } from "../interfaces/Tile.ts";

export const GRID_WIDTH = 3;
export const GRID_HEIGHT = 6;
export const coordToIndex = (x: number, y: number) => { return (GRID_HEIGHT - 1 - y) * GRID_WIDTH + x; };
export type GridCell = {tile: MatchTile, salmons: SalmonMatch[]};

function isFull (playersNumber:number, tile:MatchTile) {
    if (tile.capacity === 0) return false;
    if (playersNumber > 2 && (tile.tile.type === TileType.ROCK)) return tile.capacity === (tile.salmonsNumber - 1);
    else return tile.capacity === tile.salmonsNumber; 
}

export const handleTileClick = (tile:MatchTile, myPlayer:Player, match:Match, setSelectedTile:Function, setSelectedSalmon:Function) => {
    if (myPlayer.id === match.actualPlayerId && match.phase === 'TILES') {
        setSelectedTile(tile);
        setSelectedSalmon(null);
    }
}

export const handleSalmonClick = (salmon: SalmonMatch, myPlayer:Player|null, match:Match, setSelectedSalmon:Function) => {
    if (myPlayer) {
        if (myPlayer.id === match.actualPlayerId && myPlayer.id === salmon.playerId && match.phase === 'MOVING')
            setSelectedSalmon(salmon);
    }
}

export const updateSalmonPosition = async(salmon:SalmonMatch, x:number, y:number, jwt:string, match:Match, actualPlayerId:number|undefined, myPlayerId:number|undefined) => {
    if (match.phase === "MOVING" && actualPlayerId === myPlayerId) {
        try {
        const response = await patch(`/api/v1/salmonMatches/coordinate/${salmon.id}`, jwt, {x,y});
        if (!response.ok) {
            const errorData = await response.json();
            if (errorData.error) alert(errorData.error) 
            else alert(errorData.message);
            console.log("Error updating salmon:", errorData);
        }
        return response;
    } catch (error:any) {
        console.log("Error updating salmon from catch.", error)
        throw error.message;
    }
    }
    else alert("Now it's tiles placing phase!")
};

export const changephase = async(match:Match, jwt:string, myPlayerId:number) => {
    try {
        const responseChangePhase = await patch(`/api/v1/matches/${match.id}/changephase/${myPlayerId}`, jwt)
        if (!responseChangePhase.ok) {
            const errorData = await responseChangePhase.json();
            alert(errorData.error);
            console.log("Error changing phase.", errorData);
        }            
    } catch (error:any) {
        console.log("Error changing phase from catch.", error)
        throw error.message;
    }
};

export const passTurn = (match:Match, jwt:string, myPlayer:Player) => {
    return (<button onClick={() => {
        myPlayer.energy = myPlayer.energy -1;
        put(`/api/v1/players/${myPlayer.id}`, jwt, myPlayer)
        changephase(match, jwt, myPlayer.id)
     }}>Pass Turn</button>)
};
    
export const enterSpawn = async(salmon:SalmonMatch, jwt:string) => {
    try {
        const response = await patch(`/api/v1/salmonMatches/enterSpawn/${salmon.id}`, jwt);
        if (!response.ok) {
            const errorData = await response.json();
            alert(errorData.error);
            console.log("Error updating salmon:", errorData);
        }
        return response;
    } catch (error:any) {
        console.log("Error updating salmon from catch:", error)
        throw error.message;
    }
};

export const updateTilePosition = async (tile:MatchTile, x:number, y:number, jwt:string, actualPlayerId:number|undefined, myPlayerId:number, match:Match) => {
    if (match.phase === "TILES" && actualPlayerId === myPlayerId) {
        try {
        const response = await patch(`/api/v1/matchTiles/${tile.id}`, jwt, {actualPlayerId, myPlayerId, x,y});
        if (!response.ok) {
            const errorData = await response.json();
            alert(errorData.message);
            console.log("Error positioning tile:", errorData);
        }
        return response;
    } catch (error) {
        console.error('Error updating tile position:', error);
        throw error; 
    }
    }
    else alert("Now it's salmon moving phase!")
};

export const handleGridClick = async (jwt:string, index:number, selectedSalmon:SalmonMatch|null, setSelectedTile:Function, selectedTile:MatchTile|null, setSelectedSalmon:Function, match:Match, actualPlayerId:number|undefined, myPlayerId:number, grid:GridCell[]) => {
    const x = index % GRID_WIDTH;
    const y = GRID_HEIGHT - 1 - Math.floor(index / GRID_WIDTH);
    let updateTilePositionResponse = null;
    let updateSalmonPositionResponse = null;
    let updateSpawnReponse = null;
    let confirmed;
    if (selectedSalmon === null) {
        if (selectedTile) updateTilePositionResponse = await updateTilePosition(selectedTile, x, y, jwt, actualPlayerId, myPlayerId, match);
        setSelectedTile(null);
    }
    else {
        const foundTileAndSalmons = grid[index]?.tile;
        if (isFull(match.playersNumber, foundTileAndSalmons)) {
            confirmed = window.confirm("This tile is full of salmons, you must spend 3 energy points to jump to the next one (If the next one is also full, this move will not be valid). Do you want to jump?");
            if (confirmed) {
                updateSalmonPositionResponse = await updateSalmonPosition(selectedSalmon, x, y, jwt, match, actualPlayerId, myPlayerId);
                setSelectedSalmon(null);
            }
        }
        else if(x === 1 && y === 5 && match.round >= 6) {
            updateSpawnReponse = await enterSpawn(selectedSalmon, jwt);        
            setSelectedSalmon(null);
        }
        else {
            updateSalmonPositionResponse = await updateSalmonPosition(selectedSalmon, x, y, jwt, match, actualPlayerId, myPlayerId);
            setSelectedSalmon(null);
        }
    }
    if (updateTilePositionResponse != null && updateTilePositionResponse.ok && match.phase === "TILES") await changephase(match, jwt, myPlayerId);
    else if (updateSalmonPositionResponse != null && updateSalmonPositionResponse.ok && match.phase === "MOVING") await changephase(match, jwt, myPlayerId);
    else if (updateSpawnReponse != null && updateSpawnReponse.ok && match.phase === "MOVING") await changephase(match, jwt, myPlayerId);
};

export const handleRotateTile = async (tile:MatchTile, jwt:string) => {
    try {
        const newOrientation = (tile.orientation + 1) % 7;
        const response = await patch(`/api/v1/matchTiles/${tile.id}/rotation`, jwt, newOrientation)
        if (!response.ok) {
            throw new Error('Error updating tile rotation');
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

export const fetchPlayers = async (match:Match, jwt:string, setPlayers:Function) => {
    const response = await get("/api/v1/players/match/"+match.id, jwt);
    const data = await response.json();
    setPlayers(data);
}

export function buildGridFromSnapshot(tiles: MatchTile[], salmons: SalmonMatch[]): GridCell[] {
    const grid: GridCell[] = Array(18).fill(null).map(() => ({
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

export const updateGridWithMovement = (prevGrid: GridCell[], salmon: SalmonMatch, myTile: MatchTile, toTravel: MatchTile): GridCell[] => {
  const newGrid = [...prevGrid];
  const fromIndex = coordToIndex(myTile.coordinate.x, myTile.coordinate.y);
  const toIndex = coordToIndex(toTravel.coordinate.x, toTravel.coordinate.y);
  // Remove salmon from current tile
  newGrid[fromIndex] = {...newGrid[fromIndex],
    tile: myTile,
    salmons: newGrid[fromIndex].salmons.filter(s => s.id !== salmon.id),
  };
  // Add salmon to new tile
    newGrid[toIndex] = {...newGrid[toIndex],
    tile: toTravel,
    salmons: salmon.salmonsNumber > 0 ? [...newGrid[toIndex].salmons, salmon] : newGrid[toIndex].salmons
    };
    console.log("myTile", newGrid[fromIndex])
    console.log("toTravel", newGrid[toIndex])
  return newGrid;
};

export const updateGridWithMovementFromSea = (prevGrid: GridCell[], salmon: SalmonMatch, toTravel: MatchTile): GridCell[] => {
  const newGrid = [...prevGrid];
  const toIndex = coordToIndex(toTravel.coordinate.x, toTravel.coordinate.y);
  // Add salmon to new tile
  if (salmon.salmonsNumber > 0) {
    newGrid[toIndex] = {
    ...newGrid[toIndex],
    tile: toTravel,
    salmons: [...newGrid[toIndex].salmons, salmon],
    };
  }
  return newGrid;
};

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

export const updateGridWithSalmons = (prevGrid: GridCell[], updatedSalmons: SalmonMatch[]): GridCell[] => {
  const updatedMap = new Map(updatedSalmons.map(s => [s.id, s]));

  return prevGrid.map(cell => {
    if (cell.salmons.length === 0) return cell;

    const newSalmons = cell.salmons
      .map(s => updatedMap.get(s.id) ?? s)
      .filter(s => updatedMap.has(s.id) || (s as any).salmonsNumber > 0); // ⚠️ revisa esta condición

    if (newSalmons === cell.salmons) {
      return cell;
    }

    return { ...cell, salmons: newSalmons };
  });
};

export const updateGridWithHerons=(prevGrid:GridCell[],updatedTiles:MatchTile[],updatedSalmons:SalmonMatch[],deletedIds?:number[]):GridCell[]=>{
 const newGrid=[...prevGrid];
 const salmonMap=new Map(updatedSalmons.map(s=>[s.id,s]));
 newGrid.forEach((cell,index)=>{
  if(cell.salmons.length===0) return;
  const newSalmons=cell.salmons.filter(s=>!deletedIds?.includes(s.id)).map(s=>salmonMap.get(s.id)??s);
  if(newSalmons!==cell.salmons)
   newGrid[index]={...cell,salmons:newSalmons};
 });
 updatedTiles.forEach(tile=>{
  if(!tile.coordinate)return;
  const {x,y}=tile.coordinate;
  const index=coordToIndex(x,y);
  newGrid[index]={...newGrid[index],tile};
 });
 return newGrid;
};

export const calculateSalmonPosition = (index:number, totalSalmons:number) => {
  const tileWidth = 250;
  const tileHeight = 217;
  const salmonWidth = 80;
  const salmonHeight = 77;

  // Centro del hexágono (ajustado para centrar el salmón)
  const centerX = tileWidth / 2 - salmonWidth / 2;
  const centerY = tileHeight / 2 - salmonHeight / 2;

  // Radio desde el centro del hexágono a los salmones
  const radius = 60; // Ajustable: radio de la "órbita" donde se ponen los salmones

  if (totalSalmons === 1) {
    return {
      left: `${centerX}px`,
      top: `${centerY}px`
    };
  }

  // Para 2 o más, usamos diferentes figuras
  let angle = 0;
  let x:number, y:number;

  switch (totalSalmons) {
    case 2: {
      // Dos salmones: uno ligeramente a la izquierda, otro a la derecha, en el centro vertical
      const offset = 40; // separación horizontal
      x = tileWidth / 2 + (index === 0 ? -offset : +offset) - salmonWidth / 2;
      y = tileHeight / 2 - salmonHeight / 2;
      break;
    }

    case 3: {
      // Triángulo: ángulos 0°, 120°, 240° (empezando desde arriba)
      angle = (index * 2 * Math.PI) / 3 - Math.PI / 2; // -PI/2 para empezar desde arriba
      x = tileWidth / 2 + radius * Math.cos(angle) - salmonWidth / 2;
      y = tileHeight / 2 + radius * Math.sin(angle) - salmonHeight / 2;
      break;
    }

    case 4: {
      // Cuadrado: ángulos 45°, 135°, 225°, 315° (rotado para que no esté en cruz)
      angle = (index * 2 * Math.PI) / 4 + Math.PI / 4; // +PI/4 para empezar en esquina
      x = tileWidth / 2 + radius * Math.cos(angle) - salmonWidth / 2;
      y = tileHeight / 2 + radius * Math.sin(angle) - salmonHeight / 2;
      break;
    }

    case 5: {
      // Pentágono: 5 puntos, cada 72°, empezando desde arriba
      angle = (index * 2 * Math.PI) / 5 - Math.PI / 2; // empezar desde arriba
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