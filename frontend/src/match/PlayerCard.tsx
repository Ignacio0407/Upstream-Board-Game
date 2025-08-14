import "../static/css/game/playerCard.css"
import { Table } from 'reactstrap';
import { ColorToRgb } from '../util/ColorParser.ts';

interface PlayerCardProps {name?: string; color: string}

/**
 * @param name - Whether the button color is applied to the border. Default: false.
 * @param color - Player color.
 * @returns a Player card for the Lobby.
 */
const PlayerCard: React.FC<PlayerCardProps> = ({ name, color }) => {
  return (
    <Table className="playerCardContainer">
      <tbody>
        <tr className="rowContainer">
          <td className="column-card-name">{name}</td>
          <td
            className="column-card-color"
            style={{ background: ColorToRgb(color) }}
          ></td>
        </tr>
      </tbody>
    </Table>
  );
};

export default PlayerCard;