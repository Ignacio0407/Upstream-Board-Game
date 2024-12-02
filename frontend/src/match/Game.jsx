    import React, { useState, useEffect } from 'react';
    import tokenService from '../services/token.service'
    import useFetchState from "../util/useFetchState";
    import '../static/css/game/game.css'
    import bearTile from '../static/images/tiles/bearTile.png'
    import eagleTile from '../static/images/tiles/eagleTile.png'
    import heronTile from '../static/images/tiles/heronTile.png'
    import jumpTile from '../static/images/tiles/jumpTile.png'
    import rockTile from '../static/images/tiles/rockTile.png'
    import seaTile from '../static/images/tiles/seaTile.png'
    import waterTile from '../static/images/tiles/waterTile.png'

    function Game({match}){
        const jwt = tokenService.getLocalAccessToken();
        const user = tokenService.getUser();
        const [players, setPlayers] = useFetchState([],`/api/v1/matches/${match.id}/players`,jwt);
        const [tilesList,setTilesList] = useFetchState([], '/api/v1/tiles',jwt); // Siempre igual
        const [matchTiles, setMatchTiles] = useFetchState([], `/api/v1/matchTiles/${match.id}`,jwt) // Todos los front tienen las mismas tiles
        const [allDataLoaded, setAllDataLoaded] = useState(false);
        const [tilesAndImages, setTilesAndImages] = useState([]);
        const [gridTiles, setGridTiles] = useState([]);
        const [selectedTile, setSelectedTile] = useState(null);
        const [grid, setGrid] = useState(Array(18).fill(null).reverse());
        const [myPlayer, setMyPlayer] = useState(null);

        const getImage = (tileP) => {
            if (!tileP) return null;  // Casilla vacia
            const realTile = tilesList[tileP.tile-1]
            switch (realTile.type) {
                case 'OSO':
                    return bearTile;
                case 'AGUILA':
                    return eagleTile;
                case 'GARZA':
                    return heronTile;
                case 'SALTO':
                    return jumpTile;
                case 'PIEDRA':
                    return rockTile;
                case 'AGUA':
                    return waterTile;
                default:
                    return null;
            }
        }

        useEffect(() => {
            if (players.length > 0 && tilesList.length > 0 && matchTiles.length > 0) {
                console.log("players", players)
                console.log("tilesList ", tilesList)
                console.log("matchTiles", matchTiles)
                setAllDataLoaded(true);
                const matchTilesCopy = [...matchTiles].filter(mT => mT.coordinate === null).map((t) => [t,getImage(t)])
                const matchTilesCopy2 = [...matchTiles].filter(mT => mT.coordinate !== null).map((t) => [t,getImage(t)])
                setTilesAndImages(matchTilesCopy)
                setGridTiles(matchTilesCopy2)
                const orderedPlayers = [...players].sort(p => p.playerOrder)
                setPlayers(orderedPlayers) // Siempre igual
                setMyPlayer(players.filter(p => p.userPlayer === user.id)[0]);
            }
        }, [tilesList, matchTiles]);

        useEffect(() => {
            // Construir el nuevo estado del grid basado en gridTiles
            const newGrid = Array(18).fill(null); // Crea una cuadrícula vacía de 18 espacios (ajustar si es necesario)
            const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
            const gridHeight = 6; // Altura de la cuadrícula (número de filas)
            // Asignar las tiles con coordenadas al grid
            gridTiles.forEach((tile) => {
                // Convertir las coordenadas (x, y) en un índice del grid
                const index = (gridHeight - 1 - tile[0].coordinate.y) * gridWidth + tile[0].coordinate.x; // Suponiendo que el grid tiene 3 columnas (ajustar según sea necesario)
                const image = tile[1]; // Obtener la imagen asociada al tile
                newGrid[index] = [tile, image]; // Asignar la tile con su imagen al grid
            });
        
            // Actualizar el estado del grid
            setGrid(newGrid);
            console.log(grid)
        }, [gridTiles]);

        useEffect(() => {
            const interval = setInterval(() => {
                fetch(`/api/v1/matchTiles/${match.id}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwt}`
                    },
                })
                .then(response => response.json())
                .then(data => setMatchTiles(data))
                .catch(error => console.error('Error fetching tiles:', error));
            }, 1000); // Cada 5 segundos
            return () => clearInterval(interval);
        }, [jwt]);

        if (!allDataLoaded) {
            return <div style={{justifySelf:'center'}}>Loading data</div>;
        }



        const playerList = players.map((p) => {
            return (
                <tr key = {p.id} className="table-row">
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.name}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.color}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.points}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.alive? <i className="fa fa-check-circle"></i> : <i className="fa fa-times-circle"></i>}</td>
                </tr>
            );
        })

        const handleTileClick = (tile) => {
                if (myPlayer.id === match.actualPlayer) {
                    setSelectedTile(tile);
                }
        }

        const updateTilePosition = async (tile, x, y) => {
            try {
                const response = await fetch(`/api/v1/matchTiles/${tile[0].id}`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwt}`
                    },
                    body: JSON.stringify({ x, y })
                });
        
                if (!response.ok) {
                    throw new Error('Invalid tile placement');
                }
        
                const updatedTile = await response.json();
                console.log(updatedTile);
        
                const tileWithImage = tilesAndImages.find(t => t[0].id === tile.id);
                setTilesAndImages(prevTiles =>
                    prevTiles.map(t => (t[0].id === tile.id ? tileWithImage : t))
                );
        
                return updatedTile; 
            } catch (error) {
                console.error('Error updating tile position:', error);
                throw error; 
            }
        };

        // Actualiza el grid una vez está seleccionada la casilla
        const handleGridClick = async (index) => {
    if (selectedTile) {
        const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
        const gridHeight = 6; // Altura de la cuadrícula (número de filas)

        // Calcular la coordenada x (columna) y y (fila) con filas invertidas
        const x = index % gridWidth; // Coordenada x (columna)
        const y = gridHeight - 1 - Math.floor(index / gridWidth); // Coordenada y invertida (fila)

        try {
            // Intentar actualizar la posición de la tile
            await updateTilePosition(selectedTile, x, y);

            // Reiniciar la casilla seleccionada después de moverla
            setSelectedTile(null);

            // Determinar el siguiente jugador
            let nextPlayer = players[myPlayer.playerOrder + 1];
            if (!nextPlayer) {
                nextPlayer = players[0]; // Volver al primer jugador si se termina la lista
            }

            // Actualizar el turno en el servidor
            await fetch(`/api/v1/matches/${match.id}/actualPlayer/${nextPlayer.id}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${jwt}`
                }
            });

        } catch (error) {
            console.error("Error updating tile position or advancing turn:", error);
            // Detener ejecución si ocurre un error
            return;
        }
    }
};

    const handleRotateTile = async (tile) => {
        try {
            const newOrientation = (tile[0].orientation + 1) % 7; // Incrementa la rotación

            const response = await fetch(`/api/v1/matchTiles/${tile[0].id}/rotation`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${jwt}`
                },
                body: JSON.stringify(newOrientation)
            });

            if (!response.ok) {
                throw new Error('Error updating tile rotation');
                
            }
            console.log(response);

        } catch (error) {
            console.error('Error rotating tile:', error);
        }
    };

    const getRotationStyle = (tile) => {
        // Puedes usar la propiedad `orientation` de cada tile
        return {
            transform: `rotate(${tile.orientation * 60}deg)` // Si orientation va de 0 a 6, rota en incrementos de 60 grados
        };
    };


        return(
            <div className='gamePage-container'>
                <h1 class="game-title game-name">Game: {match.name}</h1>
                <h1 class="game-title game-round">Round: {match.round}</h1>
                <h1 class="game-title game-phase">Phase: {match.phase}</h1>
                <div className="users-table">
                    <thead>
                        <tr>
                            <th className="table-row" style={{position: 'relative', padding: '20px'}}>Name</th>
                            <th className="table-row" style={{position: 'relative', padding: '20px'}}>Color</th>
                            <th className="table-row" style={{position: 'relative', padding: '20px'}}>Points</th>
                            <th className="table-row" style={{position: 'relative', padding: '20px'}}>Alive</th>
                        </tr>
                    </thead>
                    <tbody>{playerList}</tbody>
                </div>
                
                {tilesAndImages.length > 0 &&
                
                <div key={tilesAndImages[0][0].id}
                    style={{
                        cursor: 'pointer',
                        position: 'absolute',
                        bottom: '-900px',
                        right: '20px'
                    }}
                    onClick={() => handleTileClick(tilesAndImages[0])}>
                        {myPlayer.id === match.actualPlayer && <h2>Pick the tile!</h2>}
                        <h2>Next tile:</h2>
                        {<img 
                        onClick={() => handleTileClick(tilesAndImages[0])}
                        src={tilesAndImages[0][1]} alt='' style={{
                            width: '150px',
                            ...getRotationStyle(tilesAndImages[0][0])} 
                            }></img>
                        }
                        {myPlayer.id ===match.actualPlayer && 
                        (tilesList[tilesAndImages[0][0].tile-1].type === 'OSO' || 
                            tilesList[tilesAndImages[0][0].tile-1].type === 'SALTO')
                        && <button onClick={() => handleRotateTile(tilesAndImages[0])}>Rotate Tile</button>}
                </div>
                }
                <div className='game-container'>
                    <div className='grid1'>
                    {grid.map((tile, index) => (
                        <div key={index} onClick={() => handleGridClick(index)} className="grid-item"> 
                            {tile ? <img src={tile[1]} alt="Grid Tile" style={tile[1]===seaTile ? {width:'400px'} : { width: '150px', ...getRotationStyle(tile[0][0])}} /> : null}
                        </div>
                    ))}
                    </div>
                    <div className='grid2'>
                        
                    </div>
                </div>
            </div>
        )

    }

export default Game;