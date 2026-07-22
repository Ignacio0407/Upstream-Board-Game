import { describe, it, expect } from 'vitest';
import { calculateValidMoves } from './calculateValidMoves';
import { GridCell, GRID_WIDTH, GRID_HEIGHT, coordToIndex } from './matchUtil';
import { TileType } from '../interfaces/Tile';
import SalmonMatch from '../interfaces/SalmonMatch';

/**
 * NOTE: `above`, `left`, `right`, `directionEnergy`, `calculateMovementEnergy`, `fromSea*`
 * and `fromGrid*` are not exported from calculateValidMoves.ts — the only public
 * entry point is `calculateValidMoves`. That is why all tests, including parameterized ones,
 * go through it, building a minimal grid with the specific tiles needed to
 * trigger each branch. This is functionally equivalent to a JUnit @CsvSource: the table on the
 * left represents the "parameters" and the columns on the right are the expected results.
 */

const makeTile = (opts: {
    x: number; y: number; type: TileType; orientation?: number; capacity?: number; salmonsNumber?: number;
}) => ({
    id: `${opts.x}-${opts.y}`,
    coordinate: { x: opts.x, y: opts.y },
    orientation: opts.orientation ?? 0,
    capacity: opts.capacity ?? 0,
    salmonsNumber: opts.salmonsNumber ?? 0,
    tile: { type: opts.type, image: '' },
});

const emptyCellTile = { coordinate: null, orientation: 0, capacity: 0, salmonsNumber: 0, tile: null };

const makeGrid = (tiles: ReturnType<typeof makeTile>[]): GridCell[] => {
    const grid: GridCell[] = new Array(18).fill(null).map(() => ({ tile: emptyCellTile as any, salmons: [] }));
    tiles.forEach(t => {
        grid[coordToIndex(t.coordinate.x, t.coordinate.y)] = { tile: t as any, salmons: [] };
    });
    return grid;
};

const makeSalmon = (x: number | null, y: number | null, playerId = 1): SalmonMatch =>
    ({ id: 1, playerId, salmonsNumber: 3, coordinate: x === null ? null : { x, y: y as number } } as any);

const findMove = (moves: ReturnType<typeof calculateValidMoves>, x: number, y: number) =>
    moves.find(m => m.x === x && m.y === y);

