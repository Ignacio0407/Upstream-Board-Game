import { useState, useEffect, JSX } from 'react';
import { useLocation, useNavigate, useParams } from "react-router-dom";
import tokenService from '../services/token.service.ts'
import useFetchState from "../util/useFetchState.ts";
import '../static/css/game/game.css'
import { GridCell, handleTileClick, handleSalmonClick, handleRotateTile, getRotationStyle, generatePlayerList, calculateSalmonPosition, handleGridClick, renderPlayerList, passTurn, buildGridFromSnapshot, updateGridWithSalmons, updateGridWithTile, updateGridWithMovement, coordToIndex, updateGridWithMovementFromSea, updateGridWithHerons } from './matchUtil.tsx';
import { ColorToRgb } from '../util/ColorParser.ts';
import Chat from '../components/chat/Chat.tsx'
import Match, { emptyMatch } from '../interfaces/Match.ts';
import MatchTile, {emptyTile} from '../interfaces/MatchTile.ts';
import SalmonMatch from '../interfaces/SalmonMatch.ts';
import Player from '../interfaces/Player.ts';
import WebSocketManager from '../util/webSocketManager.ts';
import { TileType } from '../interfaces/Tile.ts';
import { Phase } from '../interfaces/Phase.ts';
import { State } from '../interfaces/State.ts';

