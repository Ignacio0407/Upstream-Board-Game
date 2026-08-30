import MatchTile from "../interfaces/MatchTile";
import SalmonMatch from "../interfaces/SalmonMatch";
import { TileType } from "../interfaces/Tile";
import { GridCell, GRID_WIDTH, coordToIndex, isFull, GRID_HEIGHT } from "./matchUtil";

// --- NEW TYPES FOR CLIENT-SIDE PREDICTION ---
export type ValidMove = { x: number, y: number, energyUsed: number, salmonLoss: number, isJump: boolean, jumpedCoords?: {x: number, y: number} };

type EnergyResult = { energyUsed: number, salmonLoss: number };
type Direction = 'above' | 'left' | 'right';

interface MovementOrientations {
    toTravelBear: number[];
    toTravelJump: number[];
    myBear: number[];
    myJump: number[];
}

function calculateMovementEnergy(
    myType: TileType,
    toTravelType: TileType,
    toOri: number,
    myOri: number,
    orientations: MovementOrientations
): EnergyResult {
    let energyUsed = 1;
    let salmonLoss = 0;

    const toTravelBearHasJump = toTravelType === TileType.BEAR && orientations.toTravelBear.includes(toOri);
    const toTravelHasJump = toTravelType === TileType.JUMP && orientations.toTravelJump.includes(toOri);

    if (myType === TileType.BEAR) {
        if (orientations.myBear.includes(myOri)) {
            energyUsed = 2;
            salmonLoss += (toTravelType === TileType.BEAR ? 2 : 1);
        } else if (toTravelBearHasJump || toTravelHasJump) {
            energyUsed = 2;
            if (toTravelBearHasJump) {
                salmonLoss += 2;
            } else if (toTravelHasJump) {
                salmonLoss += 1;
            }
        }
    } else if (myType === TileType.JUMP) {
        if (orientations.myJump.includes(myOri)) {
            energyUsed = 2;
            salmonLoss += (toTravelType === TileType.BEAR ? 1 : 0);
        } else if (toTravelBearHasJump || toTravelHasJump) {
            energyUsed = 2;
            salmonLoss += (toTravelBearHasJump ? 1 : 0);
        }
    } else if (toTravelBearHasJump || toTravelHasJump) {
        energyUsed = 2;
        salmonLoss += (toTravelBearHasJump ? 1 : 0);
    }

    return { energyUsed, salmonLoss };
};

const above = (myType: TileType, toTravelType: TileType, toOri: number, myOri: number): EnergyResult =>
    calculateMovementEnergy(myType, toTravelType, toOri, myOri, {
        toTravelBear: [0, 1],
        toTravelJump: [0, 1, 5],
        myBear: [3, 4],
        myJump: [2, 3, 4]
    });

const left = (myType: TileType, toTravelType: TileType, toOri: number, myOri: number): EnergyResult =>
    calculateMovementEnergy(myType, toTravelType, toOri, myOri, {
        toTravelBear: [0, 5],
        toTravelJump: [0, 4, 5],
        myBear: [2, 3],
        myJump: [1, 2, 3]
    });

const right = (myType: TileType, toTravelType: TileType, toOri: number, myOri: number): EnergyResult =>
    calculateMovementEnergy(myType, toTravelType, toOri, myOri, {
        toTravelBear: [1, 2],
        toTravelJump: [0, 1, 2],
        myBear: [4, 5],
        myJump: [3, 4, 5]
    });

const directionEnergy = (
    direction: Direction,
    myType: TileType,
    toTravelType: TileType,
    toOri: number,
    myOri: number
): EnergyResult => {
    if (direction === 'above') return above(myType, toTravelType, toOri, myOri);
    if (direction === 'left') return left(myType, toTravelType, toOri, myOri);
    return right(myType, toTravelType, toOri, myOri);
};

const fromSea = (
    grid: GridCell[],
    energy: number,
    playersNumber: number
): ValidMove[] => {
    const validMoves: ValidMove[] = [];

    for (let targetX = 0; targetX < GRID_WIDTH; targetX++) {
        const targetCell = grid[coordToIndex(targetX, 0)];
        const toTravel = targetCell?.tile;
        if (!toTravel?.tile) continue;

        const toTravelType = toTravel.tile.type as TileType;
        const toOri = toTravel.orientation;

        if (isFull(playersNumber, toTravel)) {
            const jumpMove = fromSeaFull(grid, targetX, toTravel, toTravelType, energy, playersNumber);
            if (jumpMove) validMoves.push(jumpMove);
            continue;
        }

        const normalMove = fromSeaNormal(targetX, toTravelType, toOri, energy);
        if (normalMove) validMoves.push(normalMove);
    }

    return validMoves;
};

