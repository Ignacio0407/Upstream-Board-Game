import ColorPickerModal from './ColorPickerModal.tsx';
import {buildSafeUrl, post} from './fetchers.ts'
import { JSX } from 'react';

interface ColorHandlerProps {matchId:number, jwt:string, finalUserId:number, takenColors:string[], onColorChanged:Function, 
    setNewPlayer?:Function};

export default function ColorHandler({ matchId, jwt, finalUserId, takenColors, onColorChanged }:ColorHandlerProps): JSX.Element {
    async function handleColorChange(color:string) {
        try {
            const response = await post(`/api/v1/players/match/${matchId}`, jwt, { color: color, user: finalUserId });

            if (response.ok) {
                const createdPlayer = await response.json();

                post(buildSafeUrl('/api/v1/salmonMatches/player/', createdPlayer.id), jwt);

                onColorChanged();
            } else {
                console.error('Error creating player:', response.statusText);
            }
        } catch (error) {
            console.error('Unexpected error creating player:', error);
        }
    }

    return ( <ColorPickerModal onColorSelect={handleColorChange} takenColors={takenColors} /> );
}