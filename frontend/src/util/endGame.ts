import { deleteEntity, put } from "./fetchers.ts";
import Match from '../interfaces/Match.ts'
import Player from '../interfaces/Player.ts'
import User from '../interfaces/User.ts'

interface endGamePrompts {match:Match, players:Player[], user:User, jwt:string, spectatorIds:number[], 
    navigate:Function, playersNum:number, setMatches:Function}

export default async function endGame ({ match, players, user, jwt, spectatorIds, navigate, playersNum, setMatches }:endGamePrompts) {
    
    if (spectatorIds.find((p) => p === user.id)) {
        navigate("/dashboard");
        return;
    }

    let playersNumber = playersNum - 1;

    if (match.matchCreatorId === user.id)
        playersNumber = 0;

    const playerId = players.find((p) => p.userId === user.id)?.id;

    if (!playerId) {
        console.error("Player ID not found.");
        return;
    }

    match.playersNumber = playersNumber;

    if (playersNumber === 0) {
        match.state = "FINALIZED";
    } else {
        match.state = "WATIING";
    }

    try {
        await deleteEntity(`/api/v1/players`, jwt, playerId);
        await put(`/api/v1/matches/${match.id}`, jwt, match);
        setMatches(match);
        navigate("/dashboard");
    } catch (error) {
        console.error("Error exiting from match:", error);
    }
};