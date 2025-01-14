import React from 'react';
import ColorPickerModal from './ColorPickerModal';

export default function ColorHandler({ matchId, jwt, finalUserId, takenColors, onColorChanged, stompClient }) {
    async function handleColorChange(color) {
        try {
            const response = await fetch(`/api/v1/players/match/${matchId}`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${jwt}`,
                    Accept: "application/json",
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ color: color, user: finalUserId }),
            });

            if (response.ok) {
                const createdPlayer = await response.json();
                stompClient.publish({
                    destination: "/app/hello",
                    body: JSON.stringify({ action: "colorChanged", userId: finalUserId }),
                });

                const req = fetch(`/api/v1/salmonMatches/player/${createdPlayer.id}`, {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${jwt}`,
                        Accept: "application/json",
                        "Content-Type": "application/json",
                    },
                });

                await Promise.all([req]);
                console.log("Todos los SalmonMatch han sido creados.");
                onColorChanged();
            } else {
                console.error('Error al crear el jugador:', response.statusText);
            }
        } catch (error) {
            console.error('Error al crear el jugador:', error);
        }
    }

    return (
        <ColorPickerModal onColorSelect={handleColorChange} takenColors={takenColors} />
    );
}
