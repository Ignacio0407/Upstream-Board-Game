import { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { Alert, Button } from 'reactstrap';
import '@fortawesome/fontawesome-free/css/all.min.css'
import '../static/css/dashboard/dashb.css' 
import tokenService from '../services/token.service.ts'
import jwt_decode, { JwtPayload } from "jwt-decode";
import ButtonLink from "../components/ButtonLink/ButtonLink.tsx";
import useFetchState from '../util/useFetchState.ts';
import SearchBar from '../components/SearchBar/SearchBar.tsx';
import deleteFromList from '../util/deleteFromList.ts';
import WhiteSpace from '../util/WhiteSpace.tsx';
import { createWebSocket, createStompClient } from '../util/fetchers.ts'
import {DashboardMatch} from '../interfaces/Match.ts';
   
export default function Dashboard() {
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUserList();
    const navigate = useNavigate(); 
    const [username, setUsername] = useState("");
    const [matches, setMatches] = useFetchState<DashboardMatch[]>([],'/api/v1/matches/dashboard',jwt);
    const [filtered, setFiltered] = useState<DashboardMatch[]>([]);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [message, setMessage] = useState("");
    const [visible, setVisible] = useState(false);
    const [spectatorIds, setSpectatorIds] = useState<number[]>([]);

    useEffect(() => {
        if (jwt) {
            const decoded = jwt_decode<JwtPayload>(jwt);
            if (decoded && decoded.sub) setUsername(decoded.sub);
        };
    }, [jwt])
    
    function espectate(match:DashboardMatch) {
        setSpectatorIds(prevSpectators => [...prevSpectators, user.id]);
        navigate('/matches/'+match.id, { state: { spectatorIds: [...spectatorIds, user.id] } });
    }

    function join(match:DashboardMatch) {
        if (match.password !== "") {
            const password = prompt("Enter the password to join the game");
            if (password !== match.password) {
                alert("Incorrect password");
                return;
            }
        }
        navigate('/matches/'+match.id);
    }

    const socket = createWebSocket();
    const stompClient = createStompClient(socket, jwt);
    
    stompClient.onConnect = () => {
    stompClient.subscribe('/topic/reload', (message) => {
        console.log('Message received: ' + message.body);
        window.location.reload();
    });
};

    stompClient.activate();

    function matchesList(matchesToList:DashboardMatch[]) {
      return matchesToList.map((match) => {
        return (
            <tr key={match.name} className='fila'>
                <td className='celda'>{match.name}</td>
                <td className='celda'>{match.playersNumber}</td>
                <td className='celda'>{match.state}</td>
                <td className='celda'>{match.password != "" && <i className="fa fa-lock"></i>}</td>
                <td className='celda'>{match.state === 'WAITING' &&
                <Button color={"success"} onClick={() => join(match)}>Join game</Button>
                }</td>
                <td className='celda'>{(match.state === 'ON_GOING' || match.state === 'WAITING') &&
                <Button color={"warning"} onClick={() => espectate(match)} >Spectate game</Button>
                }</td>
                {user.roles[0] == "ADMIN" && <Button color="danger" onClick={() =>
                    deleteFromList(`/api/v1/matches/${match.id}`, match.id, [matches, setMatches], [alerts, setAlerts],
                    setMessage, setVisible)}>
                    {<i className="fa fa-trash"></i>}
                    Delete
                </Button>}
            </tr>
        );
      })
    }

    return ( 
        <> 
        <div className="dashboard-page-container">
            <h1 className='welcome'>
            Game Listing for {username}
            </h1>
            <SearchBar data={matches} setFiltered={setFiltered} placeholder={"Search matches"} defaultCaseSensitive={true}/>
            <WhiteSpace />
            <div>
            <div className='crear-partida'>
              <ButtonLink color={"success"} direction={"/creategame"} text={"Create Game"}></ButtonLink>
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
                <tbody>{filtered ? matchesList(filtered) : matchesList(matches)}</tbody>
                </div>
            </div>
        </div> 
        </> 
); 
}