export default function Game() {
    const navigate = useNavigate();
    const jwt = tokenService.getLocalAccessToken();
    const { matchId } = useParams<{ matchId: string }>();
    const [match, setMatch] = useFetchState<Match>(emptyMatch, matchId ? `/api/v1/matches/${encodeURIComponent(String(matchId))}` : null, jwt);
    const user = tokenService.getUser();
    const [players, setPlayers] = useFetchState<Player[]>([],`/api/v1/players/match/${encodeURIComponent(String(matchId))}`,jwt);
    const [matchTiles, setMatchTiles] = useFetchState<MatchTile[]>([], `/api/v1/matchTiles/match/${encodeURIComponent(String(matchId))}`, jwt);
    const [matchTilesNoCoordinate, setMatchTilesNoCoordinate] = useState<MatchTile[]>([]);
    const [salmons, setSalmons] = useFetchState<SalmonMatch[]>([], `/api/v1/salmonMatches/match/${encodeURIComponent(String(matchId))}`, jwt);
    const [allDataLoaded, setAllDataLoaded] = useState(false);
    const [selectedTile, setSelectedTile] = useState<MatchTile | null>(null);
    const [selectedSalmon, setSelectedSalmon] = useState<SalmonMatch | null>(null);
    const [grid, setGrid] = useState<GridCell[]>(Array(18).fill(null).reverse().map(() => ({ tile: emptyTile, salmons: [] })));
    const [gridSea, setGridSea] = useState<SalmonMatch[][]>(Array(4).fill(null).map(() => []));
    const [gridSpawn, setGridSpawn] = useState<SalmonMatch[][]>(Array(5).fill(null).map(() => []));
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    let playerList: JSX.Element[] = [];
    if (match.actualPlayerId) playerList = generatePlayerList(players, match.actualPlayerId);
    const location = useLocation();
    const spectatorIds:number[] = location.state?.spectatorIds || [];
    const currentUserIsSpectator = spectatorIds.includes(user.id);
    interface TilesAndSalmons { salmonMatches: SalmonMatch[], matchTiles: MatchTile[] }
    interface SalmonMovement { salmon: SalmonMatch, myTile: MatchTile, toTravel: MatchTile }
    interface TileAndSalmon { salmonMatch: SalmonMatch, matchTile: MatchTile }

    // Cada vez que cambian los salmones o los matchTiles pueden cambiar los jugadores, y mejor devolverlos siempre ordenados.

    // To keep everything right after reloading the page.
    useEffect(() => {
        if (!allDataLoaded) return;
        setMatchTilesNoCoordinate(matchTiles.filter(t => !t.coordinate));
        setGrid(buildGridFromSnapshot(matchTiles.filter(t => t.coordinate !== null), salmons));
        if (match.round >= 6) {
            const newGridDesove: SalmonMatch[][] = Array(5).fill(null).map(() => []);
            let spawnIndex: number | null = null;
            salmons.forEach(s => {
            if (s.coordinate) spawnIndex = s.coordinate.y - 21;
            if (spawnIndex !== null && spawnIndex >= 0 && spawnIndex <= newGridDesove.length)
                newGridDesove[spawnIndex].push(s);
            });
            setGridSpawn(newGridDesove);
        }
    }, [allDataLoaded]);

    useEffect(() => {
        if (match && players.length > 0 && matchTiles.length > 0 && salmons.length > 0) {
            setAllDataLoaded(true);
        }
    }, [match, players, matchTiles, salmons]);

    useEffect(() => {
        const me = players.find(p => p.userId === user.id);
        if (me) setMyPlayer(me);
    }, [players, match, salmons, matchTiles]);

    useEffect(() => {
        const wsManager = new WebSocketManager(jwt);
        wsManager.initConnection();

        wsManager.addSubscription<Match>(`match/update/${matchId}`, (updatedMatch) => {
            setMatch(updatedMatch)
            if (updatedMatch.state === State.FINALIZED)
                navigate(`/matches/${matchId}/end`)
        });

        wsManager.addSubscription<SalmonMovement>(`movingSalmon/${matchId}`, ({ salmon, myTile, toTravel }) => {
            console.log("salmon", salmon)
            setMatchTiles(prev => prev.map(tile => {
                if (tile.id === myTile.id) return myTile;
                if (tile.id === toTravel.id) return toTravel;
                return tile;
                })
            );
            setSalmons(prev => prev.map(s => s.id === salmon.id ? salmon : s).filter(s => s.salmonsNumber > 0));
            setGrid(prev => updateGridWithMovement(prev, salmon, myTile, toTravel));
        });

        wsManager.addSubscription<SalmonMovement>(`movingSalmonFromSea/${matchId}`, ({ salmon, toTravel }) => {
            console.log("salmon from sea", salmon)
            setSalmons(prev => prev.map(s => s.id === salmon.id ? salmon : s).filter(s => s.salmonsNumber > 0));
            setMatchTiles(prev => prev.map(tile => {
                if (tile.id === toTravel.id) return toTravel;
                return tile;
                })
            );
            setGrid(prev => updateGridWithMovementFromSea(prev, salmon, toTravel));
        });
        
        wsManager.addSubscription<TileAndSalmon>(`salmon/spawn/enter/${matchId}`, ({salmonMatch, matchTile}) => {
            setSalmons(prev => prev.map(s => s.id === salmonMatch.id ? salmonMatch : s));
            setGrid(prev => {
                const newGrid = [...prev];
                const index = coordToIndex(matchTile.coordinate.x, matchTile.coordinate.y);
                newGrid[index] = {...newGrid[index],
                    tile: matchTile,
                    salmons: newGrid[index].salmons.filter(s => s.id !== salmonMatch.id),
                };
                return newGrid;
            });
            setGridSpawn(prev => {
                const newGridSpawn = [...prev];
                if (!newGridSpawn[0].some(s => s.id === salmonMatch.id)) newGridSpawn[0].push(salmonMatch);
                return newGridSpawn;
            });
        });

        wsManager.addSubscription<SalmonMatch[]>(`salmons/spawn/${matchId}`, () => {
            setGridSpawn(prev => {
                const newGridSpawn = prev.map(row => [...row]); // Deep copy
                // i = 3 into i=4, delete i=3, i=2 into i=3, delete i=2 ...
                for (let i = newGridSpawn.length - 2; i >= 0; i--) {
                    newGridSpawn[i + 1] = [...newGridSpawn[i + 1], ...newGridSpawn[i]];
                    newGridSpawn[i] = [];
                }
                return newGridSpawn;
            });
        });

        // Update tile when placed on the board.
        wsManager.addSubscription<MatchTile>(`tiles/place/${matchId}`, (updatedTile) => { 
            setMatchTilesNoCoordinate(prev => {
                const newTiles = [...prev];
                newTiles.shift();
                return newTiles;
            });
            setMatchTiles(prev => prev.map(tile => tile.id === updatedTile.id ? updatedTile : tile));
            setGrid(prev => updateGridWithTile(prev, updatedTile));
        });

        wsManager.addSubscription<TilesAndSalmons>(`tilesAndSalmons/${matchId}`, ({ matchTiles, salmonMatches }) => {
            setSalmons(salmonMatches);
            setMatchTiles(matchTiles);
            setGrid(buildGridFromSnapshot(matchTiles, salmonMatches));
        });

        wsManager.addSubscription<SalmonMatch[]>(`herons/${matchId}`, (salmonMatches) => {
            setSalmons(prev => {
            const updatedMap = new Map(salmonMatches.map(s => [s.id, s]));
            const merged = prev.map(s => updatedMap.get(s.id) ?? s)
            return merged;
            });
            setGrid(prev => updateGridWithSalmons(prev, salmonMatches));
        });

        wsManager.addSubscription<TilesAndSalmons & { deletedSalmonIds?: number[] }>(`heronsUpdated/${matchId}`, ({ matchTiles, salmonMatches, deletedSalmonIds }) => {
            console.log("deletedIds", deletedSalmonIds)
            setSalmons(prev => { const updatedMap = new Map(salmonMatches.map(s => [s.id, s]));
            return prev.filter(s => !deletedSalmonIds?.includes(s.id)).map(s => updatedMap.get(s.id) ?? s);
            });
            setMatchTiles(prev => prev.map(tile => {
                const updated = matchTiles.find(t => t.id === tile.id);
                return updated ?? tile;
            }));
            setGrid(prev => updateGridWithHerons(prev, matchTiles, salmonMatches, deletedSalmonIds));
        });

        wsManager.addSubscription<MatchTile>(`tiles/rotation/${matchId}`, (updatedTile) => {
            setMatchTilesNoCoordinate(prev => prev.map(tile => tile.id === updatedTile.id ? updatedTile : tile));
        });

        wsManager.addSubscription(`players/${matchId}`, (updatedPlayers:Player[]) => {
            setPlayers(updatedPlayers);
            setMyPlayer(updatedPlayers.filter(p => p.userId === user.id)[0]);
        });

        return () => {
            wsManager.deactivate(); // 🔹 Closes on game exit.
        };
    }, []); // It only opens the connection once.

    useEffect(() => {
        if (match.round < 2) {
            const salmonsPerPlayer = 4;
            const newGridSea:SalmonMatch[][] = Array(salmonsPerPlayer).fill(null).map(() => []);
            players.forEach((p) => {
                const pSalmons = salmons.filter(s => s.playerId === p.id);
                if(pSalmons.length > 0) {
                    for (let i = 0; i < pSalmons.length; i++) {
                    newGridSea[i].push(pSalmons[i]) }
            }})
            setGridSea(newGridSea);
        }
    }, [salmons, players])

    if (!allDataLoaded) {
        return <div style={{justifySelf:'center'}}>Loading data</div>;
    }

    return (
        <div className='gamePage-container'>
            <h1 className="game-title game-name">Game: {match.name}</h1>
            <h1 className="game-title game-round">Round: {match.round}</h1>
            <h1 className="game-title game-phase">Phase: {match.phase}</h1>
            {!currentUserIsSpectator && myPlayer && myPlayer.id === match.actualPlayerId && match.phase === Phase.MOVING && 
            <h1 className="game-title game-turn move-your-salmons">Move your salmons!</h1>}
            {renderPlayerList(playerList)}
            {myPlayer && !currentUserIsSpectator && passTurn(match, jwt, myPlayer)}
            
        {matchTilesNoCoordinate.length > 0 && myPlayer &&
        <div role="button" tabIndex={0} key={matchTilesNoCoordinate[0].id} className='pick-the-tile-block'
            onClick={() => handleTileClick(matchTilesNoCoordinate[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
            onKeyDown={(e) => e.key === 'Enter' && handleTileClick(matchTilesNoCoordinate[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
            >
                {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === Phase.TILES && <h2>Pick the tile!</h2>}
                {<h2>Remaining tiles: {matchTilesNoCoordinate.length}</h2>}
                <h2>Next tile:</h2>
                {<img onClick={() => handleTileClick(matchTilesNoCoordinate[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
                src={matchTilesNoCoordinate[0].tile.image} alt='' style={{width: '150px', ...getRotationStyle(matchTilesNoCoordinate[0])} }></img>}
                {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === Phase.TILES && 
                (matchTilesNoCoordinate[0].tile.type === TileType.BEAR || matchTilesNoCoordinate[0].tile.type === TileType.JUMP)
                && <button onClick={() => handleRotateTile(matchTilesNoCoordinate[0], jwt)}>Rotate Tile</button>}
        </div>
        }


        { 
        <div className='game-container'>
            {match.round >= 6 && <div className='grid3'>
                {[...gridSpawn].reverse().map((salmons:SalmonMatch[], index) => (
                <div key={index} className="grid-item3">    
                    <div className='salmons-containerSpawn'>
                    {salmons.map((s, i) => ((s.coordinate.y > 20 && <img key = {i} src = {s.salmon.image} alt=""
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
                handleGridClick(jwt, index, selectedSalmon, setSelectedTile, selectedTile, setSelectedSalmon, match, match.actualPlayerId, myPlayer.id, grid)}>
                
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