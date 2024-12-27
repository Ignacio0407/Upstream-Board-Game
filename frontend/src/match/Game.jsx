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
    import amarillo1 from '../static/images/salmons/Amarillo_1.png';
    import amarillo2 from '../static/images/salmons/Amarillo_2.png';
    import blanco1 from '../static/images/salmons/Blanco_1.png';
    import blanco2 from '../static/images/salmons/Blanco_2.png';
    import morado1 from '../static/images/salmons/Morado_1.png';
    import morado2 from '../static/images/salmons/Morado_2.png';
    import rojo1 from '../static/images/salmons/Rojo_1.png';
    import rojo2 from '../static/images/salmons/Rojo_2.png';
    import verde1 from '../static/images/salmons/Verde_1.png';
    import verde2 from '../static/images/salmons/Verde_2.png';


    export default function Game({match}){
        const jwt = tokenService.getLocalAccessToken();
        const user = tokenService.getUser();
        const [players, setPlayers] = useFetchState([],`/api/v1/matches/${match.id}/players`,jwt);
        const [tilesList,setTilesList] = useFetchState([], '/api/v1/tiles',jwt); // Siempre igual
        const [matchTiles, setMatchTiles] = useFetchState([], `/api/v1/matchTiles/${match.id}`,jwt) // Todos los front tienen las mismas tiles
        const [salmons, setSalmons] = useFetchState([], `/api/v1/salmonMatches/match/${match.id}`, jwt)
        const [allDataLoaded, setAllDataLoaded] = useState(false);
        const [tilesAndImages, setTilesAndImages] = useState([]);
        const [salmonAndImages, setSalmonAndImages] = useState([]);
        const [gridTiles, setGridTiles] = useState([]);
        const [gridSalmons, setGridSalmons] = useState([]);
        const [selectedTile, setSelectedTile] = useState(null);
        const [selectedSalmon, setSelectedSalmon] = useState(null);
        const [grid, setGrid] = useState(Array(18).fill(null).reverse()); // Cada celda será un array vacío.
        const [gridS, setGridS] = useState(Array(4).fill(null));
        const [myPlayer, setMyPlayer] = useState(null);
        const [currentPhase, setCurrentPhase] = useState(null);

        const getTileImage = (tileP) => {
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

        // AL CREAR LA PARTIDA, SE CREAN, PARA CADA JUGADOR, 4 INSTANCIAS DE SALMONMATCH CON COORDENADAS (0,i+1)
        const getSalmonImage = (salmonTile) => {
            if (!salmonTile) return null;  // Casilla vacia
            const color = players.filter(p => p.id === salmonTile.player)[0].color;
            const salmonN = salmonTile.salmonsNumber;
            switch(color){
                case 'AMARILLO':
                    return salmonN === 1? amarillo1 : amarillo2;
                case 'BLANCO':
                    return salmonN === 1? blanco1 : blanco2;
                case 'MORADO':
                    return salmonN === 1? morado1 : morado2;
                case 'ROJO':
                    return salmonN === 1? rojo1 : rojo2;
                case 'VERDE':
                    return salmonN === 1? verde1 : verde2;
                default:
                    return null;
            }
        }

        useEffect(() => {
            if (players.length > 0 && tilesList.length > 0 && matchTiles.length > 0 && salmons.length > 0) {
                setAllDataLoaded(true);
                const matchTilesNoCoord = [...matchTiles].filter(mT => mT.coordinate === null).map((t) => [t,getTileImage(t)])
                const matchTilesWCoord = [...matchTiles].filter(mT => mT.coordinate !== null).map((t) => [t,getTileImage(t)])
                const salmonMatchesNoCoord = [...salmons].filter(s => s.coordinate === null).map((s) => [s,getSalmonImage(s)])
                const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null).map((s) => [s,getSalmonImage(s)])
                setTilesAndImages(matchTilesNoCoord)
                setGridTiles(matchTilesWCoord)
                setSalmonAndImages(salmonMatchesNoCoord)
                setGridSalmons(salmonMatchesWCoord)
                const orderedPlayers = [...players].sort(p => p.playerOrder)
                setPlayers(orderedPlayers) // Siempre igual
                setMyPlayer(players.filter(p => p.userPlayer === user.id)[0]);
                setCurrentPhase(match.phase);
            }
        }, [tilesList, matchTiles]);

        useEffect(() => {
            // Construir el nuevo estado del grid basado en gridTiles
            const newGrid = Array(18).fill(null).map(() => []); // Crea una cuadrícula vacía de 18 espacios
            const gridWidth = 3; // Ancho de la cuadrícula (número columnas)
            const gridHeight = 6; // Altura de la cuadrícula (número filas)
            // Asignar las tiles con coordenadas al grid
            gridTiles.forEach((tile) => {
                // Convertir las coordenadas (x, y) en un índice del grid
                const index = (gridHeight - 1 - tile[0].coordinate.y) * gridWidth + tile[0].coordinate.x;
                const image = tile[1]; // Obtener la imagen asociada al tile
                newGrid[index].push([tile, image,"tile"]); // Asignar la tile con su imagen al grid 
            });

            gridSalmons.forEach((salmon) => {
                const index = (gridHeight - 1 - salmon[0].coordinate.y) * gridWidth + salmon[0].coordinate.x;
                const image = salmon[1]; // Obtener la imagen asociada al salmon
                newGrid[index].push(salmon,image,"salmon")
            });
            setGrid(newGrid);
            console.log(grid)
        }, [gridTiles,gridSalmons]);

        useEffect(() => {
            const salmonsPerPlayer = 4;
            const newGridS = Array(salmonsPerPlayer).fill(null).map(() => []);
            players.forEach((p) => {
                const pSalmons = salmons.filter(s => s.player === p.id);
                if(pSalmons.length > 0) {
                    for (let i = 0; i < salmonsPerPlayer; i++) {
                    newGridS[i].push([pSalmons[i], getSalmonImage(pSalmons[i])]); }
                }})
            setGridS(newGridS);
        }, [gridSalmons])

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
                fetch(`/api/v1/salmonMatches/match/${match.id}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwt}`
                    },
                })
                .then(response => response.json())
                .then(data => setSalmons(data))
                .catch(error => console.error('Error fetching tiles:', error));
            }, 1000); // Cada 5 segundos
            return () => clearInterval(interval);
        }, [jwt]);

        const changePhase = async () => {
            if(gridTiles.length === 12 && currentPhase === 'CASILLAS') {
                try {
                    const response = await fetch(`/api/v1/matches/${match.id}/changephase`, {
                        method: 'PATCH',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${jwt}`
                        }
                    });

                    if(response.ok) {
                        console.log('Fase cambiada correctamente.');
                    } else {
                        console.error('Error al cambiar de fase.')
                    }

                } catch (error) {
                    console.error('Error changing phase', error);
                }
            }
        };

        useEffect(() => {
            changePhase();
        }, [gridTiles])

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
                if (myPlayer.id === match.actualPlayer && currentPhase === 'CASILLAS') {
                    setSelectedTile(tile);
                    setSelectedSalmon(null)
                }
        }

        const handleSalmonClick = (salmon) => {
            if (myPlayer.id === match.actualPlayer && myPlayer.id === salmon[0].player && currentPhase === 'MOVIENDO') {
                setSelectedSalmon(salmon);
                console.log("selectedSalmon",selectedSalmon)
            }
        }

        const updateSalmonPosition = async(salmon,x,y) => {
            try{
            const response = await fetch(`/api/v1/salmonMatches/coordinate/${salmon[0].id}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${jwt}`
                },
                body: JSON.stringify({ x, y })
            });
            if (!response.ok) {
                throw new Error('Invalid salmon placement');
            }else{
                console.log(response, salmonAndImages)
                const salmonWithImage = salmonAndImages.find(s => s[0][0].id === salmon.id);
                setSalmonAndImages(prevSalmons =>
                    prevSalmons.map(s => (s[0][0].id === salmon[0].id ? salmonWithImage : s))
                );
                
            }

        }catch{

        }

        }

        const updateTilePosition = async (tile, x, y) => {
            try {
                console.log("dato", tile, x, y)
                const response = await fetch(`/api/v1/matchTiles/${tile[0].id}`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwt}`
                    },
                    body: JSON.stringify({ x, y })
                });
                
                if (!response.ok) {
                    console.log(x,y)
                    throw new Error('Invalid tile placement');

                }
        
                const updatedTile = await response.json();
                console.log("updatedTile",updatedTile);
        
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
        const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
        const gridHeight = 6; // Altura de la cuadrícula (número de filas)

        // Calcular la coordenada x (columna) y y (fila) con filas invertidas
        const x = index % gridWidth; // Coordenada x (columna)
        const y = gridHeight - 1 - Math.floor(index / gridWidth); // Coordenada y invertida (fila)

                // Reiniciar la casilla seleccionada después de moverla
                /*
                setSelectedTile(null);
                setSelectedSalmon(null)
                let nextPlayer = players[myPlayer.playerOrder+1];
                console.log(nextPlayer)
                console.log(players)
                if(!nextPlayer){
                    nextPlayer = players[0];}
                  */
        try {
            
            console.log("salmon seleccionada",selectedSalmon)
            if(selectedSalmon === null){
                await updateTilePosition(selectedTile, x, y);
                setSelectedTile(null);
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
            }else{
                const foundTile = gridTiles.find(
                    t => t.some(tile => tile.coordinate?.x === x && tile.coordinate?.y === y)
                  );
                console.log("AAAAAAA", foundTile)
                if(foundTile){
                await updateSalmonPosition(selectedSalmon, x, y);
                setSelectedSalmon(null);
                   
                }
            
            }
        } catch (error) {
            console.error("Error updating tile position or advancing turn:", error);
            // Detener ejecución si ocurre un error
            return;
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
                    style={{cursor: 'pointer', position: 'absolute', bottom: '-900px', right: '20px'}}
                    onClick={() => handleTileClick(tilesAndImages[0])}>
                        {myPlayer.id === match.actualPlayer && currentPhase === 'CASILLAS' && <h2>Pick the tile!</h2>}
                        <h2>Next tile:</h2>
                        {<img 
                        onClick={() => handleTileClick(tilesAndImages[0])}
                        src={tilesAndImages[0][1]} alt='' style={{
                            width: '150px',
                            ...getRotationStyle(tilesAndImages[0][0])} 
                            }></img>
                        }
                        {myPlayer.id === match.actualPlayer && currentPhase === 'CASILLAS' && 
                        (tilesList[tilesAndImages[0][0].tile-1].type === 'OSO' || 
                            tilesList[tilesAndImages[0][0].tile-1].type === 'SALTO')
                        && <button onClick={() => handleRotateTile(tilesAndImages[0])}>Rotate Tile</button>}
                </div>
                }
                {gridS[0].length > 0 && 
                <div className='game-container'>
                    <div className='grid1'>
                    {grid.map((cell, index) => (
                        <div key={index} onClick={() => handleGridClick(index)} className="grid-item"> 
                            {cell.map((element, i) => (
                                
                                <img 
                                key = {i}
                                src = {element[1]}
                                alt=""
                                style={
                            element[2] === "tile"
                            ? { width: '150px', ...getRotationStyle(element[0][0]) }
                            : { width: '50px',position: 'absolute',top:`${9*i}px`, margin:0} // Superponer salmons
                    }
/>
                            ))}
                        </div>
                    ))}
                    </div>
                    <div className='grid2'>
                    {gridS.map((salmon, index) => (
                        <div key={index} className="grid-item2">    
                            {salmon.map((s, i) => (
                                (s[0].coordinate === null && 
                                <img
                                key = {i}
                                src = {s[1]}
                                alt=""
                                onClick={() => handleSalmonClick(s)}
                                />
                                )
                            ))}
                        </div>
                    ))}
                    </div>
    
                </div>
    }
            </div>
        )

    }