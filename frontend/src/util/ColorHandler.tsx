import { Client } from '@stomp/stompjs';
import ColorPickerModal from './ColorPickerModal.tsx';
import {post} from './fetchers.ts'
import { JSX } from 'react';

interface ColorHandlerProps {matchId:number, jwt:string, finalUserId:number, takenColors:string[], onColorChanged:Function, stompClient:Client};

export default function ColorHandler({ matchId, jwt, finalUserId, takenColors, onColorChanged, stompClient }:ColorHandlerProps): JSX.Element {
    async function handleColorChange(color:string) {
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
                console.log("All SalmonMatches have been created.");
                onColorChanged();
            } else {
                console.error('Error creating player:', response.statusText);
            }
        } catch (error) {
            console.error('Unexpected error creating player:', error);
        }
    }

    return (
        <ColorPickerModal onColorSelect={handleColorChange} takenColors={takenColors} />
    );
}