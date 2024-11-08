import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/game.css'
import { Button } from 'reactstrap';
import { ColorToRgb } from '../util/ColorParser';

function Game({match}){
    const jwt = tokenService.getLocalAccessToken();
   // const [players,setPlayers] = useFetchState([],`/api/v1/matches/${match.id}/players`,jwt) // Tenemos los jugadores de la partida
    const [filteredPlayers,setFilteredPlayers] = useState([])
    const [tiles,setTiles] = useFetchState([], '/api/v1/casilla',jwt)
    const [players,setPlayers] = useFetchState([], `/api/v1/players/match/${match.id}`,jwt)
    console.log(players)
    const playerList = players.map(player => 
    <div color= {ColorToRgb(player.color)}>
        
    </div>)

    return(
        <div className='gameContainer'>
            <div>
            {playerList}
            </div>
            

        </div>
    )
}

export default Game;