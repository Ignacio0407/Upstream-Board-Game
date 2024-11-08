import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/game.css'
import PlayerCard from './PlayerCard';
import { Button } from 'reactstrap';

function Game({match}){
    const jwt = tokenService.getLocalAccessToken();
   // const [players,setPlayers] = useFetchState([],`/api/v1/matches/${match.id}/players`,jwt) // Tenemos los jugadores de la partida
    const [filteredPlayers,setFilteredPlayers] = useState([])
    const [tiles,setTiles] = useFetchState([], '/api/v1/casilla',jwt)

    console.log(tiles);

    return(
        <div className='gameContainer'>

        </div>
    )
}

export default Game;