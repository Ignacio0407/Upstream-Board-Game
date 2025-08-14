import { useState, useEffect, JSX } from 'react';
import { useLocation } from "react-router-dom";
import tokenService from '../services/token.service.ts'
import useFetchState from "../util/useFetchState.ts";
import '../static/css/game/game.css'
import { Client } from '@stomp/stompjs';
import { get, createWebSocket } from '../util/fetchers.ts';
import { handleTileClick, handleRotateTile, getRotationStyle, generatePlayerList, calculateSalmonPosition, handleGridClick } from './matchUtil.tsx';
import { ColorToRgb } from '../util/ColorParser.ts';
import Chat from '../chat/Chat.tsx'
import Match from '../interfaces/Match.ts';
import Tile from '../interfaces/Tile.ts';
import MatchTile, {emptyTile} from '../interfaces/MatchTile.ts';
import SalmonMatch from '../interfaces/SalmonMatch.ts';
import Player from '../interfaces/Player.ts';

export default function Game({match}: {match: Match}) {
    type GridCell = {tile: MatchTile, salmons: SalmonMatch[]};
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser();
    const [players, setPlayers] = useFetchState<Player[]>([],`/api/v1/players/match/${match.id}`,jwt);
    const [tilesList,setTilesList] = useFetchState<Tile[]>([], '/api/v1/tiles',jwt); // Siempre igual
    const [matchTilesList, setMatchTilesList] = useFetchState<MatchTile[]>([], `/api/v1/matchTiles/match/${match.id}`,jwt) // Todos los front tienen las mismas tiles
    const [salmons, setSalmons] = useFetchState<SalmonMatch[]>([], `/api/v1/salmonMatches/match/${match.id}`, jwt)
    const [salmonAndImages, setSalmonAndImages] = useState<SalmonMatch[]>([])
    const [allDataLoaded, setAllDataLoaded] = useState(false);
    const [matchTiles, setMatchTiles] = useState<MatchTile[]>([]);
    const [gridTiles, setGridTiles] = useState<MatchTile[]>([]);
    const [gridSalmons, setGridSalmons] = useState<SalmonMatch[]>([]);
    const [spawnSalmons, setSpawnSalmons] = useState<SalmonMatch[]>([]);
    const [selectedTile, setSelectedTile] = useState<MatchTile | null>(null);
    const [selectedSalmon, setSelectedSalmon] = useState<SalmonMatch | null>(null);
    const [grid, setGrid] = useState<GridCell[]>(Array(18).fill(null).reverse()); // Cada celda será un array vacío.
    const [gridSea, setGridSea] = useState<SalmonMatch[][]>(Array(4).fill(null).map(() => []));
    const [gridDesove, setGridDesove] = useState<SalmonMatch[][]>(Array(5).fill(null).map(() => []));
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    let playerList: JSX.Element[] = [];
    if (match.actualPlayerId) playerList = generatePlayerList(players, match.actualPlayerId);
    const location = useLocation();
    const spectatorIds:number[] = location.state?.spectatorIds || [];

    const currentUserIsSpectator = spectatorIds.includes(user.id);

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
    
    const handleSalmonClick = (salmon: SalmonMatch) => {
        if (myPlayer) {
            if (myPlayer.id === match.actualPlayerId && myPlayer.id === salmon.playerId && match.phase === 'MOVING')
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
        const newGrid:GridCell[] = Array(18).fill(null).map(() => ({
            tile: emptyTile, 
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
        const newGridSea:SalmonMatch[][] = Array(salmonsPerPlayer).fill(null).map(() => []);
        players.forEach((p) => {
            const pSalmons = salmons.filter(s => s.playerId === p.id);
            if(pSalmons.length > 0) {
                for (let i = 0; i < pSalmons.length; i++) {
                newGridSea[i].push(pSalmons[i]) }
            }})
        setGridSea(newGridSea);
    }, [salmons, players])

    useEffect(() => {
        const newGridDesove:SalmonMatch[][] = Array(5).fill(null).map(() => []);
        const coords = {21: 4, 22: 3, 23: 2, 24: 1, 25: 0}; 
        if(spawnSalmons.length > 0) {
            for (let i = 0; i < spawnSalmons.length; i++) {
            const index = coords[spawnSalmons[i].coordinate.y]
            newGridDesove[index].push(spawnSalmons[i]); }
        }
        setGridDesove(newGridDesove)
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

    return (
        <div className='gamePage-container'>
            <h1 className="game-title game-name">Game: {match.name}</h1>
            <h1 className="game-title game-round">Round: {match.round}</h1>
            <h1 className="game-title game-phase">Phase: {match.phase}</h1>
            {!currentUserIsSpectator && myPlayer && myPlayer.id === match.actualPlayerId && match.phase === 'MOVING' && 
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
            

        {matchTiles.length > 0 && myPlayer &&
        <div key={matchTiles[0].id} className='pick-the-tile-block'
            onClick={() =>
            handleTileClick(matchTiles[0], myPlayer, match, setSelectedTile, setSelectedSalmon)
            }>
                {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === 'TILES' && <h2>Pick the tile!</h2>}
                {<h2>Remaining tiles: {matchTiles.length}</h2>}
                <h2>Next tile:</h2>
                {<img onClick={() => handleTileClick(matchTiles[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
                src={matchTiles[0].tile.image} alt='' style={{width: '150px', ...getRotationStyle(matchTiles[0])} }></img>}
                {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === 'TILES' && 
                (tilesList[matchTiles[0].tile.id-1].type === 'BEAR' || 
                    tilesList[matchTiles[0].tile.id-1].type === 'JUMP')
                && <button onClick={() => handleRotateTile(matchTiles[0], jwt)}>Rotate Tile</button>}
        </div>
        }


        { 
        <div className='game-container'>
            {match.round >= 6 && <div className='grid3'>
                {gridDesove.map((salmon:SalmonMatch[], index) => (
                <div key={index} className="grid-item3">    
                    <div className='salmons-containerSpawn'>
                    {salmon.map((s, i) => ((s.coordinate.y > 20 && <img key = {i} src = {s.salmon.image} alt=""
                        style={{                          
                            filter: `drop-shadow(0px 0px 5px ${ColorToRgb(players.filter(p => p.id === s.playerId)[0].color)}`,
                            border: `3px solid ${ColorToRgb(players.filter(p => p.id === s.playerId)[0].color)}`,
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
            
            <div className={`grid1 ${match.round < 6 ? 'grid1-80' : 'grid1-100'}`}>
            {grid.map((cell, index) => (
            <div key={index} className="grid-item" style={{ position: 'relative' }} onClick={() =>
                handleGridClick(jwt, index, selectedSalmon, setSelectedTile, selectedTile, gridTiles, setSelectedSalmon, match)}>
                
                {cell.tile && (<img src={cell.tile.tile.image} alt="" style={{ width: '250px', ...getRotationStyle(cell.tile)}}/>)}

                {cell.salmons && cell.salmons.map((salmon, sIndex) => {
                    const position = calculateSalmonPosition(sIndex, cell.salmons.length);
                    return (
                        <img key={`salmon-${index}-${sIndex}`} src={salmon.salmon.image} alt="" className="salmon-img"
                            onClick={(e) => {
                                e.stopPropagation();
                                handleSalmonClick(salmon);}}
                            style={{...position,
                                filter: `drop-shadow(0px 0px 2px ${ColorToRgb(players.find(p => p.id === salmon.playerId)?.color)})`,
                                border: `3px solid ${ColorToRgb(players.find(p => p.id === salmon.playerId)?.color)}`}}
                        />
                    );
                    })}
            </div>
            ))}
            </div>
            
            {match.round < 2 &&
            <div className='grid2'>
            {gridSea.map((salmon:SalmonMatch[], index) => (
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
        {myPlayer && <Chat match={match} players={players} currentPlayer={myPlayer} />}
    </div>
    )

}