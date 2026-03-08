export default interface Tile {
    id: number,
    image: string,
    type: TileType;
}

export enum TileType {
    WATER="WATER", ROCK="ROCK", HERON="HERON", BEAR="BEAR", EAGLE="EAGLE", JUMP="JUMP", SEA="SEA", SPAWNING="SPAWNING"
}