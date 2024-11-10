    import React, { useState, useEffect } from 'react';
    import tokenService from '../services/token.service'
    import useFetchState from "../util/useFetchState";
    import '../static/css/game/game.css'
    import { ColorToRgb } from '../util/ColorParser';
    import bearTile from '../static/images/tiles/bearTile.png'
    import eagleTile from '../static/images/tiles/eagleTile.png'
    import heronTile from '../static/images/tiles/heronTile.png'
    import jumpTile from '../static/images/tiles/jumpTile.png'
    import rockTile from '../static/images/tiles/rockTile.png'
    import seaTile from '../static/images/tiles/seaTile.png'
    import spawningTile from '../static/images/tiles/spawningTile.png'
    import waterTile from '../static/images/tiles/waterTile.png'

    function Game({match}){
        const jwt = tokenService.getLocalAccessToken();
        const [players,setPlayers] = useFetchState([],`/api/v1/matches/${match.id}/players`,jwt) // Tenemos los jugadores de la partida
        const [tilesList,setTilesList] = useFetchState([], '/api/v1/casilla',jwt)
        const [matchTiles, setMatchTiles] = useFetchState([], `/api/v1/matchTiles/${match.id}`,jwt)
        const matchTilesNotC= matchTiles.filter(mT => mT.coordinate === null);
        //const playerList = players.map(player => <div color= {ColorToRgb(player.color)}></div>)
        /** const matchTilesWithoutSeaOrSpawn = matchTiles.filter(mT => tilesList[mT.tile-1].tipo !== 'MAR' && tilesList[mT.tile-1].tipo !== 'DESOVE') // Quitar las casillas de mar y desove;  
        matchTilesWithoutSeaOrSpawn.sort(() => Math.random() - 0.5) */
        const [selectedTile, setSelectedTile] = useState(null);
        const [grid, setGrid] = useState(Array(20).fill(null));
        const [matchTilesWithImages, setTilesWithImages] = useState([]);
        const [isReady, setIsReady] = useState(false);

        console.log("TILES ",tilesList);    
        console.log("MATCH TILES ", matchTiles);
       // console.log("MATCH TILES WITHOUT SEA OR SPAWN ", matchTilesWithoutSeaOrSpawn);
        console.log("MATCH", match);
        console.log("PLAYERS", players);
        
        
        const playerList =
        players.map((p) => {
            return (
                <tr key = {p.id} className="table-row">
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.name}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.color}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.puntos}</td>
                    <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.vivo? <i className="fa fa-check-circle"></i> : <i className="fa fa-times-circle"></i>}</td>
                </tr>
            );
        })

        const handleTileClick = (tile) => {
            setSelectedTile(tile);
            const index = matchTilesWithImages.indexOf(tile);
            const matchTilesWithImagesCopy = [...matchTilesWithImages];
            if (index > -1) {
                matchTilesWithImagesCopy.splice(index, 1);
                setTilesWithImages(matchTilesWithImagesCopy);
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
                const tileWithImage = matchTilesWithImages.find(t => t[0].id === tile.id);
                setTilesWithImages(prevTiles =>
                    prevTiles.map(t => (t[0].id === tile.id ? tileWithImage : t))
                );
                
            })
            .catch(error => console.error('Error updating tile position:', error));
        };
        

        const handleGridClick = (index) => {
            if (selectedTile) {
                const gridWidth = 3; // Definir el ancho de la cuadrícula (por ejemplo, 5 columnas)
                const x = index % gridWidth; // Coordenada x (columna)
                const y = Math.floor(grid.length / gridWidth) - 1 - Math.floor(index / gridWidth);
    
                updateTilePosition(selectedTile, x, y); // Actualizar posición en el servidor
    
                // Reiniciar la casilla seleccionada después de moverla
                setSelectedTile(null);
            }
            
            if(index !== 18 && index !== 19) {
                if (selectedTile) {
                    const newGrid = [...grid];
                    newGrid[index] = selectedTile;
                    setGrid(newGrid);
                    setSelectedTile(null);
                }
            }
                 
                
        };

        
        const getImage = (tileP) => {
            if (!tileP) return null;  // Casilla vacia
            const realTile = tilesList[tileP.tile-1]
            switch (realTile.tipo) {
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
                case 'MAR':
                    return seaTile;
                case 'DESOVE':
                    return spawningTile;
                case 'AGUA':
                    return waterTile;
                default:
                    return null;
            }
        }   
    

        useEffect(() => {


            const timer = setTimeout(() => {
                setIsReady(true);
                
            }, 500);
            

            setTilesWithImages(
                matchTilesNotC.map((tile) => [tile, getImage(tile)])
            );
            const newGrid = [...grid];
        const gridWidth = 5; // Suponiendo una cuadrícula de 5 columnas
        const centerIndex = newGrid.length - Math.ceil(gridWidth / 2); // Calcula el centro de la fila inferior
        newGrid[19] = [null, seaTile]; // Usamos `bearTile` o cualquier otra imagen
        setGrid(newGrid);
            console.log("TILES WITH IMAGES ", matchTilesWithImages);
            // Limpiar el temporizador cuando el componente se desmonte
            return () => clearTimeout(timer);
        }, [matchTiles]);
        

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
                    }}>Round: {match.ronda}</h1>
                <h1 style={{
                    position: 'absolute',
                    marginBottom: '10px',
                    marginTop: '10px',
                    marginLeft: '830px',
                    fontSize: '30px',
                    }}>Phase: {match.fase}</h1>
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
                {matchTilesWithImages.length > 0 &&
                <div key={matchTilesWithImages[0]}
                    style={{
                        cursor: 'pointer',
                        position: 'absolute',
                        bottom: '-900px',
                        right: '20px'
                    }}
                    onClick={() => handleTileClick(matchTilesWithImages[0])}>
                        <h2>Next tile:</h2>
                        {<img 
                        onClick={() => handleTileClick(matchTilesWithImages[0])}
                        src={matchTilesWithImages[0][1]} alt='' style={{width: '150px'}}></img>}
                </div>
                }
                <div className="game-container">
                    {grid.map((tile, index) => (
                        <div key={index} onClick={() => handleGridClick(index)} className={index !== 18 ? "grid-item":"grid-item18"}> 
                            {tile ? <img src={tile[1]} alt="Grid Tile" style={tile[1]===seaTile ? {width:'400px'} : { width: '150px' }} /> : null}
                        </div>
                    ))}
                </div>
            </div>
        )
    }

    export default Game;