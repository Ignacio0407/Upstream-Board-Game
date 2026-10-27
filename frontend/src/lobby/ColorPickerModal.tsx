import { JSX, useState } from 'react';
import { RgbToColor } from './ColorParser.ts';
import '../static/css/game/coloPicker.css';

interface ColorPickerModalPrompts {
    readonly onColorSelect: (color: string) => void;
    readonly takenColors: readonly string[];
}

export default function ColorPickerModal({ onColorSelect, takenColors }: Readonly<ColorPickerModalPrompts>): JSX.Element {
    const [isVisible, setIsVisible] = useState(true);
    const [selectedColor, setSelectedColor] = useState("");
    
    const colors = ['#FFFF00', '#FF0000', '#008F39', '#572364', '#FFFFFF'];

    const handleColorClick = (color: string) => {
        if (!takenColors.includes(color)) {
            setSelectedColor(color);
        }
        setSelectedColor(color);
    };

    const handleAccept = () => {
        if (selectedColor) {
            const colorTransformed = RgbToColor(selectedColor);
            onColorSelect(colorTransformed); // Calls the function of father component with selected color
            setIsVisible(false);
        }
    };

    if (!isVisible) return <></>;

    return (
        <div className="modalOverlay">
            <div className="modalContent">
                <h3>Pick a color:</h3>
                <div className="buttonContainer">
                    {colors.map((color) => (
                        <button type="button" key={color} className="colorButton"
                            style={{
                                backgroundColor: color,
                                border: color === selectedColor ? '3px solid black' : '1px solid #ccc',
                                cursor: takenColors.includes(color) ? 'not-allowed' : 'pointer',
                                position: 'relative', // Superpose the X
                            }}
                            onClick={() => handleColorClick(color)}
                            disabled={takenColors.includes(color)}
                        >
                            {takenColors.includes(color) && (
                                <div className="crossOverlay">✖</div>
                            )}
                        </button>
                    ))}
                </div>
                <button type="button" onClick={handleAccept} className="acceptButton">
                    Accept
                </button>
            </div>
        </div>
    );
}
