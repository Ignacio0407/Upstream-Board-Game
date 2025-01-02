import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/game.css'
import bearTile from '../static/images/tiles/bearTile.png'
import eagleTile from '../static/images/tiles/eagleTile.png'
import heronTile from '../static/images/tiles/heronTile.png'
import jumpTile from '../static/images/tiles/jumpTile.png'
import rockTile from '../static/images/tiles/rockTile.png'
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
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { get, patch } from '../util/fetchers';


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

    // AL CREAR LA PARTIDA, SE CREAN, PARA CADA JUGADOR, 4 INSTANCIAS DE SALMONMATCH CON COORDENADAS NULAS
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
        /*if(gridTiles.length > 0 || gridSalmons.length > 0){
            reloadSalmon();
            reloadTiles();
        }*/
        if (players.length > 0 && tilesList.length > 0 && matchTiles.length > 0 && salmons.length > 0) {
            /*console.log("players", players)
            console.log("match", match)
            console.log("salmons", salmons)
            console.log("tilesList ", tilesList)
            console.log("matchTiles", gridTiles)*/
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
            setPlayers(orderedPlayers)
            setMyPlayer(players.filter(p => p.userPlayer === user.id)[0]);
        }
    }, [tilesList, matchTiles]);

    const fetchPlayers = async () => {
        const playersResponse = await get(`/api/v1/matches/${match.id}/players`, jwt);
        const playerData = await playersResponse.json();
        setPlayers(playerData);
    };
    
    const socket = new SockJS('http://localhost:8080/ws-upstream');
        const stompClient = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
            //console.log(str);
        },
        connectHeaders: {
            Authorization: `Bearer ${jwt}`
        },
        onConnect: (frame) => {
            console.log('Connected: ' + frame);
            
            stompClient.subscribe('/topic/salmon', (message) => {
                console.log('Message received: ' + message.body);
                const salmonMatchesNoCoord = [...salmons].filter(s => s.coordinate === null).map((s) => [s,getSalmonImage(s)])
                const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null).map((s) => [s,getSalmonImage(s)])
                setSalmonAndImages(salmonMatchesNoCoord)
                setGridSalmons(salmonMatchesWCoord)
            });
            stompClient.subscribe('/topic/tiles', (message) => {
                console.log('Message received: ' + message.body);
                const matchTilesNoCoord = [...matchTiles].filter(mT => mT.coordinate === null).map((t) => [t,getTileImage(t)])
                const matchTilesWCoord = [...matchTiles].filter(mT => mT.coordinate !== null).map((t) => [t,getTileImage(t)])
                setTilesAndImages(matchTilesNoCoord)
                setGridTiles(matchTilesWCoord)    
                
            });
            stompClient.subscribe('/topic/players', (message) => {
                console.log('Message received: ' + message.body);
                fetchPlayers();
            });
        },
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        },
        onWebSocketError: (error) => {
            console.error('Error with websocket', error);
        }
    });
    
    stompClient.activate();


    useEffect(() => {
        const newGrid = Array(18).fill(null).map(() => ({
            tile: null, 
            salmons: [] 
        }));
    
        const gridWidth = 3;
        const gridHeight = 6;
    
        gridTiles.forEach((tile) => {
            const index = (gridHeight - 1 - tile[0].coordinate.y) * gridWidth + tile[0].coordinate.x;
            newGrid[index].tile = {
                data: tile[0],
                image: tile[1]
            };
        });
    
        gridSalmons.forEach((salmon) => {
            const index = (gridHeight - 1 - salmon[0].coordinate.y) * gridWidth + salmon[0].coordinate.x;
            newGrid[index].salmons.push({
                data: salmon[0],
                image: salmon[1]
            });
        });
        setGrid(newGrid);
    }, [gridTiles, gridSalmons]);    


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

    // No quitar este useEffect
    useEffect(() => {
        const interval = setInterval(() => {
            get(`/api/v1/matchTiles/${match.id}`, jwt)
            .then(response => response.json())
            .then(data => setMatchTiles(data))
            .catch(error => console.error('Error fetching tiles:', error));
            
            get(`/api/v1/salmonMatches/match/${match.id}`, jwt)
            .then(response => response.json())
            .then(data => setSalmons(data))
            .catch(error => console.error('Error fetching tiles:', error));
        }, 1000); // Cada 5 segundos
        return () => clearInterval(interval);
    }, [jwt]);

    /*const reloadSalmon = async () => {
        const salmonMatchesNoCoord = [...salmons].filter(s => s.coordinate === null).map((s) => [s,getSalmonImage(s)])
        const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null).map((s) => [s,getSalmonImage(s)])
        setSalmonAndImages(salmonMatchesNoCoord)
        setGridSalmons(salmonMatchesWCoord) // Actualiza el estado con los nuevos jugadores
    };

    const reloadTiles = async () => {
        const matchTilesNoCoord = [...matchTiles].filter(mT => mT.coordinate === null).map((t) => [t,getTileImage(t)])
        const matchTilesWCoord = [...matchTiles].filter(mT => mT.coordinate !== null).map((t) => [t,getTileImage(t)])
        setTilesAndImages(matchTilesNoCoord)
        setGridTiles(matchTilesWCoord) // Actualiza el estado con los nuevos jugadores
    };*/


    /*const changePhase = async () => {
        if(gridTiles.length === 12 && match.phase === 'CASILLAS' && match.round === 0) {
            try {
                const response = await patch(`/api/v1/matches/${match.id}/changephase`, jwt)
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
    }, [gridTiles])*/

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
                <td className={'table-cell'} style={{position: 'relative', padding: '20px'}}>{p.energy}</td>
            </tr>
        );
    })

    const handleTileClick = (tile) => {
            if (myPlayer.id === match.actualPlayer && match.phase === 'CASILLAS') {
                setSelectedTile(tile);
                setSelectedSalmon(null)
            }
    }

    const handleSalmonClick = (salmon) => {
        if (myPlayer.id === match.actualPlayer && myPlayer.id === salmon[0].player && match.phase === 'MOVIENDO') {
            setSelectedSalmon(salmon);
            //console.log("selectedSalmon",selectedSalmon)
        }
    }

    const updateSalmonPosition = async(salmon,x,y) => {
        //console.log("grid", grid)
        //console.log("salmon",salmon[0])
        try{
            let energyUsed;
            if(salmon[0].coordinate === null){
                energyUsed = y+1;
            }else{
                energyUsed = Math.abs(salmon[0].coordinate.y - y);
            }
            //if(energyUsed > players.filter(p => p.id === salmon[0].player)[0].energy){
            if (energyUsed > match.actualPlayer.energy) {
                throw new Error('Not enough energy');
            }

        const responseSalmon = await patch(`/api/v1/salmonMatches/coordinate/${salmon[0].id}`, jwt, {x,y});
        const responseEnergy = await patch(`/api/v1/players/${salmon[0].player}/energy`, jwt, energyUsed);
        console.log("energyUsed",energyUsed)
        if (!responseSalmon.ok) {
            throw new Error('Invalid salmon placement');
        }
        else if (!responseEnergy.ok){
            throw new Error('Invalid energy use');
        }
        else{
            console.log("salmon e imagen",responseEnergy, salmonAndImages, salmon, energyUsed)
            const salmonWithImage = salmonAndImages.find(s => s[0][0].id === salmon[0].id);
            //console.log("salmonWithImage",salmonWithImage)
            setSalmonAndImages(prevSalmons =>
                prevSalmons.map(s => (s[0][0].id === salmon[0].id ? salmonWithImage : s))
            );   
        }
    }catch (error){
        throw error;
    }
    }

    const updateTilePosition = async (tile, x, y) => {
        try {
            const response = await patch(`/api/v1/matchTiles/${tile[0].id}`, jwt, {x,y});
            
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

    try {
        let nextPlayer = players[myPlayer.playerOrder + 1];
        if(selectedSalmon === null){
            await updateTilePosition(selectedTile, x, y);
            console.log("Entra en primer if")
            setSelectedTile(null);
        if (!nextPlayer) {
            nextPlayer = players[0]; // Volver al primer jugador si se termina la lista
            console.log("Entra en segundo if")
        }

        // Actualizar el turno en el servidor

        // await patch(`/api/v1/matches/${match.id}/threats/garza`, jwt)
        await patch(`/api/v1/matches/${match.id}/actualPlayer/${nextPlayer.id}`, jwt);
        }else{
            console.log("Entra en else")
            const foundTile = gridTiles.find(
                t => t.some(tile => tile.coordinate?.x === x && tile.coordinate?.y === y)
              );
            console.log("Casilla a la que quiero ir", foundTile)
            if(foundTile){    
            await updateSalmonPosition(selectedSalmon, x, y);
            setSelectedSalmon(null);
            try{
                stompClient.publish({
                    destination: "/app/players",
                    body: JSON.stringify({ action: "colorChanged" }),
                });
            //console.log("refreshPlayers",refreshPlayers)
            }catch(error){
                console.error("Error updating players", error);
            }
            }
            const actualPlayer = players.find(p => p.id === match.actualPlayer);
            console.log("actualPlayer",actualPlayer)
            if(actualPlayer.energy === 0){
                console.log("actual player sin energia")
                await patch(`/api/v1/matches/${match.id}/actualPlayer/${nextPlayer.id}`, jwt);
            }       
        }
        await patch(`/api/v1/matches/${match.id}/changephase`)
    } catch (error) {
        console.error("Error updating tile position or advancing turn:", error);
        // Detener ejecución si ocurre un error
        return;
    }
};

const handleRotateTile = async (tile) => {
    try {
        const newOrientation = (tile[0].orientation + 1) % 7; // Incrementa la rotación

        const response = await patch(`/api/v1/matchTiles/${tile[0].id}/rotation`, jwt, newOrientation);

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

const calculateSalmonPosition = (index, totalSalmons) => {
    const radius = 35; // Radio del pentágono
    const centerOffsetX = 90; // Centro X relativo a la casilla (mitad del ancho de la tile)
    const centerOffsetY = 30; // Centro Y relativo a la casilla (mitad del alto de la tile)
    
    // Si solo hay un salmón, colocarlo en el centro
    if (totalSalmons === 1) {
      return {
        left: `${centerOffsetX - 25}px`, // 25 es la mitad del ancho del salmón (50px/2)
        top: `${centerOffsetY - 25}px`
      };
    }

    // Calcular el ángulo para cada posición
    const angleStep = (2 * Math.PI) / totalSalmons // Usar 5 posiciones como máximo
    const angle = index * angleStep - Math.PI / 2; // Empezar desde arriba (-90 grados)

    // Calcular las coordenadas X e Y en el pentágono
    const x = centerOffsetX + radius * Math.cos(angle) + 10;
    const y = centerOffsetY + radius * Math.sin(angle) + 10;

    return {
      left: `${x}px`,
      top: `${y}px`
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
                        <th className="table-row" style={{position: 'relative', padding: '20px'}}>Energy</th>
                    </tr>
                </thead>
                <tbody>{playerList}</tbody>
            </div>
            

            {tilesAndImages.length > 0 &&
            
            <div key={tilesAndImages[0][0].id}
                style={{cursor: 'pointer', position: 'absolute', bottom: '-900px', right: '20px'}}
                onClick={() => handleTileClick(tilesAndImages[0])}>
                    {myPlayer.id === match.actualPlayer && match.phase === 'CASILLAS' && <h2>Pick the tile!</h2>}
                    <h2>Next tile:</h2>
                    {<img 
                    onClick={() => handleTileClick(tilesAndImages[0])}
                    src={tilesAndImages[0][1]} alt='' style={{
                        width: '150px',
                        ...getRotationStyle(tilesAndImages[0][0])} 
                        }></img>
                    }
                    {myPlayer.id === match.actualPlayer && match.phase === 'CASILLAS' && 
                    (tilesList[tilesAndImages[0][0].tile-1].type === 'OSO' || 
                        tilesList[tilesAndImages[0][0].tile-1].type === 'SALTO')
                    && <button onClick={() => handleRotateTile(tilesAndImages[0])}>Rotate Tile</button>}
            </div>
            }


            {gridS[0].length > 0 && 
                <div className='game-container'>
                    <div className='grid1'>
                    {grid.map((cell, index) => (
                    <div 
                        key={index} 
                        onClick={() => handleGridClick(index)} 
                        className="grid-item"
                        style={{ position: 'relative' }}
                    >
                        {cell.tile && (
                        <img 
                            src={cell.tile.image}
                            alt=""
                            style={{ 
                            width: '150px', 
                            ...getRotationStyle(cell.tile.data)
                            }}
                        />
                        )}

                        {cell.salmons && cell.salmons.map((salmon, sIndex) => {
                            const position = calculateSalmonPosition(sIndex, cell.salmons.length);
                            return (
                                <img
                                key={`salmon-${index}-${sIndex}`}
                                src={salmon.image}
                                alt=""
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleSalmonClick([salmon.data, salmon.image]);
                                }}
                                style={{
                                    width: '50px',
                                    position: 'absolute',
                                    ...position,
                                    transition: 'all 0.3s ease-in-out',
                                    zIndex: 2,
                                    cursor: 'pointer'
                                }}
                                />
                            );
                            })}
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