import { useState, useEffect } from 'react';
import { Button, Table } from 'reactstrap';
import { useNavigate, useLocation } from "react-router-dom";
import { Client } from '@stomp/stompjs';
import tokenService from '../services/token.service.ts'
import useFetchState from "../util/useFetchState.ts";
import '../static/css/game/lobby.css'
import PlayerCard from './PlayerCard.tsx';
import { ColorToRgb } from '../util/ColorParser.ts';
import startGame from '../util/startGame.ts';
import endGame from '../util/endGame.ts';
import ColorHandler from '../util/ColorHandler.tsx';
import {get, createWebSocket} from '../util/fetchers.ts'
import Match, {emptyMatch} from '../interfaces/Match.ts';
import Player, {emptyPlayer} from '../interfaces/Player.ts';
import User, { emptyUser } from '../interfaces/User.ts';

export default function Lobby({match}: {match: Match}) {
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState<User>(emptyUser,`/api/v1/users/${user.id}`,jwt)
    const [players,setPlayers] = useFetchState<Player[]>([],`/api/v1/players/match/${match.id}`,jwt)
    const [matches,setMatches] = useFetchState<Match>(emptyMatch,`/api/v1/matches/${match.id}`,jwt)
    const [userPlayer,setUserPlayer] = useState(emptyPlayer);
    const [showColorPicker, setShowColorPicker] = useState(true);
    const navigate = useNavigate();
    const [takenColors, setTakenColors] = useState<string[]>([]);
    const [playersNum, setnumjug] = useState<number>(match.playersNumber);
    const [loading, setLoading] = useState(false);
    const spectatorIds = useLocation().state?.spectatorIds||[];
    
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
        //console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/refresh', (message) => {
            //console.log('Message received: ' + message.body);
            fetchPlayers();
        });
        stompClient.subscribe('/topic/game', (message) => {
            //console.log('Message received: ' + message.body);
            window.location.reload();
        })
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
        const playerUser = players.find(player => player.userId === user.id);
        if (playerUser) setUserPlayer(playerUser);
        const colorsUsed = players.map(player => ColorToRgb(player.color));
        setTakenColors(colorsUsed);
        setnumjug(players.length);
 
        if(matches.state === "FINALIZED"){
            navigate("/dashboard");
        }
    }, [players, match.id, user.id, matches.state]);
 
    const fetchPlayers = async () => {
        const response = await get("/api/v1/players/match/"+match.id, jwt);
        const data = await response.json();
        setPlayers(data);
    };

    const startingGame = async () => {
        await startGame({matchId: match.id, jwt, stompClient, finalUser, setLoading});
    };
    
    const endingGame = async () => {
        await endGame({match, players, user, jwt, spectatorIds, navigate, playersNum, setMatches});
    };
    
    const playerList = players.map((p) =>{
        return (
        <tr key={p.id} className="r">
            <PlayerCard name={p.name} color={p.color} />
        </tr>
        )
    })
    
    return(
        <div className='lobbyContainer'>
        {players.find(p => p.userId === user.id)===undefined && spectatorIds.find(p => p === user.id) === undefined &&(showColorPicker &&
        (
            <ColorHandler matchId={match.id} jwt={jwt} finalUserId={finalUser.id} takenColors={takenColors} 
            onColorChanged={() => setShowColorPicker(false)} stompClient={stompClient} />
        )
        )}
        <div className='lobbyTitleContainer'>
        <h1>
            Nombre de la sala: {match.name}
        </h1>
        {match.password !== "" &&
         <h4 className='passwordContainer'>
            Password: {match.password}
        </h4>}
        <h4 className='passwordContainer'>
            Número de jugadores: {match.playersNumber}
        </h4>
        </div>
        <div className='lobbyMainContainer'>
        
        <Table className='lobbyPlayerContainer'>
        <thead> 
            <h2>
            Jugadores 
            </h2>
        </thead>
        {playerList}
        </Table>
       
        <div className='lobbyUtilContainer' tabIndex={0} 
        onKeyDown={(e) => {
            if (e.key === "Enter" && match.matchCreatorId === user.id && spectatorIds.find(p => p === userPlayer.id) === undefined && playersNum >= 1) {
                startingGame();
            }
        }}>
        {match.matchCreatorId === user.id && spectatorIds.find(p => p === userPlayer.id) === undefined && playersNum>=2 &&
        <Button color='success' onClick={startingGame} className='buttonStart'>
            Iniciar
        </Button>}
        {loading && <div>Loading tiles...</div>}

        <Button color='danger' onClick={endingGame} className='buttonExit'>
            Salir
            </Button>

        </div>
        </div>
        </div>
    )
}