const fromSeaFull = (
    grid: GridCell[],
    targetX: number,
    toTravel: MatchTile,
    toTravelType: TileType,
    energy: number,
    playersNumber: number
): ValidMove | null => {
    if (energy < 3) return null;

    const landCell = grid[coordToIndex(targetX, 1)];
    if (!landCell?.tile || isFull(playersNumber, landCell.tile)) return null;

    let salmonLoss = 0;
    if (toTravelType === TileType.BEAR) salmonLoss++;
    if (landCell.tile.tile.type === TileType.BEAR) salmonLoss++;
    if (landCell.tile.tile.type === TileType.EAGLE) salmonLoss++;

    return { x: targetX, y: 1, energyUsed: 3, salmonLoss, isJump: true, jumpedCoords: { x: targetX, y: 0 } };
};

const fromSeaNormal = (
    targetX: number,
    toTravelType: TileType,
    toOri: number,
    energy: number
): ValidMove | null => {
    let energyUsed = 1;
    let salmonLoss = 0;

    if (toTravelType === TileType.BEAR && [0, 1].includes(toOri)) {
        energyUsed = 2;
        salmonLoss++;
    } else if (toTravelType === TileType.JUMP && [0, 1, 5].includes(toOri)) {
        energyUsed = 2;
    } else if (toTravelType === TileType.EAGLE) {
        salmonLoss++;
    }

    if (energyUsed > energy) return null;
    return { x: targetX, y: 0, energyUsed, salmonLoss, isJump: false };
};

function fromGridFull(
    grid: GridCell[],
    targetCoords: { targetX: number, targetY: number, dx: number },
    myType: TileType, toTravelType: TileType,
    energy: number,
    playersNumber: number
): ValidMove | null {
    const { targetX, targetY, dx } = targetCoords;
    if (energy < 3) return null;

    const landX = targetX + dx;
    const landY = targetY + 1;
    if (landY >= GRID_HEIGHT || landX < 0 || landX >= GRID_WIDTH) return null;

    const landCell = grid[coordToIndex(landX, landY)];
    if (!landCell?.tile || isFull(playersNumber, landCell.tile)) return null;

    let salmonLoss = 0;
    if (myType === TileType.BEAR) salmonLoss++;
    if (toTravelType === TileType.BEAR) salmonLoss++;
    if (landCell.tile.tile.type === TileType.BEAR) salmonLoss++;
    if (landCell.tile.tile.type === TileType.EAGLE) salmonLoss++;

    return { x: landX, y: landY, energyUsed: 3, salmonLoss, isJump: true, jumpedCoords: { x: targetX, y: targetY } };
};

function fromGrid(
    direction: Direction,
    targetCoords: { targetX: number, targetY: number },
    myType: TileType, toTravelType: TileType,
    toOri: number, myOri: number,
    energy: number
): ValidMove | null {
    const { targetX, targetY } = targetCoords;
    const { energyUsed, salmonLoss: baseLoss } = directionEnergy(direction, myType, toTravelType, toOri, myOri);
    const salmonLoss = baseLoss + (toTravelType === TileType.EAGLE ? 1 : 0);

    if (energyUsed > energy) return null;
    return { x: targetX, y: targetY, energyUsed, salmonLoss, isJump: false };
};

export const calculateValidMoves = (
    salmon: SalmonMatch,
    grid: GridCell[],
    energy: number,
    playersNumber: number
): ValidMove[] => {
    if (salmon.coordinate?.y == null) {
        return fromSea(grid, energy, playersNumber);
    }

    const { x: myX, y: myY } = salmon.coordinate;
    const myCell = grid[coordToIndex(myX, myY)];
    const myTile = myCell?.tile;
    if (!myTile?.tile) return [];

    const validMoves: ValidMove[] = [];
    const directions: { dx: number, dy: number, type: Direction }[] = [
        { dx: 0, dy: 1, type: 'above' },
        { dx: -1, dy: 1, type: 'left' }, { dx: -1, dy: 0, type: 'left' },
        { dx: 1, dy: 1, type: 'right' }, { dx: 1, dy: 0, type: 'right' }
    ];

    directions.forEach(({ dx, dy, type }) => {
        const targetX = myX + dx;
        const targetY = myY + dy;

        if (targetY < 0 || targetY >= GRID_HEIGHT || targetX < 0 || targetX >= GRID_WIDTH) return;
        if (targetY === myY && myX === 1) return;
        if ([0, 2].includes(myX) && Math.abs(dx) === 1 && Math.abs(dy) === 1) return;

        const targetCell = grid[coordToIndex(targetX, targetY)];
        const toTravel = targetCell?.tile;
        if (!toTravel?.tile) return;

        const myType = myTile.tile.type as TileType;
        const toTravelType = toTravel.tile.type as TileType;
        const myOri = myTile.orientation;
        const toOri = toTravel.orientation;

        let move: ValidMove | null;
        if (isFull(playersNumber, toTravel)) {
            move = fromGridFull(grid, { targetX, targetY, dx }, myType, toTravelType, energy, playersNumber);
        } else {
            move = fromGrid(type, { targetX, targetY }, myType, toTravelType, toOri, myOri, energy);
        }

        if (move) validMoves.push(move);
    });

    return validMoves;
};