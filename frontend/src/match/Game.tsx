import { useState, useEffect, JSX } from 'react';
import { useLocation } from "react-router-dom";
import tokenService from '../services/token.service.ts'
import useFetchState from "../util/useFetchState.ts";
import '../static/css/game/game.css'
import { handleTileClick, handleSalmonClick, handleRotateTile, getRotationStyle, generatePlayerList, calculateSalmonPosition, handleGridClick, renderPlayerList, fetchSortedPlayers } from './matchUtil.tsx';
import { ColorToRgb } from '../util/ColorParser.ts';
import Chat from '../components/chat/Chat.tsx'
import Match from '../interfaces/Match.ts';
import Tile from '../interfaces/Tile.ts';
import MatchTile, {emptyTile} from '../interfaces/MatchTile.ts';
import SalmonMatch from '../interfaces/SalmonMatch.ts';
import Player from '../interfaces/Player.ts';
import WebSocketManager from '../util/webSocketManager.ts';

export default function Game({match}: {match: Match}) {
    type GridCell = {tile: MatchTile, salmons: SalmonMatch[]};
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser();
    const [players, setPlayers] = useFetchState<Player[]>([],`/api/v1/players/match/${match.id}`,jwt);
    const [tilesList,setTilesList] = useFetchState<Tile[]>([], '/api/v1/tiles',jwt); // Siempre igual
    const [matchTilesNoCoordinate, setMatchTilesNoCoordinate] = useFetchState<MatchTile[]>([], `/api/v1/matchTiles/match/${match.id}`,jwt) // Todos los front tienen las mismas tiles
    const [salmons, setSalmons] = useFetchState<SalmonMatch[]>([], `/api/v1/salmonMatches/match/${match.id}`, jwt);
    const [allDataLoaded, setAllDataLoaded] = useState(false);
    const [gridTiles, setGridTiles] = useState<MatchTile[]>([]);
    const [gridSalmons, setGridSalmons] = useState<SalmonMatch[]>([]);
    const [spawnSalmons, setSpawnSalmons] = useState<SalmonMatch[]>([]);
    const [selectedTile, setSelectedTile] = useState<MatchTile | null>(null);
    const [selectedSalmon, setSelectedSalmon] = useState<SalmonMatch | null>(null);
    const [grid, setGrid] = useState<GridCell[]>(Array(18).fill(null).reverse()); // Each cell will be an empty array.
    const [gridSea, setGridSea] = useState<SalmonMatch[][]>(Array(4).fill(null).map(() => []));
    const [gridDesove, setGridDesove] = useState<SalmonMatch[][]>(Array(5).fill(null).map(() => []));
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    let playerList: JSX.Element[] = [];
    if (match.actualPlayerId) playerList = generatePlayerList(players, match.actualPlayerId);
    const location = useLocation();
    const spectatorIds:number[] = location.state?.spectatorIds || [];
    const currentUserIsSpectator = spectatorIds.includes(user.id);

    // Cada vez que cambian los salmones o los matchTiles pueden cambiar los jugadores, y mejor devolverlos siempre ordenados.

    useEffect(() => {
        if (players.length > 0 && tilesList.length > 0 && matchTilesNoCoordinate.length > 0 && salmons.length > 0) {
            setAllDataLoaded(true);
            fetchSortedPlayers(match, jwt, setPlayers);
            const matchTilesWithCoordinate = matchTilesNoCoordinate.filter(mt => mt.coordinate != null)
            //setGridTiles(matchTilesWithCoordinate);
            const salmonsWithCoordinate = salmons.filter(s => s.coordinate != null);
            //setGridSalmons(salmonsWithCoordinate)
        }
    }, [match.id])

    useEffect(() => {
        const salmonMatchesWCoord = [...salmons].filter(s => s.coordinate !== null);
        setGridSalmons(salmonMatchesWCoord);
    }, [salmons]);

    useEffect(() => {
        setMyPlayer(players.filter(p => p.userId === user.id)[0]);
    }, [salmons, gridTiles])

    useEffect(() => {
        const wsManager = new WebSocketManager(jwt);
        wsManager.initConnection();

        wsManager.addSubscription<SalmonMatch>('salmon', (updatedSalmon) => {
            setSalmons(prev => prev.map(s => s.id === updatedSalmon.id ? updatedSalmon : s));
        });

        wsManager.addSubscription<SalmonMatch[]>('salmons/herons', (updatedSalmons) => {
            setSalmons(prev =>
                prev.map(s => {
                    const updated = updatedSalmons.find(u => u.id === s.id);
                    return updated ? updated : s;   
                })
            );
        });

        wsManager.addSubscription<MatchTile>('tiles/place', (updatedTile) => {
            setMatchTilesNoCoordinate(prev => {
                const copy = [...prev];
                copy.shift();
                return copy});
            setGridTiles(prev => [...prev, updatedTile]);
        });

        wsManager.addSubscription<MatchTile>('tiles', (updatedTile) => {
            setGridTiles(prev => [...prev, updatedTile]);
        });

        wsManager.addSubscription<MatchTile>('tiles/rotation', (updatedTile) => {
            setMatchTilesNoCoordinate(prev => prev.map(tile => tile.id === updatedTile.id ? updatedTile : tile));
        });

        wsManager.addSubscription(`players/${match.id}`, (updatedPlayers:Player[]) => {
            setPlayers(updatedPlayers);
            setMyPlayer(updatedPlayers.filter(p => p.userId === user.id)[0]);
        });

        return () => {
            wsManager.deactivate(); // 🔹 Closes on game exit.
        };
    }, [match.id]); // Using match.id as trigger, it only opens the connection once.

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
        const coords: { [key: number]: number } = {21: 4, 22: 3, 23: 2, 24: 1, 25: 0};
        if(spawnSalmons.length > 0) {
            for (let i = 0; i < spawnSalmons.length; i++) {
            const index = coords[spawnSalmons[i].coordinate.y]
            newGridDesove[index].push(spawnSalmons[i]); }
        }
        setGridDesove(newGridDesove)
    }, [spawnSalmons])

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
            {renderPlayerList(playerList)}
            
        {matchTilesNoCoordinate.length > 0 && myPlayer &&
        <div key={matchTilesNoCoordinate[0].id} className='pick-the-tile-block'
            onClick={() =>
            handleTileClick(matchTilesNoCoordinate[0], myPlayer, match, setSelectedTile, setSelectedSalmon)
            }>
                {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === 'TILES' && <h2>Pick the tile!</h2>}
                {<h2>Remaining tiles: {matchTilesNoCoordinate.length}</h2>}
                <h2>Next tile:</h2>
                {<img onClick={() => handleTileClick(matchTilesNoCoordinate[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
                src={matchTilesNoCoordinate[0].tile.image} alt='' style={{width: '150px', ...getRotationStyle(matchTilesNoCoordinate[0])} }></img>}
                {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === 'TILES' && 
                (tilesList[matchTilesNoCoordinate[0].tile.id-1].type === 'BEAR' || 
                    tilesList[matchTilesNoCoordinate[0].tile.id-1].type === 'JUMP')
                && <button onClick={() => handleRotateTile(matchTilesNoCoordinate[0], jwt)}>Rotate Tile</button>}
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
            {myPlayer && grid.map((cell, index) => (
            <div key={index} className="grid-item" style={{ position: 'relative' }} onClick={() =>
                handleGridClick(jwt, index, selectedSalmon, setSelectedTile, selectedTile, setSelectedSalmon, match, match.actualPlayerId, myPlayer.id, gridTiles)}>
                
                {cell.tile && (<img src={cell.tile.tile.image} alt="" style={{ width: '250px', ...getRotationStyle(cell.tile)}}/>)}

                {cell.salmons && cell.salmons.map((salmon, sIndex) => {
                    const position = calculateSalmonPosition(sIndex, cell.salmons.length);
                    return (
                        <img key={`salmon-${index}-${sIndex}`} src={salmon.salmon.image} alt="" className="salmon-img"
                            onClick={(e) => {
                                e.stopPropagation();
                                handleSalmonClick(salmon, myPlayer, match, setSelectedSalmon)}}
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
                        (s.coordinate === null && <img key = {i} src = {s.salmon.image} alt="" onClick={() => handleSalmonClick(s, myPlayer, match, setSelectedSalmon)}
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