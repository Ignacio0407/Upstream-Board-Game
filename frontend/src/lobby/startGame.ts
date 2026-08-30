import {patch, post} from '../util/fetchers.ts';

interface StartGamePrompts {matchId:number, jwt:string, setLoading:Function};

const startGame = async ({ matchId, jwt, setLoading }:StartGamePrompts) => {
    setLoading(true);
    await patch(`/api/v1/matches/${matchId}/startGame`, jwt);
    await post(`/api/v1/matchTiles/createMatchTiles/${matchId}`, jwt);
};

export default startGame;