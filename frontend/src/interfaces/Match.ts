export default interface Match {
    id: number,
    name: string,
    password: string,
    state: string,
    playersNumber: number,
    round: number,
    phase: string,
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

export const emptyMatch: Match = { id: 0, name: "", password: "", state: "", playersNumber: 0, round: 0, phase: "", finalScoreCalculated: false, initialPlayerId: undefined, actualPlayerId: undefined, matchCreatorId: 0 };