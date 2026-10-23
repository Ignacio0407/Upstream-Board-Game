import { JSX, useEffect, useState } from 'react';
import { Button } from 'reactstrap';
import { useNavigate, useParams } from "react-router-dom";
import tokenService from '../services/token.service.ts';
import { get } from '../util/fetchers.ts';
import "../static/css/game/endGame.css";
import Player from '../interfaces/Player.ts';

export default function EndGame(): JSX.Element {
    const jwt = tokenService.getLocalAccessToken();
    const { matchId } = useParams<{ matchId: string }>();
    const [playersWithPoints, setPlayersWithPoints] = useState<Player[]>([]);
    const [initialized, setInitialized] = useState(false);
    const navigate = useNavigate();

    const fetchPlayers = async () => {
        const playersResponse = await get(`/api/v1/players/match/${matchId}`, jwt);
        const playerData:Player[] = await playersResponse.json();
        playerData.sort((a, b) => b.points - a.points);
        setPlayersWithPoints(playerData);
    };

    useEffect(() => {
        if (!initialized) {
            const initialize = async () => {
                await fetchPlayers();
                setInitialized(true);
            };
            void initialize();
        }
    }, [initialized, matchId, jwt]);

    const getPodiumStyle = (index:number) => {
        switch (index) {
            case 0:
                return { backgroundColor: 'rgba(255, 215, 0, 0.4)' }; // Gold
            case 1:
                return { backgroundColor: 'rgba(101, 101, 101, 0.4)' }; // Silver
            case 2:
                return { backgroundColor: 'rgba(205, 127, 50, 0.4)' }; // Bronce
            default:
                return { backgroundColor: 'rgba(255, 255, 255, 0.4)' }; // Others
        }
    };

    const player = playersWithPoints.map((p, index) => (
        <div key={p.id ?? p.name} className="endgame-players" style={getPodiumStyle(index)}>
            <h2>
                {p.name}
                <span className="spacer"></span>
                {p.points}
            </h2>
        </div>
    ));

    return (
        <div className="endgame-container">
            <h2 className="endgame-title">SCORE</h2>
            
            {playersWithPoints.length > 0 && <div>{player}</div>}
            
            <Button color='danger' onClick={() => navigate("/dashboard")} className='buttonExit'> Exit </Button>
        </div>
    );
}