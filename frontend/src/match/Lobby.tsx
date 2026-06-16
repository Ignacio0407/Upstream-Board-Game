import { useState, useEffect } from 'react';
import { Button } from 'reactstrap';
import { useNavigate, useLocation, useParams } from "react-router-dom";
import tokenService from '../services/token.service.ts'
import useFetchState from "../util/useFetchState.ts";
import '../static/css/game/lobby.css'
import PlayerCard from './PlayerCard.tsx';
import { ColorToRgb } from '../util/ColorParser.ts';
import startGame from '../util/startGame.ts';
import endGame from '../util/endGame.ts';
import ColorHandler from '../util/ColorHandler.tsx';
import Match, {emptyMatch} from '../interfaces/Match.ts';
import Player, {emptyPlayer} from '../interfaces/Player.ts';
import User, { emptyUser } from '../interfaces/User.ts';
import WebSocketManager from '../util/webSocketManager.ts';
import { State } from '../interfaces/State.ts';

export default function Lobby () {
    const jwt = tokenService.getLocalAccessToken();
    const { matchId } = useParams<{ matchId: string }>();
    const [match, setMatch] = useFetchState<Match>(emptyMatch, matchId ? `/api/v1/matches/${encodeURIComponent(String(matchId))}` : null, jwt);
    const user = tokenService.getUser()
    // Remove everything that is not a letter or number from ID to prevent resource manipulation
    const safeUserId = encodeURIComponent(String(user.id).replace(/[^a-zA-Z0-9-]/g, ''));
    const [finalUser,setUser] = useFetchState<User>(emptyUser, `/api/v1/users/${safeUserId}`, jwt);
    const [players,setPlayers] = useFetchState<Player[]>([],`/api/v1/players/match/${encodeURIComponent(String(match.id))}`,jwt)
    const [userPlayer,setUserPlayer] = useState(emptyPlayer);
    const [showColorPicker, setShowColorPicker] = useState(true);
    const navigate = useNavigate();
    const [takenColors, setTakenColors] = useState<string[]>([]);
    const [loading, setLoading] = useState(false);
    const spectatorIds:number[] = useLocation().state?.spectatorIds||[];
    
    useEffect(() => {
        const wsManager = new WebSocketManager(jwt);
        wsManager.initConnection();

        wsManager.addSubscription<Match>(`match/update/${matchId}`, (updatedMatch) => {
            setMatch(updatedMatch)
        });

        wsManager.addSubscription<Player>(`match/newPlayer/${matchId}`, (newPlayer) => {
            setPlayers((prev) => [...prev, newPlayer]);
        });

        wsManager.addSubscription<any>(`match/start/${matchId}`, () => {
            navigate(`/matches/${matchId}/game`);
        });

        return () => { wsManager.deactivate() };
    }, []); // It only opens the connection once
    
    useEffect(() => {
        const playerUser = players.find(player => player.userId === user.id);
        if (playerUser) setUserPlayer(playerUser);
        const colorsUsed = players.map(player => ColorToRgb(player.color));
        setTakenColors(colorsUsed);
        if(match.state === State.FINALIZED) navigate("/dashboard");
    }, [players, user.id, match.state]);

    const startingGame = async () => {
        await startGame({matchId: match.id, jwt, setLoading, navigate});
    };
    
    const endingGame = async () => {
        await endGame({match, players, user, jwt, spectatorIds, navigate, setMatch});
    };
    
    const playerList = players.map((p, id) => { return ( <PlayerCard key={id} name={p.name} color={p.color} /> ) });
    
    return (
        <div className='lobbyContainer'>
            {players.find(p => p.userId === user.id)===undefined && spectatorIds.find(p => p === user.id) === undefined && (showColorPicker &&
            (
                <ColorHandler matchId={Number(matchId)} jwt={jwt} finalUserId={finalUser.id} takenColors={takenColors} 
                onColorChanged={() => setShowColorPicker(false)} /> )
            )}
            <div className='lobbyTitleContainer'>
                <h1> Room name: {match.name} </h1>
                {match.password !== "" && <h4 className='passwordContainer'> Password: {match.password} </h4>}
                <h4 className='passwordContainer'> Players number: {match.playersNumber} </h4>
            </div>
            <div className='lobbyMainContainer'>
                <div className='lobbyUtilContainer'>
                    <h2>Players</h2>
                    {playerList}
                </div>
            <br />

                <div role="button" className='lobbyUtilContainer' tabIndex={0} 
                onKeyDown={(e) => {
                    if (e.key === "Enter" && match.matchCreatorId === user.id && spectatorIds.find(p => p === userPlayer.id) 
                        === undefined && match.playersNumber >= 1) {
                        startingGame();
                    }
                }}>
                {match.matchCreatorId === user.id && spectatorIds.find(p => p === userPlayer.id) === undefined && 
                match.playersNumber >=2 &&
                <Button color='success' onClick={startingGame} className='buttonStart'> Start </Button>}
                {loading && <div>Loading tiles...</div>}
                <Button color='danger' onClick={endingGame} className='buttonExit'> Exit </Button>

                </div>
            </div>
        </div>
    )
}