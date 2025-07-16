import { useState, useEffect } from 'react';
import tokenService from '../services/token.service'
import useFetchState from "../util/useFetchState";
import getIdFromUrl from "../util/getIdFromUrl";
import Lobby from "./Lobby"
import Game from "./Game"
import EndGame from './EndGame';
import {get} from '../util/fetchers'

export default function Match() { 
    const jwt = tokenService.getLocalAccessToken();
    const [id, setId] = useState(null);
    const [isReady, setIsReady] = useState(false);
    const [match, setMatch, error] = useFetchState([], id ? `/api/v1/matches/${id}` : null, jwt);

    useEffect(() => {
        const fetchedId = getIdFromUrl(2);
        setId(fetchedId);
        // Wait 1 second (1000 ms) before charging data
        const timer = setTimeout(() => {
            setIsReady(true);
        }, 1000);

        // Clean timer upon component disasembly
        return () => clearTimeout(timer);
    }, []);

    useEffect(() => {
        if(id) {
        const interval = setInterval(() => {
            get("/api/v1/matches/"+ id, jwt)
            .then(response => {response.json()
            .then(data => {setMatch(data)})})
            }, 500); // Cada 5 segundos
        return () => clearInterval(interval);
        }
    }, [isReady]);

    if (!isReady || !id || !match) {
        return <div>Loading...</div>;
    }
    if (error) {
        return <div>Error: {error.message}</div>;
    }

    return (        
        match.state === "WAITING" ? <Lobby match={match}></Lobby> : match.state === "FINALIZED" ? <EndGame match={match}></EndGame> : <Game match={match}></Game>  
    )
}