import Coordinate from "./Coordinate.ts";
import Tile from './Tile.ts';

export default interface MatchTile {
  id: number;
  /** * @min 0 * @max 5 */
  capacity: number;
  /** * @min 0 */ 
  orientation: number;
  /** * @min 0 * @max 5 */
  salmonsNumber: number;
  coordinate: Coordinate;
  tile: Tile;
  matchId: number;
}

export const emptyTile: MatchTile = { id: 0, capacity: 0, orientation: 0, salmonsNumber: 0, coordinate: { x: 0, y: 0 }, tile: { id: 0, image:'', type: '' }, matchId: 0 };