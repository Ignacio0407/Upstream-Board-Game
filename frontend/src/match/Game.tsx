import { useState, useEffect, JSX, useRef } from 'react';
import { useLocation, useNavigate, useParams } from "react-router-dom";
import tokenService from '../services/token.service.ts'
import useFetchState from "../util/useFetchState.ts";
import '../static/css/game/game.css'
import { GridCell, handleTileClick, handleSalmonClick, handleRotateTile, getRotationStyle, generatePlayerList, calculateSalmonPosition, renderPlayerList, buildGridFromSnapshot, updateGridWithTile, PendingMove, GRID_WIDTH, GRID_HEIGHT, confirmMovementBatch, handleUpdateTilePosition, handleNoMoves } from './matchUtil.tsx';
import { ColorToRgb } from '../lobby/ColorParser.ts';
import Chat from '../components/chat/Chat.tsx'
import Match, { emptyMatch } from '../interfaces/Match.ts';
import MatchTile, {emptyTile} from '../interfaces/MatchTile.ts';
import SalmonMatch from '../interfaces/SalmonMatch.ts';
import Player from '../interfaces/Player.ts';
import WebSocketManager from '../components/webSocketManager/webSocketManager.ts';
import { TileType } from '../interfaces/Tile.ts';
import { Phase } from '../interfaces/Phase.ts';
import { State } from '../interfaces/State.ts';
import { calculateValidMoves, ValidMove } from './calculateValidMoves.ts';
import ConfirmModal from '../util/ConfirmModal.tsx';

