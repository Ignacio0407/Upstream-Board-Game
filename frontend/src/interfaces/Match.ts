import { Phase } from "./Phase.ts";
import { State } from "./State.ts";

export default interface Match {
    id: number,
    name: string,
    password: string,
    state: State,
    playersNumber: number,
    round: number,
    phase: Phase,
    finalScoreCalculated: boolean,
    initialPlayerId?: number,
    actualPlayerId?: number,
    matchCreatorId: number;
}

export interface DashboardMatch {
    id: number,
    name: string,
    password: string,
    state: string,
    playersNumber: number,
}

export const emptyMatch: Match = { id: 0, name: "", password: "", state: State.WAITING, playersNumber: 0, round: 0, phase: Phase.TILES, finalScoreCalculated: false, initialPlayerId: undefined, actualPlayerId: undefined, matchCreatorId: 0 };