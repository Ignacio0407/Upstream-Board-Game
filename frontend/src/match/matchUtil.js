import { ColorToRgb } from "../util/ColorParser";
import { patch } from "../util/fetchers";

export const getSalmonImage = (salmonTile, players, images) => {
    if (!salmonTile) return null;  // Casilla vacia
            const color = players.filter(p => p.id === salmonTile.playerId)[0].color;
            const salmonN = salmonTile.salmonsNumber;
            switch(color){
                case 'YELLOW':
                    return salmonN === 1? images.amarillo1 : images.amarillo2;
                case 'WHITE':
                    return salmonN === 1? images.blanco1 : images.blanco2;
                case 'PURPLE':
                    return salmonN === 1? images.morado1 : images.morado2;
                case 'RED':
                    return salmonN === 1? images.rojo1 : images.rojo2;
                case 'GREEN':
                    return salmonN === 1? images.verde1 : images.verde2;
                default:
                    return null;
            }
}

export const handleTileClick = (tile, myPlayer, match, setSelectedTile, setSelectedSalmon) => {
    if (myPlayer.id === match.actualPlayerId && match.phase === 'TILES') {
        setSelectedTile(tile);
        setSelectedSalmon(null);
        console.log('selected', tile)
    }
}

export const handleRotateTile = async (tile, jwt) => {
    try {
        const newOrientation = (tile.orientation + 1) % 7; // Incrementa la rotación
        console.log(newOrientation);

        const response = await patch(`/api/v1/matchTiles/${tile.id}/rotation`, jwt, newOrientation)

        if (!response.ok) {
            throw new Error('Error updating tile rotation');
        }

        const data = await response.json(); // Si la respuesta incluye un JSON
        console.log(data);

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
        <tr key={p.id} style={{
            backgroundColor: p.id === actualPlayerId ? 'rgba(0, 255, 0, 0.3) !important' : 'transparent',
        }}
        >
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
    const radius = 35; // Radio del pentágono
    const centerOffsetX = 90; // Centro X relativo a la casilla (mitad del ancho de la tile)
    const centerOffsetY = 30; // Centro Y relativo a la casilla (mitad del alto de la tile)
    
    // Si solo hay un salmón, colocarlo en el centro
    if (totalSalmons === 1) {
    return {
        left: `${centerOffsetX + 15}px`, // 25 es la mitad del ancho del salmón (50px/2)
        top: `${centerOffsetY + 10}px`
    };
    }

    // Calcular el ángulo para cada posición
    const angleStep = (2 * Math.PI) / totalSalmons // Usar 5 posiciones como máximo
    const angle = index * angleStep - Math.PI / 2; // Empezar desde arriba (-90 grados)

    // Calcular las coordenadas X e Y en el hexagono
    const x = centerOffsetX + radius * Math.cos(angle) + 10;
    const y = centerOffsetY + radius * Math.sin(angle) + 10;

    return {
    left: `${x}px`,
    top: `${y}px`
    };
};