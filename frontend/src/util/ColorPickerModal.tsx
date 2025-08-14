import { JSX, useState } from 'react';
import {RgbToColor} from './ColorParser.ts';

interface ColorPickerModalPrompts {onColorSelect:Function , takenColors:string[]};

export default function ColorPickerModal({ onColorSelect , takenColors}:ColorPickerModalPrompts):JSX.Element {
    const [isVisible, setIsVisible] = useState(true);
    const [selectedColor, setSelectedColor] = useState("");
    
    const colors = ['#FFFF00', '#FF0000', '#008F39', '#572364', '#FFFFFF'];

    const handleColorClick = (color:string) => {
        
        if (!takenColors.includes(color)) {
            setSelectedColor(color);
            console.log(`Color seleccionado: ${color}`);
            console.log(takenColors);
        }
          
        setSelectedColor(color);
    };

    const handleAccept = () => {
        if (selectedColor) {
            const colorTransformed = RgbToColor(selectedColor);
            onColorSelect(colorTransformed);  // Calls the function of father component with selected color
            setIsVisible(false);
        }
    };

    if (!isVisible) return <></>;

    return (
        <div style={styles.modalOverlay}>
            <div style={styles.modalContent}>
                <h3>Escoge un color:</h3>
                <div style={styles.buttonContainer}>
                    {colors.map((color) => (
                        <button key={color}
                            style={{
                                ...styles.colorButton,
                                backgroundColor: color,
                                border: color === selectedColor ? '3px solid black' : '1px solid #ccc',
                                cursor: takenColors.includes(color) ? 'not-allowed' : 'pointer',
                                position: 'relative', // Para superponer la cruz
                            }}
                            onClick={() => handleColorClick(color)}
                            disabled={takenColors.includes(color)} // Deshabilita el botón si el color está tomado
                        >
                            {takenColors.includes(color) && (
                                <div style={styles.crossOverlay}>✖</div>
                            )}
                        </button>
                    ))}
                </div>
                <button onClick={handleAccept} style={styles.acceptButton}>
                    Aceptar
                </button>
            </div>
        </div>
    );
}

const styles = {
    modalOverlay: {
        position: 'fixed' as const,
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1000,
    },
    modalContent: {
        backgroundColor: '#fff',
        padding: '20px',
        borderRadius: '8px',
        textAlign: 'center' as const,
        boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)',
    },
    buttonContainer: {
        display: 'flex',
        justifyContent: 'center',
        gap: '10px',
        marginTop: '10px',
        marginBottom: '20px',
    },
    colorButton: {
        width: '40px',
        height: '40px',
        cursor: 'pointer' as const,
        borderRadius: '5px',
        outline: 'none',
    },
    acceptButton: {
        padding: '10px 20px',
        backgroundColor: '#4CAF50',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer' as const,
    },
    crossOverlay: {
        position: 'absolute' as const,
        top: '0',
        left: '0',
        width: '100%',
        height: '100%',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        fontSize: '24px',
        color: 'red',
        backgroundColor: 'rgba(255, 255, 255, 0.5)',
        borderRadius: '5px',
    },
};