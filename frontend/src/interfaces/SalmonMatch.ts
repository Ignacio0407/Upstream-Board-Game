import Salmon from './Salmon.ts';
import Coordinate from './Coordinate.ts';

export default interface SalmonMatch {
  id: number;
  playerId: number;
  /** * @min 0 * @max 5 */
  salmonsNumber: number;
  /** * @min 0 * @max 5 */
  spawningNumber: number;
  coordinate: Coordinate;
  salmon: Salmon;
  matchId: number;
}
