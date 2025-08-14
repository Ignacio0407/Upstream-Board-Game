export default interface Player {
  id: number;
  name: string;
  color: string;
  playerOrder: number;
  alive: boolean;
  points: number;
  energy: number;
  userId: number;
  matchId: number;
}

export const emptyPlayer:Player  = {
  id: 0,
  name: "",
  color: "",
  playerOrder: 0,
  alive: false,
  points: 0,
  energy: 0,
  userId: 0,
  matchId: 0
};