export default function Game() {
    const navigate = useNavigate();
    const jwt = tokenService.getLocalAccessToken();
    const { matchId } = useParams<{ matchId: string }>();
    const [match, setMatch] = useFetchState<Match>(emptyMatch, matchId ? `/api/v1/matches/${matchId}` : null, jwt);
    const user = tokenService.getUser();
    const [players, setPlayers] = useFetchState<Player[]>([],`/api/v1/players/match/${matchId}`,jwt);
    const [matchTiles, setMatchTiles] = useFetchState<MatchTile[]>([], `/api/v1/matchTiles/match/${matchId}`, jwt);
    const [matchTilesNoCoordinate, setMatchTilesNoCoordinate] = useState<MatchTile[]>([]);
    const [salmons, setSalmons] = useFetchState<SalmonMatch[]>([], `/api/v1/salmonMatches/match/${matchId}`, jwt);
    const [allDataLoaded, setAllDataLoaded] = useState(false);
    const [selectedTile, setSelectedTile] = useState<MatchTile | null>(null);
    const [selectedSalmon, setSelectedSalmon] = useState<SalmonMatch | null>(null);
    const [grid, setGrid] = useState<GridCell[]>(new Array(18).fill(null).reverse().map(() => ({ tile: emptyTile, salmons: [] })));
    const [gridSea, setGridSea] = useState<SalmonMatch[][]>(new Array(4).fill(null).map(() => []));
    const [gridSpawn, setGridSpawn] = useState<SalmonMatch[][]>(new Array(5).fill(null).map(() => []));
    const [myPlayer, setMyPlayer] = useState<Player | null>(null);
    let playerList: JSX.Element[] = [];
    if (match.actualPlayerId) playerList = generatePlayerList(players, match.actualPlayerId);
    const location = useLocation();
    const spectatorIds:number[] = location.state?.spectatorIds || [];
    const currentUserIsSpectator = spectatorIds.includes(user.id);
    interface TilesAndSalmons { salmonMatches: SalmonMatch[], matchTiles: MatchTile[] }
    const [pendingMoves, setPendingMoves] = useState<PendingMove[]>([]);
    const [validMoves, setValidMoves] = useState<ValidMove[]>([]);
    const [history, setHistory] = useState<{salmons: SalmonMatch[], matchTiles: MatchTile[], energy: number}[]>([]);
    const [visible, setVisible] = useState(false);
    const [confirmBatchIsSubmitting, setConfirmBatchIsSubmitting] = useState(false); //Remove race condition when confirming batch, to avoid double click and send twice the same batch.
    const wsManagerRef = useRef<WebSocketManager | null>(null);

    // Cada vez que cambian los salmones o los matchTiles pueden cambiar los jugadores, y mejor devolverlos siempre ordenados.

    // To keep everything right after reloading the page.
    useEffect(() => {
        if (!allDataLoaded) return;
        setMatchTilesNoCoordinate(matchTiles.filter(t => !t.coordinate));
        setGrid(buildGridFromSnapshot(matchTiles.filter(t => t.coordinate !== null), salmons.filter(s => s.salmonsNumber > 0)));
        if (match.round >= 6) {
            const newGridDesove: SalmonMatch[][] = new Array(5).fill(null).map(() => []);
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
        if (match.id !== 0 && players.length > 0 && matchTiles.length > 0 && salmons.length > 0) {
            setAllDataLoaded(true);
        }
    }, [match, players, matchTiles, salmons]);

    useEffect(() => {
        const me = players.find(p => p.userId === user.id);
        if (me) setMyPlayer(me);
    }, [players, match, salmons, matchTiles]);

    // --- Handlers for local movement ---
    const onSalmonClickWrapper = (salmon: SalmonMatch) => {
        // Allow changing salmon in any moment
        handleSalmonClick(salmon, myPlayer, match, setSelectedSalmon);
        // If it is my turn, calculate where this salmon can move
        if (myPlayer && myPlayer.id === match.actualPlayerId && myPlayer.id === salmon.playerId && match.phase === Phase.MOVING) {
            const moves = calculateValidMoves(salmon, grid, myPlayer.energy, match.playersNumber);
            setValidMoves(moves);
            if (moves.length === 0)
                handleNoMoves(myPlayer, match, jwt, history, visible, setVisible, handleConfirmBatch);
        }
        else setValidMoves([]);
    };

    const onGridClickWrapper = (index: number) => {
        if (!selectedSalmon || !myPlayer) return;
        const x = index % GRID_WIDTH;
        const y = GRID_HEIGHT - 1 - Math.floor(index / GRID_WIDTH);
        const move = validMoves.find(m => m.x === x && m.y === y);
        if (!move) return; // Illegal move, ignored
        // Save Backup before starting turn in case of error
        setHistory(prev => [...prev, structuredClone({ salmons, matchTiles, energy: myPlayer.energy })]);
        // 1. Discount energy
        const newEnergy = myPlayer.energy - move.energyUsed;
        setMyPlayer({ ...myPlayer, energy: newEnergy });
        setPlayers(prev => prev.map(p => p.id === myPlayer.id ? { ...p, energy: newEnergy } : p));
        // 2. Calculate salmon losses
        const newSalmonsNumber = Math.max(0, selectedSalmon.salmonsNumber - move.salmonLoss);
        let newImage = selectedSalmon.salmon.image;
        if (newSalmonsNumber === 1 && newImage.includes('2')) newImage = newImage.replace('2', '1');
        const updatedSalmon = { ...selectedSalmon, salmonsNumber: newSalmonsNumber, 
            salmon: { ...selectedSalmon.salmon, image: newImage }, coordinate: { x, y } };
        // 3. Create new lists and update grid
        const updatedSalmonsList = salmons.map(s => s.id === updatedSalmon.id ? updatedSalmon : s).filter(s => s.salmonsNumber > 0);
        const mysalmons = updatedSalmonsList.filter(s => s.playerId === myPlayer?.id && s.salmonsNumber > 0 && 
            ( !s.coordinate || (s.coordinate.y < 20 && !(s.coordinate.x === 1 && s.coordinate.y === 5))));
        if (mysalmons.length === 0)
            ConfirmModal({ visible: visible, setVisible: setVisible, onConfirm: handleConfirmBatch, title: "Death", message: "You are going to lose, check any possible moves, if there are none, please confirm", confirmText: "Confirm", cancelText: "Cancel" });
        const finalX = move.x ? Number(move.x) : Number(x);
        const finalY = move.y ? Number(move.y) : Number(y);
        const exitX = selectedSalmon.coordinate ? Number(selectedSalmon.coordinate.x) : null;
        const exitY = selectedSalmon.coordinate ? Number(selectedSalmon.coordinate.y) : null;
        const nextMatchTiles = matchTiles.map(t => {
            const newT = { ...t };
            if (!newT.coordinate) return newT;
            const tileX = Number(newT.coordinate.x);
            const tileY = Number(newT.coordinate.y);
            // 1. Remove salmon from exit
            if (exitX !== null && exitY !== null && tileX === exitX && tileY === exitY)
                newT.salmonsNumber = Number(newT.salmonsNumber) - 1;
            // 2. Add salmon to destination
            if (tileX === finalX && tileY === finalY)
                newT.salmonsNumber = Number(newT.salmonsNumber) + 1;
            // 3. Kill eagle if it's the destination or the jumped square
            if ((tileX === finalX && tileY === finalY) || 
                (move.jumpedCoords && tileX === Number(move.jumpedCoords.x) && tileY === Number(move.jumpedCoords.y))) {
                if (newT.tile?.type === 'EAGLE') { 
                    newT.tile = { ...newT.tile, type: 'WATER' as any }; 
                    if (newT.tile.image) newT.tile.image = newT.tile.image.replace('eagle', 'water');
                }
            }
            return newT;
        });
        const nextGrid = buildGridFromSnapshot(nextMatchTiles, updatedSalmonsList);
        // 5. Save React states
        setSalmons(updatedSalmonsList);
        setMatchTiles(nextMatchTiles); 
        setGrid(nextGrid);
        const backendY = move.isJump ? y - 1 : y;
        // 6. Add to history
        if (x === 1 && y === 5 && match.round >= 6) {
            setPendingMoves(prev => [...prev, { salmonId: updatedSalmon.id, x, y:backendY, enterSpawn: true }]);
        }
        else {
            let backendX = x;
            if (move.isJump && Math.abs(move.x - selectedSalmon.coordinate?.x) === 2) backendX = move.jumpedCoords!.x;
            setPendingMoves(prev => [...prev, { salmonId: updatedSalmon.id, x: backendX, y: backendY }]);
        }
        setSelectedSalmon(updatedSalmon);
        if (mysalmons.length === 0) {
            setMyPlayer({ ...myPlayer, energy: 0 });
            setPlayers(prev => prev.map(p => p.id === myPlayer.id ? { ...p, energy: 0 } : p));
        }
        // 7. Recalculate next moves with the NEW DATA
        if (updatedSalmon.salmonsNumber > 0) {
            const nextMoves = calculateValidMoves(updatedSalmon, nextGrid, newEnergy, match.playersNumber);
            setValidMoves(nextMoves);
            if (nextMoves.length === 0)
                handleNoMoves(myPlayer, match, jwt, history, visible, setVisible, handleConfirmBatch);
        }
        else setValidMoves([])
        
    };

    // --- Handlers to confirm/undo batch ---
    const handleConfirmBatch = () => {
        if (pendingMoves.length === 0) return;
        // Reset visual selections
        setConfirmBatchIsSubmitting(true);
        setValidMoves([]);
        setSelectedSalmon(null);
        confirmMovementBatch({pendingMoves, jwt, match, myPlayerId: myPlayer!.id, initialState: history[0], setConfirmBatchIsSubmitting,
            restoreSnapshot: (backup: any) => {
                setMatchTiles(backup.matchTiles);
                setSalmons(backup.salmons.filter((s:SalmonMatch) => s.salmonsNumber > 0));
                
                setGrid(buildGridFromSnapshot(backup.matchTiles, backup.salmons.filter((s:SalmonMatch) => s.salmonsNumber > 0)));
                // setGrid(backup.grid);
                
                setMyPlayer(prev => prev ? { ...prev, energy: backup.energy } : null);
                setPlayers(prev => prev.map(p => p.id === myPlayer!.id ? { ...p, energy: backup.energy } : p));
            },
            clearPath: () => {
                // Success callback
                setPendingMoves([]);
                setHistory([]);
            }
        });
    };

    const handleUndoLastMove = () => {
        setHistory(prevHistory => {
            if (prevHistory.length === 0) return prevHistory;
            const lastState = prevHistory.at(-1)!;
            // Restore "father" data
            setSalmons(lastState.salmons.filter(s => s.salmonsNumber > 0));
            setMatchTiles(lastState.matchTiles);
            setPlayers(playersList => playersList.map(p => p.id === myPlayer!.id ? { ...p, energy: lastState.energy } : p));
            setMyPlayer(p => p ? { ...p, energy: lastState.energy } : null);
            // Rebuild the grid from the last state
            setGrid(buildGridFromSnapshot(lastState.matchTiles, lastState.salmons.filter(s => s.salmonsNumber > 0)));
            // Clean lists
            setPendingMoves(pm => pm.slice(0, -1));
            setValidMoves([]);
            setSelectedSalmon(null);
            return prevHistory.slice(0, -1);
        });
    };

    const handleUndoAll = () => {
        if (history.length === 0) return;
        // Original state is in index 0
        const firstState = history[0]; 
        setSalmons(firstState.salmons.filter(s => s.salmonsNumber > 0));
        setMatchTiles(firstState.matchTiles);
        setPlayers(prev => prev.map(p => p.id === myPlayer!.id ? { ...p, energy: firstState.energy } : p));
        setMyPlayer(prev => prev ? { ...prev, energy: firstState.energy } : null);
        setGrid(buildGridFromSnapshot(firstState.matchTiles, firstState.salmons.filter(s => s.salmonsNumber > 0)));
        setHistory([]);
        setPendingMoves([]);
        setValidMoves([]);
        setSelectedSalmon(null);
    };

    useEffect(() => {
        const wsManager = new WebSocketManager(jwt);
        wsManagerRef.current = wsManager;
        wsManager.initConnection();

        wsManager.addSubscription<Match>(`match/update/${matchId}`, (updatedMatch) => {
            setMatch(updatedMatch)
            if (updatedMatch.state === State.FINALIZED)
                navigate(`/matches/${matchId}/end`)
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

        wsManager.addSubscription<MatchTile>(`tiles/rotation/${matchId}`, (updatedTile) => {
            setMatchTilesNoCoordinate(prev => prev.map(tile => tile.id === updatedTile.id ? { ...tile, orientation: updatedTile.orientation } : tile));
            setSelectedTile(prev => (prev?.id === updatedTile.id ? { ...prev, orientation: updatedTile.orientation } : prev));
        });

        wsManager.addSubscription(`players/${matchId}`, (updatedPlayers:Player[]) => {
            setPlayers(updatedPlayers);
            setMyPlayer(updatedPlayers.find(p => p.userId === user.id) || null);
        });

        wsManager.addSubscription<TilesAndSalmons>(`tilesAndSalmons/${matchId}`, ({ matchTiles: updatedTiles, salmonMatches: updatedSalmons }) => {
            setMatchTiles(prevTiles => {
                const tileMap = new Map(updatedTiles.map(t => [t.id, t]));
                const mergedTiles = prevTiles.map(t => tileMap.get(t.id) ?? t);
                setSalmons(prevSalmons => {
                    const salmonMap = new Map(updatedSalmons.map(s => [s.id, s]));
                    const mergedSalmons = prevSalmons.map(s => salmonMap.get(s.id) ?? s).filter(s => s.salmonsNumber > 0);
                    setGrid(buildGridFromSnapshot(mergedTiles, mergedSalmons));
                    return mergedSalmons;
                });
                return mergedTiles;
            });

            updatedSalmons.forEach(s => {
                if (s.coordinate?.x === 1 && s.coordinate?.y === 21) {
                    setGridSpawn(prev => {
                        if (prev[0].some(sp => sp.id === s.id)) return prev;
                        const newGridSpawn = [...prev];
                        newGridSpawn[0] = [...newGridSpawn[0], s];
                        return newGridSpawn;
                    });
                }
            });
        });

        wsManager.addSubscription<TilesAndSalmons>(`tilesAndSalmonsByMatchId/${matchId}`, ({ matchTiles, salmonMatches }) => {
            setSalmons(salmonMatches.filter(s => s.salmonsNumber > 0));
            setMatchTiles(matchTiles);
            setGrid(buildGridFromSnapshot(matchTiles, salmonMatches.filter(s => s.salmonsNumber > 0)));
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

        return () => {
            wsManager.deactivate(); // 🔹 Closes on game exit.
        };
    }, []); // It only opens the connection once.

    useEffect(() => {
        if (match.round < 2) {
            const salmonsPerPlayer = 4;
            const newGridSea:SalmonMatch[][] = new Array(salmonsPerPlayer).fill(null).map(() => []);
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
            {/*Batch controls*/}
            {(pendingMoves.length > 0) && (
                <div className="game-title game-turn move-your-salmons batch-controls" style={{ marginBottom: '70px', display: 'flex', gap: '10px' }}>
                    {myPlayer?.energy === 0 && <button type="button" className="btn btn-success" disabled={confirmBatchIsSubmitting} onClick={handleConfirmBatch}>Confirm</button>}
                    <button type="button" className="btn btn-warning" onClick={handleUndoLastMove}>Undo last</button>
                    <button type="button" className="btn btn-danger" onClick={handleUndoAll}>Undo all</button>
                </div>
            )}
            {renderPlayerList(playerList)}
            
        {matchTilesNoCoordinate.length > 0 && myPlayer && 
        <div key={matchTilesNoCoordinate[0].id} className='pick-the-tile-block'>
            {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === Phase.TILES && <h2>Pick the tile!</h2>}
            <h2>Remaining tiles: {matchTilesNoCoordinate.length}</h2>
            <h2>Next tile:</h2>
            <button type="button" className="unstyled-button shift-right" style={{ width: '150px', display: 'block' }}
                onClick={() => handleTileClick(matchTilesNoCoordinate[0], myPlayer, match, setSelectedTile, setSelectedSalmon)}
            >
                <img src={matchTilesNoCoordinate[0].tile.image} alt='Next tile to pick' 
                    style={{ 
                        width: '100%', 
                        transition: 'transform 0.35s ease-in-out', 
                        ...getRotationStyle(matchTilesNoCoordinate[0]) 
                    }} 
                />
            </button>
            {!currentUserIsSpectator && myPlayer.id === match.actualPlayerId && match.phase === Phase.TILES && 
            (matchTilesNoCoordinate[0].tile.type === TileType.BEAR || matchTilesNoCoordinate[0].tile.type === TileType.JUMP)
            && <button className='move-btn-diagonal' type="button" onClick={() => handleRotateTile(matchTilesNoCoordinate[0], jwt)}>Rotate Tile</button>}
        </div>
        }

        { 
        <div className='game-container'>
            {match.round >= 6 && <div className='grid3'>
                {[...gridSpawn].reverse().map((salmons:SalmonMatch[], index) => (
                <div key={`spawn-col-${5 - index}`} className="grid-item3">    
                    <div className='salmons-containerSpawn'>
                    {salmons.map((s, i) => ((s.coordinate.y > 20 && <img key={`spawn-${s.id || i}`} src = {s.salmon.image} alt=""
                        style={{                          
                            filter: `drop-shadow(0px 0px 5px ${ColorToRgb(players.find(p => p.id === s.playerId)?.color ?? '')})`,
                            border: `3px solid ${ColorToRgb(players.find(p => p.id === s.playerId)?.color ?? '')}`,
                            borderRadius: '40px',}}
                        />
                        )
                    ))}
                    </div>
                    <div className='spawn-points'>
                    {5 - index} 
                    </div>
                </div>
            ))}
            </div>}

            <div className={`grid1 ${match.round < 6 ? 'grid1-80' : 'grid1-100'}`}>
                {myPlayer && grid.map((cell, index) => {
                    const x = index % GRID_WIDTH;
                    const y = GRID_HEIGHT - 1 - Math.floor(index / GRID_WIDTH);
                    
                    const isValidMove = validMoves.some(m => m.x === x && m.y === y);
                    
                    // Find all unique salmon IDs that have moved in this turn
                    const movedSalmonIds = Array.from(new Set(pendingMoves.map(p => p.salmonId)));

                    // A ghost salmon should be displayed in cell (x, y) if:
                    // 1) The salmon moved during this turn.
                    // 2) Its CURRENT real position is NOT in cell (x, y).
                    // 3) Its starting position at history[0] OR one of its intermediate moves was in cell (x, y).
                    const ghostSalmonsInThisCell = movedSalmonIds.map(salmonId => {
                        const currentRealSalmon = salmons.find(s => s.id === salmonId);
                        if (currentRealSalmon?.coordinate?.x === x && currentRealSalmon?.coordinate?.y === y) {
                            return null;
                        }
                        const initialSalmon = history[0]?.salmons.find(s => s.id === salmonId);
                        if (!initialSalmon) return null;

                        const wasInitial = initialSalmon.coordinate?.x === x && initialSalmon.coordinate?.y === y;
                        const wasIntermediate = pendingMoves.some(p => p.salmonId === salmonId && p.x === x && p.y === y);

                        if (wasInitial || wasIntermediate) {
                            return initialSalmon;
                        }
                        return null;
                    }).filter((s): s is SalmonMatch => s !== null);

                    return (
                        <button type="button" key={`grid-${x}-${y}`} className="grid-item unstyled-button" 
                            style={{ 
                                position: 'relative',
                                cursor: isValidMove ? 'pointer' : 'default',
                                // Mark valid tiles
                                boxShadow: isValidMove ? 'inset 0px 0px 20px 5px rgba(0, 255, 0, 0.7)' : 'none',
                                transition: 'box-shadow 0.2s ease-in-out'
                            }} 
                            onClick={() => {
                                if (match.phase === Phase.TILES) {
                                    handleUpdateTilePosition({jwt, index, selectedSalmon, setSelectedTile, selectedTile, match, actualPlayerId: match.actualPlayerId, myPlayerId: myPlayer.id, matchTilesNoCoordinate}); 
                                } else if (match.phase === Phase.MOVING) {
                                    onGridClickWrapper(index);
                                }
                            }}
                        >
                            <div className="grid-item-content">

                            {cell.tile && (<img src={cell.tile.tile.image} alt="" style={{...getRotationStyle(cell.tile)}}/>)}

                            {/*Real salmons*/}
                            {cell.salmons?.map((salmon, sIndex) => {
                                const position = calculateSalmonPosition(sIndex, cell.salmons.length + ghostSalmonsInThisCell.length);
                                const playerColor = players.find(p => p.id === salmon.playerId)?.color;
                                const rgbColor = playerColor ? ColorToRgb(playerColor) : 'transparent';
                                
                                return (
                                    <button type='button' key={`salmon-btn-${x}-${y}-${salmon.id}-${sIndex}`} className="unstyled-button"
                                        style={{
                                            ...position,
                                            position: 'absolute',
                                            transition: 'transform 0.35s ease-in-out',
                                            zIndex: 10
                                        }}
                                        onClick={(e) => { e.stopPropagation(); onSalmonClickWrapper(salmon);}}
                                    >
                                        <img src={salmon.salmon.image} alt="Real salmon" className="salmon-img"
                                            style={{
                                                filter: `drop-shadow(0px 0px 3px ${rgbColor})`,
                                                border: selectedSalmon?.id === salmon.id ? `3px solid white` : `3px solid ${rgbColor}`
                                            }}
                                        />
                                    </button>
                                );
                            })}

                            {/*"Ghost" salmons indicating movement trace for ALL moved salmons in this turn*/}
                            {ghostSalmonsInThisCell.map((ghostSalmon, ghostIdx) => {
                                const totalSalmons = (cell.salmons?.length || 0) + ghostSalmonsInThisCell.length;
                                const ghostPositionIndex = (cell.salmons?.length || 0) + ghostIdx;
                                const position = calculateSalmonPosition(ghostPositionIndex, totalSalmons);
                                return (
                                    <img key={`ghost-${x}-${y}-${ghostSalmon.id}-${ghostIdx}`} src={ghostSalmon.salmon.image} alt="ghost" className="salmon-img"
                                        style={{
                                            ...position,
                                            opacity: 0.7,
                                            filter: 'grayscale(20%)',
                                            pointerEvents: 'none',
                                            zIndex: 0
                                        }}
                                    />
                                );
                            })}

                            {/*Visual energy indicator (Optional)*/}
                            {isValidMove && (
                                <div style={{
                                    position: 'absolute', top: 5, right: 5, 
                                    background: 'rgba(0,0,0,0.8)', color: '#00ff00', 
                                    padding: '2px 6px', borderRadius: '50%', fontSize: '12px', fontWeight: 'bold'
                                }}>
                                    -{validMoves.find(m => m.x === x && m.y === y)?.energyUsed}⚡
                                </div>
                            )}
                            </div>
                        </button>
                    );
                })}
            </div>
            
            {match.round < 2 &&
            <div className='grid2'>
            {gridSea.map((salmon:SalmonMatch[], index) => (
                <div key={`sea-group-${salmon[0]?.id ?? index}`} className="grid-item2">    
                    {salmon.map((s, i) => (
                        (s.coordinate === null && 
                        <button key={`sea-salmon-${s.id || i}`} type="button" className="unstyled-button"
                            onClick={() => onSalmonClickWrapper(s)}
                            style={{ width: 'fit-content', display: 'inline-block' }}
                        >
                        <img src = {s.salmon.image} alt="Sea salmon"
                            style={{                          
                                filter: `drop-shadow(0px 0px 5px ${ColorToRgb(players.find(p => p.id === s.playerId)?.color ?? '')})`,
                                border: `3px solid ${ColorToRgb(players.find(p => p.id === s.playerId)?.color ?? '')}`,
                                borderRadius: '40px',}}
                            />
                        </button>
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