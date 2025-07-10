import { useState, useEffect } from 'react';
import { useLocation } from "react-router-dom";
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/game.css'
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { get, patch, createWebSocket } from '../util/fetchers';
import { handleTileClick, handleRotateTile, getRotationStyle, generatePlayerList, calculateSalmonPosition } from './matchUtil';
import { ColorToRgb } from '../util/ColorParser';
import Chat from '../chat/Chat'

export default function Game({match}){
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser();
    const [players, setPlayers] = useFetchState([],`/api/v1/players/match/${match.id}`,jwt);
    const [tilesList,setTilesList] = useFetchState([], '/api/v1/tiles',jwt); // Siempre igual
    const [matchTilesList, setMatchTilesList] = useFetchState([], `/api/v1/matchTiles/match/${match.id}`,jwt) // Todos los front tienen las mismas tiles
    const [salmons, setSalmons] = useFetchState([], `/api/v1/salmonMatches/match/${match.id}`, jwt)
    const [allDataLoaded, setAllDataLoaded] = useState(false);
    const [matchTiles, setMatchTiles] = useState([]);
    const [salmonAndImages, setSalmonAndImages] = useState([]);
    const [gridTiles, setGridTiles] = useState([]);
    const [gridSalmons, setGridSalmons] = useState([]);
    const [spawnSalmons, setSpawnSalmons] = useState([]);
    const [selectedTile, setSelectedTile] = useState(null);
    const [selectedSalmon, setSelectedSalmon] = useState(null);
    const [grid, setGrid] = useState(Array(18).fill(null).reverse()); // Cada celda será un array vacío.
    const [gridS, setGridS] = useState(Array(4).fill(null).map(() => []));
    const [gridD, setGridD] = useState(Array(5).fill(null).map(() => []));
    const [myPlayer, setMyPlayer] = useState(null);
    const playerList = generatePlayerList(players, match.actualPlayerId);
    
    const location = useLocation();
    const spectatorIds = location.state?.spectatorIds || []; // Obtener la lista de espectadores

    // Comprobar si el usuario actual está en la lista de espectadores
    const isCurrentUserSpectator = spectatorIds.includes(user.id);

    useEffect(() => {
        if (players.length > 0 && tilesList.length > 0 && matchTilesList.length > 0 && salmons.length > 0) {
            setAllDataLoaded(true);
            const matchTilesNoCoord = [...matchTilesList].filter(mT => mT.coordinate === null)
            const matchTilesWCoord = [...matchTilesList].filter(mT => mT.coordinate !== null)
            const salmonMatchesNoCoord = [...salmons].filter(s => s.coordinate === null)
            const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null)
            setMatchTiles(matchTilesNoCoord)
            setGridTiles(matchTilesWCoord)
            setSalmonAndImages(salmonMatchesNoCoord)
            setGridSalmons(salmonMatchesWCoord)
            const orderedPlayers = [...players].sort(p => p.playerOrder)
            setPlayers(orderedPlayers)
            setMyPlayer(players.filter(p => p.userId === user.id)[0]);
        }
    }, [tilesList, matchTilesList]);

    const fetchPlayers = async () => {
        const playersResponse = await get(`/api/v1/players/match/${match.id}`, jwt);
        const playerData = await playersResponse.json();
        setPlayers(playerData);
    };
    
    const handleSalmonClick = (salmon) => {
        if (myPlayer.id === match.actualPlayerId && myPlayer.id === salmon.playerId && match.phase === 'MOVING') {
            setSelectedSalmon(salmon);
        }
    }

    const socket = createWebSocket();
    const stompClient = new Client({
    webSocketFactory: () => socket,
    debug: (str) => {
        //console.log(str);
    },
    connectHeaders: {
        Authorization: `Bearer ${jwt}`
    },
    onConnect: (frame) => {
        
        stompClient.subscribe('/topic/salmon', (message) => {
            const salmonMatchesNoCoord = [...salmons].filter(s => s.coordinate === null)
            const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null)
            setSalmonAndImages(salmonMatchesNoCoord)
            setGridSalmons(salmonMatchesWCoord)
        });
        stompClient.subscribe('/topic/tiles', (message) => {
            const matchTilesNoCoord = [...matchTilesList].filter(mT => mT.coordinate === null)
            const matchTilesWCoord = [...matchTilesList].filter(mT => mT.coordinate !== null)
            setMatchTiles(matchTilesNoCoord)
            setGridTiles(matchTilesWCoord)    
            
        });
        stompClient.subscribe('/topic/players', (message) => {
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
            const index = (gridHeight - 1 - tile.coordinate.y) * gridWidth + tile.coordinate.x;
            newGrid[index].tile = tile
        });
    
        gridSalmons.forEach((salmon) => {
            if (salmon.coordinate.y < 6) {
            const index = (gridHeight - 1 - salmon.coordinate.y) * gridWidth + salmon.coordinate.x;   
            newGrid[index].salmons.push(salmon);
            }
        });
        setGrid(newGrid);
    }, [gridTiles, gridSalmons]);    

    useEffect(() => {
        const salmonsPerPlayer = 4;
        const newGridS = Array(salmonsPerPlayer).fill(null).map(() => []);
        players.forEach((p) => {
            const pSalmons = salmons.filter(s => s.playerId === p.id);
            if(pSalmons.length > 0) {
                for (let i = 0; i < pSalmons.length; i++) {
                newGridS[i].push(pSalmons[i]) }
            }})
        setGridS(newGridS);
    }, [salmons, players])

    useEffect(() => {
        const newGridD = Array(5).fill(null).map(() => []);
        const coords = {21: 4, 22: 3, 23: 2, 24: 1, 25: 0}; 
        if(spawnSalmons.length > 0) {
            for (let i = 0; i < spawnSalmons.length; i++) {
            const index = coords[spawnSalmons[i].coordinate.y]
            newGridD[index].push(spawnSalmons[i]); }
        }
        setGridD(newGridD)
    }, [spawnSalmons])

    // No quitar este useEffect -> MIRAR CON DETENIMIENTO
    useEffect(() => {
        const interval = setInterval(() => {
            get(`/api/v1/matchTiles/match/${match.id}`, jwt)
            .then(response => response.json())
            .then(data => setMatchTilesList(data))
            .catch(error => console.error('Error fetching tiles:', error));
            
            get(`/api/v1/salmonMatches/match/${match.id}`, jwt)
            .then(response => response.json())
            .then(data => setSalmons(data))
            .catch(error => console.error('Error fetching salmons:', error));
           
            get(`/api/v1/salmonMatches/match/${match.id}/spawn`, jwt)
            .then(response => response.json())
            .then(data => setSpawnSalmons(data))
            .catch(error => console.error('Error fetching spawn salmons:', error));

            get(`/api/v1/players/match/sorted/${match.id}`, jwt)
            .then(response => response.json())
            .then(data => setPlayers(data))
            .catch(error => console.error('Error fetching players:', error));
        }, 500); // Cada 5 segundos
        return () => clearInterval(interval);
    }, [jwt]);

    if (!allDataLoaded) {
        return <div style={{justifySelf:'center'}}>Loading data</div>;
    }

    const updateSalmonPosition = async(salmon,x,y) => {
        try {
            const responseSalmon = await patch(`/api/v1/salmonMatches/coordinate/${salmon.id}`, jwt, {x,y});
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

    const changephase = async() => {
        try {
            const responseChangePhase = await patch(`/api/v1/matches/${match.id}/changephase/${match.actualPlayerId}`, jwt)
            if (!responseChangePhase.ok) {
                const errorData = await responseChangePhase.json();
                alert(errorData.error || "Error changing phase.");
                console.log("Error changing phase.", errorData);
            }            
        } catch (error){
            console.log("Error changing phase.", error)
            throw error.message;
        }
    }
    
    const updateSpawn = async(salmon) => {
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
    }

    const updateTilePosition = async (tile, x, y) => {
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

    const handleGridClick = async (index) => {
        const gridWidth = 3; // Ancho de la cuadrícula (número de columnas)
        const gridHeight = 6; // Altura de la cuadrícula (número de filas)
        const x = index % gridWidth;
        const y = gridHeight - 1 - Math.floor(index / gridWidth)
        try {
            if (selectedSalmon === null) {
                await updateTilePosition(selectedTile, x, y);
                setSelectedTile(null);
            }
            else {
                const foundTile = gridTiles.find(tile => tile.coordinate?.x === x && tile.coordinate?.y === y);
                if (foundTile) {    
                    await updateSalmonPosition(selectedSalmon, x, y);
                    setSelectedSalmon(null);
                } else if(x === 1 && y === 5 && match.round >= 6){
                    await updateSpawn(selectedSalmon);        
                    setSelectedSalmon(null);
                }

                }
                changephase();
            }
            catch (error) {
                console.error("Error updating tile position or advancing turn:", error);
            }
    };

    return(
        <div className='gamePage-container'>
            <h1 className="game-title game-name">Game: {match.name}</h1>
            <h1 className="game-title game-round">Round: {match.round}</h1>
            <h1 className="game-title game-phase">Phase: {match.phase}</h1>
            {!isCurrentUserSpectator && myPlayer.id === match.actualPlayerId && match.phase === 'MOVING' && 
            <h1 className="game-title game-turn move-your-salmons">Move your salmons!</h1>}
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
            

            {matchTiles.length > 0 &&
            <div key={matchTiles[0].id}
                className='pick-the-tile-block'
                onClick={() =>
                handleTileClick(matchTiles[0], myPlayer, match, setSelectedTile, setSelectedSalmon)
                }>
                    {!isCurrentUserSpectator && myPlayer.id === match.actualPlayerId && match.phase === 'TILES' && <h2>Pick the tile!</h2>}
                    {<h2>Remaining tiles: {matchTiles.length}</h2>}
                    <h2>Next tile:</h2>
                    {<img 
                    onClick={() => handleTileClick(matchTiles[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
                    src={matchTiles[0].tile.image} alt='' style={{
                        width: '150px',
                        ...getRotationStyle(matchTiles[0])} 
                        }></img>
                    }
                    {!isCurrentUserSpectator && myPlayer.id === match.actualPlayerId && match.phase === 'TILES' && 
                    (tilesList[matchTiles[0].tile.id-1].type === 'BEAR' || 
                        tilesList[matchTiles[0].tile.id-1].type === 'JUMP')
                    && <button onClick={() => handleRotateTile(matchTiles[0], jwt)}>Rotate Tile</button>}
            </div>
            }


            { 
                <div className='game-container'>
                    {match.round >= 6 && <div className='grid3'>
                        {gridD.map((salmon, index) => (
                        <div key={index} className="grid-item3">    
                            <div className='salmons-containerSpawn'>
                            {salmon.map((s, i) => (
                                (s.coordinate.y > 20 && 
                                <img key = {i} src = {s.salmon.image} alt=""
                                style={{                          
                                    filter: `drop-shadow(0px 0px 5px ${ColorToRgb(players.filter(p => p.id === s.playerId)[0].color)}`,
                                    border: `3px solid ${ColorToRgb(players.filter(p => p.id === s.playerId)[0].color)}`, // Cambia el color y grosor del borde según necesites
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
                        {cell.tile && (<img src={cell.tile.tile.image} alt=""
                            style={{ 
                            width: '250px',
                            ...getRotationStyle(cell.tile)
                            }}
                        />
                        )}

                        {cell.salmons && cell.salmons.map((salmon, sIndex) => {
                            const position = calculateSalmonPosition(sIndex, cell.salmons.length);
                            return (
                                <img key={`salmon-${index}-${sIndex}`} src={salmon.salmon.image} alt=""
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleSalmonClick(salmon);
                                }}
                                style={{
                                    width: '80px',
                                    position: 'absolute',
                                    ...position,
                                    transition: 'all 0.3s ease-in-out',
                                    zIndex: 2,
                                    cursor: 'pointer',
                                    filter: `drop-shadow(0px 0px 2px ${ColorToRgb(players.filter(p => p.id === salmon.playerId)[0].color)}`,
                                    border: `3px solid ${ColorToRgb(players.filter(p => p.id === salmon.playerId)[0].color)}`,
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
                                (s.coordinate === null && <img key = {i} src = {s.salmon.image} alt="" onClick={() => handleSalmonClick(s)}
                                style={{                          
                                    filter: `drop-shadow(0px 0px 5px ${ColorToRgb(players.filter(p => p.id === s.playerId)[0].color)}`,
                                    border: `3px solid ${ColorToRgb(players.filter(p => p.id === s.playerId)[0].color)}`, // Cambia el color y grosor del borde según necesites
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