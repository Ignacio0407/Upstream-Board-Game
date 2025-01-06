import { ColorToRgb } from "../util/ColorParser";
import { patch } from "../util/fetchers";

export const getTileImage = (tileP, tilesList, images) => {
    if (!tileP) return null;
    const realTile = tilesList[tileP.tile - 1];
    switch(realTile.type) {
        case 'OSO':
            return images.bearTile;
        case 'AGUILA':
            return images.eagleTile;
        case 'GARZA':
            return images.heronTile;
        case 'SALTO':
            return images.jumpTile;
        case 'PIEDRA':
            return images.rockTile;
        case 'AGUA':
            return images.waterTile;
        default:
            return null;
    }
}

export const getSalmonImage = (salmonTile, players, images) => {
    if (!salmonTile) return null;  // Casilla vacia
            const color = players.filter(p => p.id === salmonTile.player)[0].color;
            const salmonN = salmonTile.salmonsNumber;
            switch(color){
                case 'AMARILLO':
                    return salmonN === 1? images.amarillo1 : images.amarillo2;
                case 'BLANCO':
                    return salmonN === 1? images.blanco1 : images.blanco2;
                case 'MORADO':
                    return salmonN === 1? images.morado1 : images.morado2;
                case 'ROJO':
                    return salmonN === 1? images.rojo1 : images.rojo2;
                case 'VERDE':
                    return salmonN === 1? images.verde1 : images.verde2;
                default:
                    return null;
            }
}

export const handleTileClick = (tile, myPlayer, match, setSelectedTile, setSelectedSalmon) => {
    if (myPlayer.id === match.actualPlayer && match.phase === 'CASILLAS') {
        setSelectedTile(tile);
        setSelectedSalmon(null);
        console.log('selected', tile)
    }
}

export const handleRotateTile = async (tile, jwt) => {
    try {
        const newOrientation = (tile[0].orientation + 1) % 7; // Incrementa la rotación
        console.log(newOrientation);

        const response = await fetch(`/api/v1/matchTiles/${tile[0].id}/rotation`, {
            method: "PATCH",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(newOrientation),
        });

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
    // Puedes usar la propiedad `orientation` de cada tile
    return {
        transform: `rotate(${tile.orientation * 60}deg)` // Si orientation va de 0 a 6, rota en incrementos de 60 grados
    };
};

export const generatePlayerList = (players) => {
    return players.map((p) => (
        <tr key={p.id} className="table-row">
            <td className="table-cell" style={{ position: 'relative', padding: '20px' }}>{p.name}</td>
            <td className="table-cell" style={{
            padding: '0', // Elimina el padding del `<td>`
            textAlign: 'center', // Centra el contenido en el `<td>`
            verticalAlign: 'middle',
                }}>
                <div style={{
                    width: '30px',
                    height: '30px',
                    borderRadius: '50%',
                    backgroundColor: `${ColorToRgb(p.color)}`,
                    border: '1px solid #000',
                    display: 'inline-block', 
                }}>
                    
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