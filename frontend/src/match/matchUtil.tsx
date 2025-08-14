import { ColorToRgb } from "../util/ColorParser.ts";
import { patch } from "../util/fetchers.ts";
import '../static/css/game/game.css'
import MatchTile from '../interfaces/MatchTile.ts'
import Player from '../interfaces/Player.ts'
import Match from "../interfaces/Match.ts";
import SalmonMatch from "../interfaces/SalmonMatch.ts";

export const handleTileClick = (tile:MatchTile, myPlayer:Player, match:Match, setSelectedTile:Function, setSelectedSalmon:Function) => {
    if (myPlayer.id === match.actualPlayerId && match.phase === 'TILES') {
        setSelectedTile(tile);
        setSelectedSalmon(null);
    }
}

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
    if (!tile) {
        return {};
    }
    return {
        transform: `rotate(${tile.orientation * 60}deg)` // Si orientation va de 0 a 6, rota en incrementos de 60 grados
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
  let x, y;

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

export const updateSalmonPosition = async(salmon:SalmonMatch, x:number, y:number, jwt:string) => {
    try {
        const responseSalmon = await patch(`/api/v1/salmonMatches/coordinate/${salmon.id}`, jwt, {x,y});
        if (!responseSalmon.ok) {
            const errorData = await responseSalmon.json();
            alert(errorData.error || "Movimiento no válido."); // Usa el mensaje del backend o un mensaje por defecto
            console.log("Error updating salmon:", errorData);
        }            
    } catch (error){
        console.log("Error updating salmon", error)
        throw error.message;
    }
};

export const changephase = async(match:Match, jwt:string) => {
    try {
        const responseChangePhase = await patch(`/api/v1/matches/${match.id}/changephase/${match.actualPlayerId}`, jwt)
        if (!responseChangePhase.ok) {
            const errorData = await responseChangePhase.json();
            alert(errorData.message || "Error changing phase.");
            console.log("Error changing phase.", errorData);
        }            
    } catch (error){
        console.log("Error changing phase.", error)
        throw error.message;
    }
};
    
export const updateSpawn = async(salmon:SalmonMatch, jwt:string) => {
    try {
        const responseSalmon = await patch(`/api/v1/salmonMatches/enterSpawn/${salmon.id}`, jwt);
        if (!responseSalmon.ok) {
            alert("Movimiento no válido.");
            console.log("Error actualizando salmon", responseSalmon)
        }
    } catch (error){
        console.log("Error actualizando salmon", error)
        throw error.message;
    }
};

export const updateTilePosition = async (tile:MatchTile, x:number, y:number, jwt:string) => {
    try {
        const response = await patch(`/api/v1/matchTiles/${tile.id}`, jwt, {x,y});
        if (!response.ok) {
            throw new Error('Invalid tile placement');
        }
    } catch (error) {
        console.error('Error updating tile position:', error);
        throw error; 
    }
};

export const handleGridClick = async (jwt:string, index:number, selectedSalmon:SalmonMatch|null, setSelectedTile:Function, selectedTile:MatchTile|null, gridTiles:MatchTile[], setSelectedSalmon:Function, match:Match) => {
    const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
    const gridHeight = 6; // Altura de la cuadrícula (número de filas)
    const x = index % gridWidth;
    const y = gridHeight - 1 - Math.floor(index / gridWidth)
    try {
        if (selectedSalmon === null) {
            if (selectedTile) await updateTilePosition(selectedTile, x, y, jwt);
            setSelectedTile(null);
        }
        else {
            const foundTile = gridTiles.find(tile => tile.coordinate?.x === x && tile.coordinate?.y === y);
            if (foundTile) {    
                await updateSalmonPosition(selectedSalmon, x, y, jwt);
                setSelectedSalmon(null);
            } else if(x === 1 && y === 5 && match.round >= 6){
                await updateSpawn(selectedSalmon, jwt);        
                setSelectedSalmon(null);
            }
        }
            await changephase(match, jwt);
        }
        catch (error) {
            console.error("Error updating tile position or advancing turn:", error);
        }
};