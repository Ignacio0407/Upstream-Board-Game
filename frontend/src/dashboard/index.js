import React, { useEffect } from 'react';
import '../App.css';
import '../static/css/dashboard/dashb.css';
import '../static/css/home/home.css'
import tokenService from '../services/token.service';
import BotonLink from '../util/BotonLink';

export default function Home() {
    const jwt = tokenService.getLocalAccessToken(); 

    useEffect(() => {
        // Any side effects based on jwt can be handled here
    }, [jwt]);

    return (
        <div className="home-page-container">
            <div className="pantallaInicio">
                <div className="game-options">
                    <div className="game-mode">
                        <h2>Game Mode</h2>
                        <button>Normal</button>
                        <button>Whirlpool</button>
                        <button>Rapids</button>
                    </div>
                    <div className="player-count">
                        <h2>Number of players</h2>
                        <button>3 Players</button>
                        <button>4 Players</button>
                        <button>5 Players</button>
                    </div>
                    <button className="create-game">Create Game</button>
                </div>
            </div>
        </div>
    );
}
