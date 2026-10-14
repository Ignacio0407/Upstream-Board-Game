import { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { Alert, Button } from 'reactstrap';
import '@fortawesome/fontawesome-free/css/all.min.css';
import './dashboard.css';
import tokenService from '../services/token.service.ts';
import jwt_decode, { JwtPayload } from "jwt-decode";
import useFetchState from '../util/useFetchState.ts';
import deleteFromList from '../util/deleteFromList.ts';
import WhiteSpace from '../util/WhiteSpace.tsx';
import Match, { DashboardMatch } from '../interfaces/Match.ts';
import WebSocketManager from '../components/webSocketManager/webSocketManager.ts';
import { State } from '../interfaces/State.ts';
import { Authority } from '../interfaces/User.ts';
import ListGenerator from '../components/ListGenerator/ListGenerator.tsx';

export default function Dashboard() {
    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser();
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [matches, setMatches] = useFetchState<DashboardMatch[]>([], '/api/v1/matches/dashboard', jwt);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [spectatorIds, setSpectatorIds] = useState<number[]>([]);

    useEffect(() => {
        if (jwt) {
            const decoded = jwt_decode<JwtPayload>(jwt);
            if (decoded?.sub) setUsername(decoded.sub);
        }
    }, [jwt]);

    function espectate(match: DashboardMatch) {
        setSpectatorIds(prevSpectators => [...prevSpectators, user.id]);
        navigate('/matches/' + match.id + "/game", { state: { spectatorIds: [...spectatorIds, user.id] } });
    }

    function join(match: DashboardMatch) {
        if (match.password !== "") {
            const password = prompt("Enter the password to join the game");
            if (password !== match.password) {
                alert("Incorrect password");
                return;
            }
        }
        navigate('/matches/' + match.id + "/waiting");
    }

    useEffect(() => {
        const wsManager = new WebSocketManager(jwt);
        wsManager.initConnection();

        wsManager.addSubscription<Match>(`matches/dashboard`, (updatedMatch) => {
            setMatches(prevMatches => {
                const filtered = prevMatches.filter(match => match.id !== updatedMatch.id);
                return [...filtered, updatedMatch];
            });
        });

        return () => { wsManager.deactivate(); };
    }, []); // It only opens the connection once

    function renderMatch(match: DashboardMatch) {
        return (
            <>
                <td className='cell'>{match.name}</td>
                <td className='cell'>{match.playersNumber}</td>
                <td className='cell'>{match.state}</td>
                <td className='cell'>{match.password !== "" && <i aria-label="private game" className="fa fa-lock"></i>}</td>
                <td className='cell'>{match.state === State.WAITING &&
                    <Button color="success" onClick={() => join(match)}>Join game</Button>
                }</td>
                <td className='cell'>{(match.state === State.ON_GOING || match.state === State.WAITING) &&
                    <Button color="warning" onClick={() => espectate(match)}>Spectate game</Button>
                }</td>
            </>
        );
    }

    return (
        <div className="dashboard-page-container">
            <h1 className='welcome'>
                Game Listing for {username}
            </h1>
            <WhiteSpace />
            <div className="game-table">
                <ListGenerator<DashboardMatch>
                    data={matches}
                    columns={["name", "playersNumber", "state", "password", "join", "spectate"] as (keyof DashboardMatch)[]}
                    customLabels={{
                        name: "Game",
                        playersNumber: "Players",
                        state: "State",
                        password: "Private",
                        join: "Join",
                        spectate: "Spectate",
                    }}
                    idKey="id"
                    customRender={renderMatch}
                    actualUser={{ id: user?.id, username: user?.username, roles: user?.roles }}
                    requiredAuth={Authority.PLAYER}
                    createButton="/creategame"
                    createButtonPosition="top"
                    deleteCondition={user?.roles?.[0] === Authority.ADMIN}
                    deleteButton={(match) =>
                        deleteFromList(`/api/v1/matches`, match.id, [matches, setMatches], [alerts, setAlerts])
                    }
                    searchBarPlaceholder="Search matches"
                />
            </div>
        </div>
    );
}
