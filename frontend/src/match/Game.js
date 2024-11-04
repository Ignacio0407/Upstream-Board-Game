import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import '../static/css/game/lobby.css'
import PlayerCard from './PlayerCard';
import { Button } from 'reactstrap';

function Game({match}){
    const jwt = tokenService.getLocalAccessToken();
    const [players,setPlayers] = useFetchState([],`/api/v1/players`,jwt)
    const [filteredPlayers,setFilteredPlayers] = useState([])

    return(
        <>
        </>
    )
}

export default Game;