describe('calculateValidMoves', () => {

    describe('movimiento "above" (dx=0, dy=+1) — tabla combinatoria de energía y pérdida de salmones', () => {
        // myX=1, myY=2 evita las exclusiones de columna central (solo aplican a dy=0)
        // y las de columna lateral en diagonal (solo aplican quando |dx|=1 y |dy|=1).
        const MY_X = 1, MY_Y = 2, TO_X = 1, TO_Y = 3;

        // myType, myOri, toType, toOri, energyEsperada, salmonLossEsperada, descripción
        const rows: [TileType, number, TileType, number, number, number, string][] = [
            [TileType.WATER, 0, TileType.WATER, 0, 1, 0, 'agua sobre agua: coste base, sin pérdidas'],
            [TileType.WATER, 0, TileType.BEAR, 0, 2, 1, 'entrar en oso con salto (orient. 0/1 arriba): +1 energía, -1 salmón'],
            [TileType.WATER, 0, TileType.BEAR, 2, 1, 0, 'entrar en oso SIN salto (orient. no colineal): coste base'],
            [TileType.WATER, 0, TileType.JUMP, 1, 2, 0, 'entrar en tile de salto activo desde agua: +1 energía, sin pérdida'],
            [TileType.BEAR, 3, TileType.WATER, 0, 2, 1, 'salir de oso en orientación de salto hacia agua: +1 energía, -1 salmón'],
            [TileType.BEAR, 3, TileType.BEAR, 0, 2, 2, 'salir de oso en orient. de salto HACIA otro oso: -2 salmones'],
            [TileType.BEAR, 0, TileType.BEAR, 0, 2, 2, 'oso sin salto propio, pero destino oso con salto activo: -2 salmones'],
            [TileType.BEAR, 0, TileType.JUMP, 1, 2, 1, 'oso sin salto propio, destino jump activo: -1 salmón'],
            [TileType.BEAR, 0, TileType.WATER, 0, 1, 0, 'oso sin ninguna condición de salto: coste base'],
            [TileType.JUMP, 2, TileType.WATER, 0, 2, 0, 'salir de jump en orient. de salto hacia agua: +1 energía, sin pérdida'],
            [TileType.JUMP, 2, TileType.BEAR, 0, 2, 1, 'salir de jump en orient. de salto hacia oso: -1 salmón'],
            [TileType.JUMP, 0, TileType.BEAR, 0, 2, 1, 'jump sin salto propio, destino oso con salto activo: -1 salmón'],
            [TileType.JUMP, 0, TileType.JUMP, 1, 2, 0, 'jump sin salto propio, destino jump activo: sin pérdida adicional'],
            [TileType.JUMP, 0, TileType.WATER, 0, 1, 0, 'jump sin ninguna condición de salto: coste base'],
        ];

        it.each(rows)('myType=%s myOri=%i toType=%s toOri=%i => energy=%i, loss=%i (%s)',
            (myType, myOri, toType, toOri, expectedEnergy, expectedLoss) => {
                const salmon = makeSalmon(MY_X, MY_Y);
                const grid = makeGrid([
                    makeTile({ x: MY_X, y: MY_Y, type: myType, orientation: myOri }),
                    makeTile({ x: TO_X, y: TO_Y, type: toType, orientation: toOri }),
                ]);

                const moves = calculateValidMoves(salmon, grid, 3, 2);
                const move = findMove(moves, TO_X, TO_Y);

                expect(move).toBeDefined();
                expect(move!.energyUsed).toBe(expectedEnergy);
                expect(move!.salmonLoss).toBe(expectedLoss);
                expect(move!.isJump).toBe(false);
            });

        it('el destino EAGLE añade siempre +1 salmón perdido, independientemente del tipo de origen', () => {
            const salmon = makeSalmon(MY_X, MY_Y);
            const grid = makeGrid([
                makeTile({ x: MY_X, y: MY_Y, type: TileType.WATER }),
                makeTile({ x: TO_X, y: TO_Y, type: TileType.EAGLE }),
            ]);
            const move = findMove(calculateValidMoves(salmon, grid, 3, 2), TO_X, TO_Y);
            expect(move!.salmonLoss).toBe(1);
        });

        it('si la energía disponible es insuficiente para el coste calculado, el movimiento no aparece como válido', () => {
            const salmon = makeSalmon(MY_X, MY_Y);
            const grid = makeGrid([
                makeTile({ x: MY_X, y: MY_Y, type: TileType.BEAR, orientation: 3 }), // requiere energyUsed = 2
                makeTile({ x: TO_X, y: TO_Y, type: TileType.WATER }),
            ]);
            const moves = calculateValidMoves(salmon, grid, 1, 2); // solo 1 de energía
            expect(findMove(moves, TO_X, TO_Y)).toBeUndefined();
        });
    });

    describe('movimiento diagonal "left"/"right" (|dx|=1, dy=1) en columna central', () => {
        it('left-diagonal desde columna central usa las orientaciones propias de "left"', () => {
            // myBearOrientations de "left" = [2,3] (distintas de las de "above" = [3,4])
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([
                makeTile({ x: 1, y: 2, type: TileType.BEAR, orientation: 2 }), // salto propio SOLO en "left"/"right", no en "above"
                makeTile({ x: 0, y: 3, type: TileType.WATER }),
            ]);
            const move = findMove(calculateValidMoves(salmon, grid, 3, 2), 0, 3);
            expect(move!.energyUsed).toBe(2);
            expect(move!.salmonLoss).toBe(1); // BEAR -> WATER
        });

        it('right-diagonal desde columna central usa las orientaciones propias de "right"', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([
                makeTile({ x: 1, y: 2, type: TileType.BEAR, orientation: 4 }), // salto propio de "right" = [4,5]
                makeTile({ x: 2, y: 3, type: TileType.WATER }),
            ]);
            const move = findMove(calculateValidMoves(salmon, grid, 3, 2), 2, 3);
            expect(move!.energyUsed).toBe(2);
            expect(move!.salmonLoss).toBe(1);
        });
    });

    describe('movimiento horizontal (dy=0) y exclusiones de dirección', () => {
        it('un movimiento horizontal (dy=0) es válido desde una columna lateral (x=0)', () => {
            const salmon = makeSalmon(0, 2);
            const grid = makeGrid([
                makeTile({ x: 0, y: 2, type: TileType.WATER }),
                makeTile({ x: 1, y: 2, type: TileType.WATER }),
            ]);
            expect(findMove(calculateValidMoves(salmon, grid, 3, 2), 1, 2)).toBeDefined();
        });

        it('se excluyen los movimientos horizontales (dy=0) cuando el salmón está en la columna central (x=1)', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([
                makeTile({ x: 1, y: 2, type: TileType.WATER }),
                makeTile({ x: 0, y: 2, type: TileType.WATER }),
                makeTile({ x: 2, y: 2, type: TileType.WATER }),
            ]);
            const moves = calculateValidMoves(salmon, grid, 3, 2);
            expect(findMove(moves, 0, 2)).toBeUndefined();
            expect(findMove(moves, 2, 2)).toBeUndefined();
        });

        it('se excluyen los movimientos diagonales cuando el salmón está en una columna lateral (x=0 o x=2)', () => {
            const salmon = makeSalmon(0, 2);
            const grid = makeGrid([
                makeTile({ x: 0, y: 2, type: TileType.WATER }),
                makeTile({ x: 1, y: 3, type: TileType.WATER }), // diagonal desde x=0
            ]);
            const moves = calculateValidMoves(salmon, grid, 3, 2);
            expect(findMove(moves, 1, 3)).toBeUndefined();
        });

        it('se descartan destinos fuera del tablero (fuera de rango en x o y)', () => {
            const salmon = makeSalmon(0, GRID_HEIGHT - 1); // fila superior, "above" se saldría del tablero
            const grid = makeGrid([makeTile({ x: 0, y: GRID_HEIGHT - 1, type: TileType.WATER })]);
            const moves = calculateValidMoves(salmon, grid, 3, 2);
            expect(moves.every(m => m.y >= 0 && m.y < GRID_HEIGHT && m.x >= 0 && m.x < GRID_WIDTH)).toBe(true);
        });

        it('no genera movimiento si la celda destino no tiene tile colocada', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([makeTile({ x: 1, y: 2, type: TileType.WATER })]); // (1,3) queda vacía
            const moves = calculateValidMoves(salmon, grid, 3, 2);
            expect(findMove(moves, 1, 3)).toBeUndefined();
        });

        it('devuelve un array vacío si la propia celda del salmón no tiene tile (dato inconsistente)', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([]); // sin tiles en absoluto
            expect(calculateValidMoves(salmon, grid, 3, 2)).toEqual([]);
        });
    });

    describe('saltos sobre casillas llenas (isFull) dentro del tablero', () => {
        it('permite saltar sobre una casilla llena si hay 3 o más de energía y la de aterrizaje está libre', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([
                makeTile({ x: 1, y: 2, type: TileType.WATER }),
                makeTile({ x: 1, y: 3, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }), // llena
                makeTile({ x: 1, y: 4, type: TileType.WATER }), // aterrizaje libre
            ]);
            const moves = calculateValidMoves(salmon, grid, 3, 2);
            const jumpMove = findMove(moves, 1, 4);
            expect(jumpMove).toBeDefined();
            expect(jumpMove!.isJump).toBe(true);
            expect(jumpMove!.energyUsed).toBe(3);
            expect(jumpMove!.jumpedCoords).toEqual({ x: 1, y: 3 });
        });

        it('no permite el salto si no hay suficiente energía (< 3)', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([
                makeTile({ x: 1, y: 2, type: TileType.WATER }),
                makeTile({ x: 1, y: 3, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }),
                makeTile({ x: 1, y: 4, type: TileType.WATER }),
            ]);
            expect(findMove(calculateValidMoves(salmon, grid, 2, 2), 1, 4)).toBeUndefined();
        });

        it('no permite el salto si la casilla de aterrizaje también está llena', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([
                makeTile({ x: 1, y: 2, type: TileType.WATER }),
                makeTile({ x: 1, y: 3, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }),
                makeTile({ x: 1, y: 4, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }),
            ]);
            expect(findMove(calculateValidMoves(salmon, grid, 3, 2), 1, 4)).toBeUndefined();
        });

        it('suma pérdida de salmones acumulada si la casilla saltada y/o la de aterrizaje son BEAR o EAGLE', () => {
            const salmon = makeSalmon(1, 2);
            const grid = makeGrid([
                makeTile({ x: 1, y: 2, type: TileType.WATER }),
                makeTile({ x: 1, y: 3, type: TileType.BEAR, capacity: 1, salmonsNumber: 1 }), // saltada: BEAR
                makeTile({ x: 1, y: 4, type: TileType.EAGLE }), // aterrizaje: EAGLE
            ]);
            const jumpMove = findMove(calculateValidMoves(salmon, grid, 3, 2), 1, 4);
            expect(jumpMove!.salmonLoss).toBe(2); // 1 por saltar sobre oso + 1 por aterrizar en águila
        });
    });

    describe('entrada desde el mar (salmón sin coordenada, fila y=0)', () => {
        it('calcula un movimiento por cada columna con tile libre en la fila 0', () => {
            const salmon = makeSalmon(null, null);
            const grid = makeGrid([
                makeTile({ x: 0, y: 0, type: TileType.WATER }),
                makeTile({ x: 1, y: 0, type: TileType.WATER }),
                makeTile({ x: 2, y: 0, type: TileType.WATER }),
            ]);
            const moves = calculateValidMoves(salmon, grid, 3, 2);
            expect(moves).toHaveLength(3);
            moves.forEach(m => expect(m.y).toBe(0));
        });

        it('entrar en una tile BEAR con orientación de salto (0 o 1) desde el mar cuesta 2 y pierde 1 salmón', () => {
            const salmon = makeSalmon(null, null);
            const grid = makeGrid([makeTile({ x: 0, y: 0, type: TileType.BEAR, orientation: 0 })]);
            const move = findMove(calculateValidMoves(salmon, grid, 3, 2), 0, 0);
            expect(move!.energyUsed).toBe(2);
            expect(move!.salmonLoss).toBe(1);
        });

        it('entrar en una tile JUMP con orientación activa (0,1,5) desde el mar cuesta 2 sin pérdidas', () => {
            const salmon = makeSalmon(null, null);
            const grid = makeGrid([makeTile({ x: 0, y: 0, type: TileType.JUMP, orientation: 5 })]);
            const move = findMove(calculateValidMoves(salmon, grid, 3, 2), 0, 0);
            expect(move!.energyUsed).toBe(2);
            expect(move!.salmonLoss).toBe(0);
        });

        it('entrar en una tile EAGLE desde el mar cuesta 1 pero pierde 1 salmón', () => {
            const salmon = makeSalmon(null, null);
            const grid = makeGrid([makeTile({ x: 0, y: 0, type: TileType.EAGLE })]);
            const move = findMove(calculateValidMoves(salmon, grid, 3, 2), 0, 0);
            expect(move!.energyUsed).toBe(1);
            expect(move!.salmonLoss).toBe(1);
        });

        it('si la tile de la fila 0 está llena, intenta el salto directo a la fila 1 (si hay energía y sitio)', () => {
            const salmon = makeSalmon(null, null);
            const grid = makeGrid([
                makeTile({ x: 0, y: 0, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }),
                makeTile({ x: 0, y: 1, type: TileType.WATER }),
            ]);
            const move = findMove(calculateValidMoves(salmon, grid, 3, 2), 0, 1);
            expect(move).toBeDefined();
            expect(move!.isJump).toBe(true);
            expect(move!.energyUsed).toBe(3);
        });

        it('si la tile de la fila 0 está llena y no hay suficiente energía, no ofrece ningún movimiento en esa columna', () => {
            const salmon = makeSalmon(null, null);
            const grid = makeGrid([
                makeTile({ x: 0, y: 0, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }),
                makeTile({ x: 0, y: 1, type: TileType.WATER }),
            ]);
            const moves = calculateValidMoves(salmon, grid, 2, 2);
            expect(findMove(moves, 0, 0)).toBeUndefined();
            expect(findMove(moves, 0, 1)).toBeUndefined();
        });

        it('si la fila 0 está llena y la fila 1 (aterrizaje) también, esa columna no ofrece movimiento', () => {
            const salmon = makeSalmon(null, null);
            const grid = makeGrid([
                makeTile({ x: 0, y: 0, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }),
                makeTile({ x: 0, y: 1, type: TileType.WATER, capacity: 1, salmonsNumber: 1 }),
            ]);
            expect(findMove(calculateValidMoves(salmon, grid, 3, 2), 0, 1)).toBeUndefined();
        });
    });
});