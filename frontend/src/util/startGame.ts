import {patch, post} from './fetchers.ts';
import { Client } from '@stomp/stompjs';
import User from '../interfaces/User.ts'

interface startGamePrompts {matchId:number, jwt:string, stompClient:Client, finalUser:User, setLoading:Function};

const startGame = async ({ matchId, jwt, stompClient, finalUser, setLoading }:startGamePrompts) => {
    setLoading(true);
    
    const requests = async () => {
        await patch(`/api/v1/matches/${matchId}/startGame`, jwt);
        await post(`/api/v1/matchTiles/createMatchTiles/${matchId}`, jwt);
    };

    try {
        await requests();
        stompClient.publish({
            destination: "/app/start",
            body: JSON.stringify({ action: "colorChanged", userId: finalUser.id }),
        });
        console.log("All matchTiles have been created in random order.");
    } catch (error) {
        console.error("Error creating some matchTiles:", error);
    } finally {
        setLoading(false);
    }
    
    window.location.reload();
};

export default startGame;