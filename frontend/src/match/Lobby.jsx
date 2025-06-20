import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/lobby.css'
import PlayerCard from './PlayerCard';
import { Button, Table } from 'reactstrap';
import { useNavigate } from "react-router-dom";
import ColorPickerModal from '../util/ColorPickerModal';
import { ColorToRgb } from '../util/ColorParser';
import { useLocation } from "react-router-dom";
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import startGame from '../util/startGame';
import endGame from '../util/endGame';
import ColorHandler from '../util/ColorHandler';
export default function Lobby({match}){
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)
    const [players,setPlayers] = useFetchState([],`/api/v1/players/match/${match.id}`,jwt)
    const [matches,setMatches] = useFetchState([],`/api/v1/matches/${match.id}`,jwt)
    const [userPlayer,setUserPlayer] = useState(null);
    const [showColorPicker, setShowColorPicker] = useState(true); // Empieza en false
    const navigate = useNavigate();
    const [takenColors, setTakenColors] = useState([]);
    const [numjug, Setnumjug] = useState(match.playersNumber);
    const [loading, setLoading] = useState(false);
    const spectatorIds = useLocation().state?.spectatorIds||[];
    
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
        stompClient.subscribe('/topic/refresh', (message) => {
            console.log('Message received: ' + message.body);
            fetchPlayers() ;
        });
        stompClient.subscribe('/topic/game', (message) => {
            console.log('Message received: ' + message.body);
            window.location.reload(true);
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
        const playerUser = players.find(player => player.userPlayer.id === user.id);
        setUserPlayer(playerUser);
        const colorsUsed = players.map(player => ColorToRgb(player.color));
        setTakenColors(colorsUsed);
        Setnumjug(players.length);
 
        if(matches.state === "FINALIZADA"){
            navigate("/dashboard");
        }
    }, [players, match.id, user.id, matches.state]);
 
    const fetchPlayers = async () => {
        const response = await get("/api/v1/matches/"+match.id+"/players", jwt);
        const data = await response.json();
        setPlayers(data); // Actualiza el estado con los nuevos jugadores
    };

    const startingGame = async () => {
        await startGame({
            matchId: match.id,
            jwt,
            stompClient,
            finalUser,
            setLoading,
        });
    };
    
    const endingGame = async () => {
        await endGame({
            match,
            players,
            user,
            jwt,
            spectatorIds,
            navigate,
            numjug,
            setMatches,
        });
    };
    
    const playerList = players.map((p) =>{
        return (
            <tr key={p.id} className="r">
                 <PlayerCard nombre={p.name} color={p.color}/>
            </tr>
            
        )
    })
    
    return(
        <div className='lobbyContainer'>
        {players.find(p => p.userPlayer === user.id)===undefined && spectatorIds.find(p => p === user.id) === undefined &&(showColorPicker &&
        (
            <ColorHandler
                matchId={match.id}
                jwt={jwt}
                finalUserId={finalUser.id}
                takenColors={takenColors}
                onColorChanged={() => setShowColorPicker(false)}
                stompClient={stompClient}
            />
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
            if (e.key === "Enter" && match.matchCreator === user.id && spectatorIds.find(p => p === userPlayer.id) === undefined && numjug >= 1) {
                startingGame();
            }
        }}>
        {match.matchCreator === user.id && spectatorIds.find(p => p === userPlayer.id) === undefined && numjug>=2 &&
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