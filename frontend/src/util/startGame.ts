import {patch, post} from './fetchers.ts';
import { NavigateFunction } from 'react-router-dom';

interface startGamePrompts {matchId:number, jwt:string, setLoading:Function, navigate:NavigateFunction};

const startGame = async ({ matchId, jwt, setLoading, navigate }:startGamePrompts) => {
    setLoading(true);
    await patch(`/api/v1/matches/${matchId}/startGame`, jwt);
    await post(`/api/v1/matchTiles/createMatchTiles/${matchId}`, jwt);
};

export default startGame;