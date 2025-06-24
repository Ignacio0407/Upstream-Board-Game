import ColorPickerModal from './ColorPickerModal';
import {post} from '../util/fetchers'

export default function ColorHandler({ matchId, jwt, finalUserId, takenColors, onColorChanged, stompClient }) {
    async function handleColorChange(color) {
        console.log(color)
        try {
            const response = await post(`/api/v1/players/match/${matchId}`, jwt, { color: color, user: finalUserId });

            if (response.ok) {
                const createdPlayer = await response.json();
                stompClient.publish({
                    destination: "/app/hello",
                    body: JSON.stringify({ action: "colorChanged", userId: finalUserId }),
                });

                const req = post(`/api/v1/salmonMatches/player/${createdPlayer.id}`, jwt);

                await Promise.all([req]);
                console.log("Todos los SalmonMatch han sido creados.");
                onColorChanged();
            } else {
                console.error('Error al crear el jugador:', response.statusText);
            }
        } catch (error) {
            console.error('Error inesperado al crear el jugador:', error);
        }
    }

    return (
        <ColorPickerModal onColorSelect={handleColorChange} takenColors={takenColors} />
    );
}
