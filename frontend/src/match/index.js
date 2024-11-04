import React, { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import getIdFromUrl from "./../util/getIdFromUrl";
import Lobby from "./Lobby"
import Game from "./Game"
import { useNavigate } from 'react-router-dom';
export default function Match() { 

    const jwt = tokenService.getLocalAccessToken();
    const user = tokenService.getUser()
    const id = getIdFromUrl(2);
    const [match,setMatch] = useFetchState([],`/api/v1/matches/${id}`,jwt)
    const navigate = useNavigate();
    useEffect(() => {
        console.log("Entered the game")
    }, []);

    
    return(
        
        match.estado === "ESPERANDO" ? <Lobby match={match}></Lobby> : <Game match={match}></Game>
    )


}