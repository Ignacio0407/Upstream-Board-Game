import { deleteEntity, put } from "./fetchers.ts";
import Match from '../interfaces/Match.ts'
import Player from '../interfaces/Player.ts'
import User from '../interfaces/User.ts'
import { State } from "../interfaces/State.ts";

interface endGamePrompts {match:Match, players:Player[], user:User, jwt:string, spectatorIds:number[], 
    navigate:Function, setMatch:Function}

export default async function endGame ({ match, players, user, jwt, spectatorIds, navigate, setMatch }:endGamePrompts) {
    
    if (spectatorIds.find((p) => p === user.id)) {
        navigate("/dashboard");
        return;
    }

    let playersNumber = match.playersNumber - 1;

    if (match.matchCreatorId === user.id)
        playersNumber = 0;

    const playerId = players.find((p) => p.userId === user.id)?.id;

    if (!playerId) {
        console.error("Player ID not found.");
        return;
    }

    match.playersNumber = playersNumber;

    if (playersNumber === 0) {
        match.state = State.FINALIZED;
    } else {
        match.state = State.WAITING;
    }

    try {
        await deleteEntity(`/api/v1/players`, jwt, playerId);
        await put(`/api/v1/matches/${match.id}`, jwt, match);
        setMatch(match);
        navigate("/dashboard");
    } catch (error) {
        console.error("Error exiting from match:", error);
    }
};