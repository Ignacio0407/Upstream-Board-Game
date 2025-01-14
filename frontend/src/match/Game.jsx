import React, { useState, useEffect } from 'react';
import { useLocation } from "react-router-dom";
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/game.css'
import bearTile from '../static/images/tiles/bearTile.png'
import eagleTile from '../static/images/tiles/eagleTile.png'
import heronTile from '../static/images/tiles/heronTile.png'
import jumpTile from '../static/images/tiles/jumpTile.png'
import rockTile from '../static/images/tiles/rockTile.png'
import waterTile from '../static/images/tiles/waterTile.png'
import spawningTile from '../static/images/tiles/spawningTile.png'
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
import { getTileImage, getSalmonImage, handleTileClick, handleRotateTile, getRotationStyle, generatePlayerList, calculateSalmonPosition } from './matchUtil';
import { ColorToRgb } from '../util/ColorParser';
import Chat from '../chat/Chat'

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
    const [spawnSalmons, setSpawnSalmons] = useState([]);
    const [selectedTile, setSelectedTile] = useState(null);
    const [selectedSalmon, setSelectedSalmon] = useState(null);
    const [grid, setGrid] = useState(Array(18).fill(null).reverse()); // Cada celda será un array vacío.
    const [gridS, setGridS] = useState(Array(4).fill(null));
    const [gridD, setGridD] = useState(Array(5).fill(null));
    const [myPlayer, setMyPlayer] = useState(null);
    const tileImages = {bearTile, eagleTile, heronTile, jumpTile, rockTile, waterTile};
    const salmonImages = {amarillo1, amarillo2, blanco1, blanco2, morado1, morado2, rojo1, rojo2, verde1, verde2};
    let playerList = [];
    if (!match.actualPlayer) {
        const firstPlayerId = players.length > 0 ? players[0].id : match.matchCreator;
        playerList = generatePlayerList(players, firstPlayerId);
    }
    playerList = generatePlayerList(players, match.actualPlayer);
    
    const location = useLocation();
    const spectatorIds = location.state?.spectatorIds || []; // Obtener la lista de espectadores

    // Comprobar si el usuario actual está en la lista de espectadores
    const isCurrentUserSpectator = spectatorIds.includes(user.id);

    useEffect(() => {
        if (players.length > 0 && tilesList.length > 0 && matchTiles.length > 0 && salmons.length > 0) {
            //console.log("match", match)
            setAllDataLoaded(true);
            const matchTilesNoCoord = [...matchTiles].filter(mT => mT.coordinate === null).map((t) => [t,getTileImage(t, tilesList, tileImages)])
            const matchTilesWCoord = [...matchTiles].filter(mT => mT.coordinate !== null).map((t) => [t,getTileImage(t, tilesList, tileImages)])
            const salmonMatchesNoCoord = [...salmons].filter(s => s.coordinate === null).map((s) => [s,getSalmonImage(s, players, salmonImages)])
            const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null).map((s) => [s,getSalmonImage(s, players, salmonImages)])
            setTilesAndImages(matchTilesNoCoord)
            setGridTiles(matchTilesWCoord)
            setSalmonAndImages(salmonMatchesNoCoord)
            setGridSalmons(salmonMatchesWCoord)
            const orderedPlayers = [...players].sort(p => p.playerOrder)
            setPlayers(orderedPlayers)
            setMyPlayer(players.filter(p => p.userPlayer === user.id)[0]);
            //console.log(gridD);
        }
    }, [tilesList, matchTiles]);

    const fetchPlayers = async () => {
        const playersResponse = await get(`/api/v1/matches/${match.id}/players`, jwt);
        const playerData = await playersResponse.json();
        setPlayers(playerData);
    };
    
    const handleSalmonClick = (salmon) => {
        console.log("My player id", myPlayer.id);
        console.log("match.actualPlayer", match.actualPlayer);
        console.log("salmon[0].player", salmon[0].player)
        console.log("Soy el salmon", salmon)
        if (myPlayer.id === match.actualPlayer && myPlayer.id === salmon[0].player && match.phase === 'MOVIENDO') {
            setSelectedSalmon(salmon);
            console.log("Selected salmon", salmon)
        }
    }

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
        //console.log('Connected: ' + frame);
        
        stompClient.subscribe('/topic/salmon', (message) => {
            console.log('Message received: ' + message.body);
            const salmonMatchesNoCoord = [...salmons].filter(s => s.coordinate === null).map((s) => [s,getSalmonImage(s, players, salmonImages)])
            const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null).map((s) => [s,getSalmonImage(s, players, salmonImages)])
            setSalmonAndImages(salmonMatchesNoCoord)
            setGridSalmons(salmonMatchesWCoord)
        });
        stompClient.subscribe('/topic/tiles', (message) => {
            console.log('Message received: ' + message.body);
            const matchTilesNoCoord = [...matchTiles].filter(mT => mT.coordinate === null).map((t) => [t,getTileImage(t, tilesList, tileImages)])
            const matchTilesWCoord = [...matchTiles].filter(mT => mT.coordinate !== null).map((t) => [t,getTileImage(t, tilesList, tileImages)])
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
            if(salmon[0].coordinate.y < 6){
            const index = (gridHeight - 1 - salmon[0].coordinate.y) * gridWidth + salmon[0].coordinate.x;
            
            newGrid[index].salmons.push({
                data: salmon[0],
                image: salmon[1]

            });
        }
        });
        setGrid(newGrid);
    }, [gridTiles, gridSalmons]);    

    useEffect(() => {
        const salmonsPerPlayer = 4;
        const newGridS = Array(salmonsPerPlayer).fill(null).map(() => []);
        players.forEach((p) => {
            const pSalmons = salmons.filter(s => s.player === p.id);
            //console.log("pSalomns", pSalmons)
            if(pSalmons.length > 0) {
                for (let i = 0; i < pSalmons.length; i++) {
                newGridS[i].push([pSalmons[i], getSalmonImage(pSalmons[i], players, salmonImages)]); }
            }})
        setGridS(newGridS);
        //console.log("gridS", newGridS)
    }, [gridSalmons])

    useEffect(() => {
        const newGridD = Array(5).fill(null).map(() => []);
        const coords = {21: 4, 22: 3, 23: 2, 24: 1, 25: 0}; 
        if(spawnSalmons.length > 0) {
            for (let i = 0; i < spawnSalmons.length; i++) {
            const index = coords[spawnSalmons[i].coordinate.y]
            newGridD[index].push([spawnSalmons[i], getSalmonImage(spawnSalmons[i], players, salmonImages)]); }
        }
        setGridD(newGridD)
    }, [spawnSalmons])

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
            .catch(error => console.error('Error fetching salmons:', error));
           
            get(`/api/v1/salmonMatches/match/${match.id}/spawn`, jwt)
            .then(response => response.json())
            .then(data => setSpawnSalmons(data))
            .catch(error => console.error('Error fetching spawn salmons:', error));

            get(`/api/v1/players/match/${match.id}`, jwt)
            .then(response => response.json())
            .then(data => {let ps = [...data].sort(p => p.playerOrder);
                setPlayers(ps);
            })
            .catch(error => console.error('Error fetching players:', error));
        }, 500); // Cada 5 segundos
        return () => clearInterval(interval);
    }, [jwt]);

    if (!allDataLoaded) {
        return <div style={{justifySelf:'center'}}>Loading data</div>;
    }

    const updateSalmonPosition = async(salmon,x,y) => {
        try {
            const responseSalmon = await patch(`/api/v1/salmonMatches/coordinate/${salmon[0].id}`, jwt, {x,y});
            if (!responseSalmon.ok) {
                const errorData = await responseSalmon.json(); // Parsea el cuerpo de la respuesta
                alert(errorData.error || "Movimiento no válido."); // Usa el mensaje del backend o un mensaje por defecto
                console.log("Error actualizando salmón:", errorData);
            }            
        } catch (error){
            console.log("Error actualizando salmon", error)
            throw error.message;
        }
    }
    
    const updateSpawn = async(salmon) => {
        try {
            const responseSalmon = await patch(`/api/v1/salmonMatches/enterSpawn/${salmon[0].id}`, jwt);
            if (!responseSalmon.ok) {
                alert("Movimiento no válido.");
                console.log("Error actualizando salmon", responseSalmon)
            }
        } catch (error){
            console.log("Error actualizando salmon", error)
            throw error.message;
        }
    }

    const updateTilePosition = async (tile, x, y) => {
        try {
            const response = await patch(`/api/v1/matchTiles/${tile[0].id}`, jwt, {x,y});
            if (!response.ok) {
                throw new Error('Invalid tile placement');
            }
        } catch (error) {
            console.error('Error updating tile position:', error);
            throw error; 
        }
    };

    const handleGridClick = async (index) => {
        const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
        const gridHeight = 6; // Altura de la cuadrícula (número de filas)
        const x = index % gridWidth;
        const y = gridHeight - 1 - Math.floor(index / gridWidth)
        console.log("Selected salmon", selectedSalmon)
        try {
            if(selectedSalmon === null){
                await updateTilePosition(selectedTile, x, y);
                setSelectedTile(null);
                //await patch(`/api/v1/matches/${match.id}/actualPlayer/${match.actualPlayer}`, jwt);
            }
            else{
                const foundTile = gridTiles.find(
                    t => t.some(tile => tile.coordinate?.x === x && tile.coordinate?.y === y)
                );
                if(foundTile){    
                    await updateSalmonPosition(selectedSalmon, x, y);
                    setSelectedSalmon(null);
                }else if(x === 1 && y === 5 && match.round >= 6){
                    await updateSpawn(selectedSalmon);        
                    setSelectedSalmon(null);
                }

                }
                console.log("JWT", jwt)
                await patch(`/api/v1/matches/${match.id}/changephase/${match.actualPlayer}`, jwt)
                //await patch(`/api/v1/matches/${match.id}/endRound`, jwt);
            }catch (error) {
                console.error("Error updating tile position or advancing turn:", error);
            }
    };

    return(
        <div className='gamePage-container'>
            <h1 class="game-title game-name">Game: {match.name}</h1>
            <h1 class="game-title game-round">Round: {match.round}</h1>
            <h1 class="game-title game-phase">Phase: {match.phase}</h1>
            {!isCurrentUserSpectator && myPlayer.id === match.actualPlayer && match.phase === 'MOVIENDO' && <h1 class="game-title game-turn">Move your salmons!</h1>}
            <table className="users-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
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
            

            {tilesAndImages.length > 0 &&
            <div key={tilesAndImages[0][0].id}
                style={{cursor: 'pointer', position: 'absolute', bottom: '-900px', right: '20px'}}
                onClick={() =>
                handleTileClick(tilesAndImages[0], myPlayer, match, setSelectedTile, setSelectedSalmon)
                }>
                    {selectedTile && <h2>Selected tile: <img src={tilesAndImages[0][1]} alt='' style={{width: '150px'}}></img></h2>}
                    {<h2>Remaining tiles: {tilesAndImages.length}</h2>}
                    {!isCurrentUserSpectator && myPlayer.id === match.actualPlayer && match.phase === 'CASILLAS' && <h2>Pick the tile!</h2>}
                    <h2>Next tile:</h2>
                    {<img 
                    onClick={() => handleTileClick(tilesAndImages[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
                    src={tilesAndImages[0][1]} alt='' style={{
                        width: '150px',
                        ...getRotationStyle(tilesAndImages[0][0])} 
                        }></img>
                    }
                    {!isCurrentUserSpectator && myPlayer.id === match.actualPlayer && match.phase === 'CASILLAS' && 
                    (tilesList[tilesAndImages[0][0].tile-1].type === 'OSO' || 
                        tilesList[tilesAndImages[0][0].tile-1].type === 'SALTO')
                    && <button onClick={() => handleRotateTile(tilesAndImages[0], jwt)}>Rotate Tile</button>}
            </div>
            }


            {gridS[0].length > 0 && 
                <div className='game-container'>
                    {match.round >= 6 && <div className='grid3'>
                        {gridD.map((salmon, index) => (
                        <div key={index} className="grid-item3">    
                            <div className='salmons-containerSpawn'>
                            {salmon.map((s, i) => (
                                (s[0].coordinate.y > 20 && 
                                <img
                                src = {s[1]}
                                alt=""
                                style={{                          
                                    filter: `drop-shadow(0px 0px 5px ${ColorToRgb(players.filter(p => p.id === s[0].player)[0].color)}`,
                                    border: `3px solid ${ColorToRgb(players.filter(p => p.id === s[0].player)[0].color)}`, // Cambia el color y grosor del borde según necesites
                                    borderRadius: '40px',}}
                                />
                               
                                
                                )
                            ))}
                             
                             </div>
                             <div key={index} className='desove-points'>
                                {5 - index} 
                             </div>
                        </div>
                    ))}
                    </div>}
                    <div className={match.round < 2 && match.round < 6 ? 'grid1' : 'grid1-100'}>
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
                            width: '250px',
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
                                    console.log("Clicked salmon data:", salmon.data);
                                    console.log("Clicked salmon image:", salmon.image);
                                    handleSalmonClick([salmon.data, salmon.image]);
                                }}
                                style={{
                                    width: '80px',
                                    position: 'absolute',
                                    ...position,
                                    transition: 'all 0.3s ease-in-out',
                                    zIndex: 2,
                                    cursor: 'pointer',
                                    filter: `drop-shadow(0px 0px 2px ${ColorToRgb(players.filter(p => p.id === salmon.data.player)[0].color)}`,
                                    border: `3px solid ${ColorToRgb(players.filter(p => p.id === salmon.data.player)[0].color)}`,
                                    borderRadius: '40px',    
                                }}
                                />
                            );
                            })}
                    </div>
                    ))}

                    </div>
                    {match.round < 2 &&
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
                                style={{                          
                                    filter: `drop-shadow(0px 0px 5px ${ColorToRgb(players.filter(p => p.id === s[0].player)[0].color)}`,
                                    border: `3px solid ${ColorToRgb(players.filter(p => p.id === s[0].player)[0].color)}`, // Cambia el color y grosor del borde según necesites
                                    borderRadius: '40px',}}
                                />
                                )
                            ))}
                        </div>
                    ))}
                    </div>
                    }
                </div>
            }
        {<Chat match={match} players={players} currentPlayer={myPlayer} />}
    </div>
    )

}