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

        if (!allDataLoaded) {
            return <div style={{justifySelf:'center'}}>Loading data</div>;
        }

        const playerList = players.map((p) => {
            return (
                <tr key = {p.id} className="table-row">
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.name}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.color}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.puntos}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.vivo? <i className="fa fa-check-circle"></i> : <i className="fa fa-times-circle"></i>}</td>
                </tr>
            );
        })

        /*const handleTileClick = (tile) => {
            setSelectedTile(tile);
            /*const index = tilesAndImages.indexOf(tile);
            const matchTilesWithImagesCopy = [...tilesAndImages];
            if (index > -1) {
                matchTilesWithImagesCopy.splice(index, 1);
                setTilesAndImages(matchTilesWithImagesCopy);
            }
        }*/

        const handleTileClick = (tile) => {
                if (myPlayer.id === match.actualPlayer) {
                    setSelectedTile(tile);

                }
        }

        const updateTilePosition = (tile, x, y) => {
            fetch(`/api/v1/matchTiles/${tile[0].id}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${jwt}`
                },
                body: JSON.stringify({ x, y })
            })
            .then(response => 
                    console.log(response.json()))
            .then(updatedTile => {
                console.log(updatedTile);
                const tileWithImage = tilesAndImages.find(t => t[0].id === tile.id);
                setTilesAndImages(prevTiles =>
                    prevTiles.map(t => (t[0].id === tile.id ? tileWithImage : t))
                );
                
            })
            .catch(error => console.error('Error updating tile position:', error));
        };

        const handleGridClick = (index) => {
            if (selectedTile) {
                const newGrid = [...grid];
                    newGrid[index] = selectedTile;
                    setGrid(newGrid);
                    setSelectedTile(null);
                    const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
                    const gridHeight = 6; // Altura de la cuadrícula (número de filas)
                    
                    // Calcular la coordenada x (columna) y y (fila) con filas invertidas
                    const x = index % gridWidth; // Coordenada x (columna)
                    const y = gridHeight - 1 - Math.floor(index / gridWidth); // Coordenada y invertida (fila)
    
                updateTilePosition(selectedTile, x, y); // Actualizar posición en el servidor
    
                // Reiniciar la casilla seleccionada después de moverla
                setSelectedTile(null);
                let nextPlayer = players[myPlayer.playerOrder+1];
                console.log(nextPlayer)
                console.log(players)
                if(!nextPlayer){
                    nextPlayer = players[0];
                }
                fetch(`/api/v1/matches/${match.id}/actualPlayer/${nextPlayer.id}`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwt}`
                    }
                }).then(response => response.json())
            }    
            
        };

        return(
            <div className='gamePage-container'>
                <h1 style={{
                    position: 'absolute',
                    marginBottom: '10px',
                    marginTop: '10px',
                    marginLeft: '1208px',
                    fontSize: '30px',
                    }}>Game: {match.name}</h1>
                <h1 style={{
                    position: 'absolute',
                    marginBottom: '10px',
                    marginTop: '10px',
                    marginLeft: '552px',
                    fontSize: '30px',
                    }}>Round: {match.round}</h1>
                <h1 style={{
                    position: 'absolute',
                    marginBottom: '10px',
                    marginTop: '10px',
                    marginLeft: '830px',
                    fontSize: '30px',
                    }}>Phase: {match.phase}</h1>
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
                        src={tilesAndImages[0][1]} alt='' style={{width: '150px'}}></img>}
                </div>
                }
                <div className='game-container'>
                    <div className='grid1'>
                    {grid.map((tile, index) => (
                        <div key={index} onClick={() => handleGridClick(index)} className="grid-item"> 
                            {tile ? <img src={tile[1]} alt="Grid Tile" style={tile[1]===seaTile ? {width:'400px'} : { width: '150px' }} /> : null}
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