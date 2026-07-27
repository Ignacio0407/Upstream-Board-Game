import ColorPickerModal from './ColorPickerModal.tsx';
import {buildSafeUrl, post} from '../util/fetchers.ts'
import { JSX } from 'react';

interface ColorHandlerProps {matchId:number, jwt:string, userId:number, takenColors:string[], onColorChanged:Function, 
    setNewPlayer?:Function};

export default function ColorHandler({ matchId, jwt, userId, takenColors, onColorChanged }:Readonly<ColorHandlerProps>): JSX.Element {
    async function handleColorChange(color:string) {
        try {
            const response = await post(`/api/v1/players/match/${matchId}`, jwt, { color: color, user: userId });

            if (response.ok) {
                const createdPlayer = await response.json();

                await post(buildSafeUrl('/api/v1/salmonMatches/player/', createdPlayer.id), jwt);

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