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

export default function Lobby({match}){
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const [finalUser,setUser] = useFetchState([],`/api/v1/users/${user.id}`,jwt)
    const [players,setPlayers] = useFetchState([],`/api/v1/matches/${match.id}/players`,jwt)
    const [matches,setMatches] = useFetchState([],`/api/v1/matches/${match.id}`,jwt)
    const [userPlayer,setUserPlayer] = useState(null);
    const [showColorPicker, setShowColorPicker] = useState(true); // Empieza en false
    const navigate = useNavigate();
    const [takenColors, setTakenColors] = useState([]);
    const [numjug, Setnumjug] = useState(match.playersNum);
    const [loading, setLoading] = useState(false);
    const [ordenPartida, setOrdenPartida] = useState(0);
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
        setOrdenPartida(0);
        setUserPlayer(playerUser);
        const colorsUsed = players.map(player => ColorToRgb(player.color));
        setTakenColors(colorsUsed);
        Setnumjug(players.length);
 
        if(matches.state === "FINALIZADA"){
            navigate("/dashboard");
        }
    }, [players, match.id, user.id, matches.state]);
 
        const fetchPlayers = async () => {
        const response = await fetch("/api/v1/matches/"+match.id+"/players", {
            method: "GET",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
        });
        const data = await response.json();
        setPlayers([])
        setPlayers(data); // Actualiza el estado con los nuevos jugadores
    };

const startGame = async () => {
    setLoading(true);
    const requests = async () => {
        await fetch(`/api/v1/matches/${match.id}/startGame`, {
            method: "PATCH",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
        });
        await fetch(`/api/v1/matchTiles/createMatchTiles/${match.id}`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
        });
    };
    await requests();

    try {
        await Promise.all(requests);
        stompClient.publish({
                destination: "/app/start",
                body: JSON.stringify({ action: "colorChanged", userId: finalUser.id }),
        });
        console.log("Todas las MatchTiles han sido creadas en orden aleatorio.");
    } catch (error) {
        console.error("Error al crear algunas MatchTiles:", error);
    }
    setLoading(false);
    
    window.location.reload(true);
};
    
    function endGame(){
        if(spectatorIds.find(p => p === user.id)){
            navigate("/dashboard");
        }
        else{
            let numJugadores = numjug - 1;
            if(match.matchCreator === user.id){
                numJugadores = 0;
            }
        const playerId = players.find(p => p.userPlayer === user.id);
        matches.playersNum = numJugadores;
        if(numJugadores === 0){
            matches.state = "FINALIZADA";
        }
        else{
            matches.state = "ESPERANDO";
             
        }
        fetch("/api/v1/players/"+ playerId, {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
            },
                 
        }).then(()=> fetch("/api/v1/matches/"+ match.id, {

            method: "PUT",
            headers: {
                Authorization: `Bearer ${jwt}`,
                Accept: "application/json",
                "Content-Type": "application/json",
              },
              body: JSON.stringify(matches),
        }).then(
            navigate("/dashboard")
        ))
    }
    }
    
    const playerList = players.map((p) =>{
        return (
            <tr key={p.id} className="r">
                 <PlayerCard nombre={p.name} color={p.color}/>
            </tr>
            
        )
    })

    async function handleColorChange(color) {
        try {
            const response = await fetch(`/api/v1/players/match/${match.id}`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${jwt}`,
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({color: color,user: finalUser.id,}),
            });

            if (response.ok) {
                stompClient.publish({
                    destination: "/app/hello",
                    body: JSON.stringify({ action: "colorChanged", userId: finalUser.id }),
                });
                setShowColorPicker(false);
                const createdPlayer = await response.json();
                const req = 
                    fetch(`/api/v1/salmonMatches/player/${createdPlayer.id}`, {
                        method: "POST",
                        headers: {
                            Authorization: `Bearer ${jwt}`,
                            Accept: "application/json",
                            "Content-Type": "application/json",
                        }});
                try {
                    await Promise.all(req);
                    console.log("Todos los SalmonMatch han sido creados.");
                } catch (error) {
                    console.error("Error al crear algunos SalmonMatch:", error);
                }
            } else {
                console.error('Error al crear el jugador:', response.statusText);
            }
        } catch (error) {
            console.error('Error al crear el jugador:', error);
        }
    }
    
    return(
        <div className='lobbyContainer'>
        {players.find(p => p.userPlayer === user.id)===undefined && spectatorIds.find(p => p === user.id) === undefined &&(showColorPicker &&
        <ColorPickerModal onColorSelect={handleColorChange} takenColors = {takenColors} />
        )}
        <h1 className='lobbyTitleContainer'>
            {match.name}
        </h1>
        {match.password !== "" && <h4 className='passwordContainer'>
            Password: {match.password}
        </h4>}
        <div className='lobbyMainContainer'>
        
        <Table className='lobbyPlayerContainer'>
        <thead> 
            <h2>
            Jugadores 
            </h2>
        </thead>
        {playerList}
        </Table>
        <div className='lobbyUtilContainer'>
        </div>
        {match.matchCreator === user.id && spectatorIds.find(p => p === userPlayer.id) === undefined && numjug>=1 &&<Button color='success' onClick={startGame}>
            Iniciar Partida
        </Button>}
        {loading && <div>Loading tiles...</div>}

        <Button color='danger' onClick={endGame}>
            Salir de la Partida
            </Button>
        </div>
        </div>
    )
}