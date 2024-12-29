import React, { useState, useEffect } from 'react'; 
import tokenService from '../services/token.service'
import jwt_decode from "jwt-decode";
import '../static/css/dashboard/dashb.css'
import BotonLink from "../util/BotonLink";
import useFetchState from '../util/useFetchState';
import SearchBar from '../util/SearchBar';
import '@fortawesome/fontawesome-free/css/all.min.css'
import { Button } from 'reactstrap';
import deleteFromList from '../util/deleteFromList';
import getErrorModal from '../util/getErrorModal';
import WhiteSpace from '../util/WhiteSpace';
import { useNavigate } from "react-router-dom";
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
   
export default function Dashboard() { 
    const [username, setUsername] = useState("");
    const jwt = tokenService.getLocalAccessToken();
    const [matches, setMatches] = useFetchState([],'/api/v1/matches',jwt);
    const user = tokenService.getUser()
    const [alerts, setAlerts] = useState([]);
    const [message, setMessage] = useState(null);
    const [visible, setVisible] = useState(false);
    const navigate = useNavigate();
    const [spectatorIds, setSpectatorIds] = useState([]);

    useEffect(() => {
        if (jwt) {
            setUsername(jwt_decode(jwt).sub);
        }
    }, [jwt])

    const modal = getErrorModal(setVisible, visible, message);
    
    function espectate(match){
        setSpectatorIds(prevSpectators => [...prevSpectators, user.id]);
        navigate('/matches/'+match.id, { state: { spectatorIds: [...spectatorIds, user.id] } });
    }

    function join(match){
        if(match.password !== ""){
            const contrasena = prompt("Enter the password to join the game");
            if(contrasena !== match.password){
                alert("Incorrect password");
                return;
            }
        }
        navigate('/matches/'+match.id);
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
            console.log('Connected: ' + frame);
            
            stompClient.subscribe('/topic/reload', (message) => {
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

    const matchesList = 
      matches.map((match) => {
        return (
            <tr key={match.nombre} className='fila'>
                <td className='celda'>{match.name}</td>
                <td className='celda'>{match.playersNum}</td>
                <td className='celda'>{match.state}</td>
                <td className='celda'>{match.password != "" && <i className="fa fa-lock"></i>}</td>
                <td className='celda'>{match.state === 'ESPERANDO' &&
                <Button color={"success"} onClick={() => join(match)}>Join game</Button>
                }</td>
                <td className='celda'>{(match.state === 'EN_CURSO' || match.estado === 'ESPERANDO') &&
                <Button color={"warning"} onClick={() => espectate(match)} >Spectate game</Button>
                }</td>
                {user.roles[0] == "ADMIN" && <Button color="danger"
                    onClick={() =>
                    deleteFromList(
                    `/api/v1/matches/${match.id}`,
                    match.id,
                    [matches, setMatches],
                    [alerts, setAlerts],
                    setMessage,
                    setVisible,
                    console.log(match))}>
                    {<i className="fa fa-trash"></i>}
                    Delete
                </Button>}
            </tr>
        );
      })

    return ( 
        <> 
        <div className="dashboard-page-container">
            <h1 className='welcome'>
            Game Listing for {username}
            </h1>
            <SearchBar setter={setMatches} uri={"matches"} />
            <WhiteSpace />
            <div>
            <div className='crear-partida'>
              <BotonLink color={"success"} direction={"/creategame"} text={"Create Game"}></BotonLink>
              </div>
                <div className="game-table">
                <thead>
                    <tr className='fila'>
                        <th className='cabeza'>Game</th>
                        <th className='cabeza'>Players</th>
                        <th className='cabeza'>State</th>
                        <th className='cabeza'>Private</th>
                        <th className='cabeza'>Join</th>
                        <th className='cabeza'>Spectate</th>
                    </tr>
                </thead>
                <tbody>{matchesList}</tbody>
                </div>
            </div>
        </div> 
        </> 
); 
}