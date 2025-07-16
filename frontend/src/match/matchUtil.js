import { ColorToRgb } from "../util/ColorParser";
import { patch } from "../util/fetchers";
import '../static/css/game/game.css'

export const handleTileClick = (tile, myPlayer, match, setSelectedTile, setSelectedSalmon) => {
    if (myPlayer.id === match.actualPlayerId && match.phase === 'TILES') {
        setSelectedTile(tile);
        setSelectedSalmon(null);
    }
}

export const handleRotateTile = async (tile, jwt) => {
    try {
        const newOrientation = (tile.orientation + 1) % 7; // Incrementa la rotación
        const response = await patch(`/api/v1/matchTiles/${tile.id}/rotation`, jwt, newOrientation)
        if (!response.ok) {
            throw new Error('Error updating tile rotation');
        }
    } catch (error) {
        console.error('Error rotating tile:', error);
    }
};

export const getRotationStyle = (tile) => {
    if (!tile) {
        return {};
    }
    // Puedes usar la propiedad `orientation` de cada tile
    return {
        transform: `rotate(${tile.orientation * 60}deg)` // Si orientation va de 0 a 6, rota en incrementos de 60 grados
    };
};

export const generatePlayerList = (players, actualPlayerId) => {
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

export const calculateSalmonPosition = (index, totalSalmons) => {
  const tileWidth = 250;
  const tileHeight = 217;
  const salmonWidth = 80;
  const salmonHeight = 77;
  const centerX = tileWidth / 2 - salmonWidth / 2; // para centrar salmones
  const centerY = tileHeight / 2 - salmonHeight / 2;
  if (totalSalmons === 1) {
    return {
      left: `${centerX}px`,
      top: `${centerY}px`
    };
  }
  // Para 6 aristas, ángulos en hexágono (empezando arriba): 0, 60, 120, 180, 240, 300
  const maxPositions = 6;
  const radius = 80; // distancia del centro a la arista donde estará el salmón
  const angleStep = (2 * Math.PI) / maxPositions;
  const angle = index * angleStep - Math.PI / 2; // desde arriba (270°)
  const x = tileWidth / 2 + radius * Math.cos(angle) - salmonWidth / 2;
  const y = tileHeight / 2 + radius * Math.sin(angle) - salmonHeight / 2;
  return {
    left: `${x}px`,
    top: `${y}px`
  };
};

export const updateSalmonPosition = async(salmon,x,y, jwt) => {
    try {
        console.log("x: ", x, "y: ", y)
        const responseSalmon = await patch(`/api/v1/salmonMatches/coordinate/${salmon.id}`, jwt, {x,y});
        if (!responseSalmon.ok) {
            const errorData = await responseSalmon.json(); // Parsea el cuerpo de la respuesta
            alert(errorData.error || "Movimiento no válido."); // Usa el mensaje del backend o un mensaje por defecto
            console.log("Error updating salmon:", errorData);
        }            
    } catch (error){
        console.log("Error updating salmon", error)
        throw error.message;
    }
};

export const changephase = async(match, jwt) => {
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
    
export const updateSpawn = async(salmon, jwt) => {
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

export const updateTilePosition = async (tile, x, y, jwt) => {
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

export const handleGridClick = async (jwt, index, selectedSalmon, setSelectedTile, selectedTile, gridTiles, setSelectedSalmon, match) => {
    const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
    const gridHeight = 6; // Altura de la cuadrícula (número de filas)
    const x = index % gridWidth;
    const y = gridHeight - 1 - Math.floor(index / gridWidth)
    try {
        if (selectedSalmon === null) {
            await updateTilePosition(selectedTile, x, y, jwt);
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