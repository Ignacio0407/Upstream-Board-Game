import { useState, useEffect } from 'react';
import tokenService from '../services/token.service.ts'
import useFetchState from "../util/useFetchState.ts";
import getIdFromUrl from "../util/getIdFromUrl.ts";
import Lobby from "./Lobby.tsx"
import Game from "./Game.tsx"
import EndGame from './EndGame.tsx';
import {get} from '../util/fetchers.ts'
import Match, {emptyMatch} from '../interfaces/Match.ts';

export default function EnterMatch() { 
    const jwt = tokenService.getLocalAccessToken();
    const [id, setId] = useState<string | null>(null);
    const [isReady, setIsReady] = useState(false);
    const [match, setMatch] = useFetchState<Match>(emptyMatch, id ? `/api/v1/matches/${id}` : null, jwt);

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

    return (        
        match.state === "WAITING" ? <Lobby match={match}></Lobby> : match.state === "FINALIZED" ? <EndGame match={match}></EndGame> : <Game match={match}></Game>  
    )
}