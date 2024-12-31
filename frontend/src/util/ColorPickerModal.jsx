// ColorPickerModal.js
import React, { useState } from 'react';
import {RgbToColor} from './ColorParser';

export default function ColorPickerModal({ onColorSelect , takenColors}) {
    // Estado para el color seleccionado y para la visibilidad del modal
    const [isVisible, setIsVisible] = useState(true);
    const [selectedColor, setSelectedColor] = useState(null);
    

    // Array de colores disponibles
    const colors = ['#FFFF00', '#FF0000', '#008F39', '#572364', '#FFFFFF'];

    // Maneja la selección de color cuando se hace clic en un botón
    const handleColorClick = (color) => {
        
        if (!takenColors.includes(color)) {
            setSelectedColor(color); // Guarda el color seleccionado si está disponible
            console.log(`Color seleccionado: ${color}`); // Muestra el color seleccionado en consola
            console.log(takenColors);
        }
          
        setSelectedColor(color); // Guarda el color seleccionado
    };

    // Maneja el botón "Aceptar"
    const handleAccept = () => {
        if (selectedColor) {
            const colorTransformed = RgbToColor(selectedColor); // Transforma el color RGB a texto
            onColorSelect(colorTransformed);  // Llama a la función del componente padre con el color seleccionado
            setIsVisible(false);           // Oculta el modal
        }
    };

    if (!isVisible) return null; // No renderizar nada si el modal no es visible

    return (
        <div style={styles.modalOverlay}>
            <div style={styles.modalContent}>
                <h3>Escoge un color:</h3>
                <div style={styles.buttonContainer}>
                    {colors.map((color) => (
                        <button
                            key={color}
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
                            {/* Si el color está tomado, muestra una cruz */}
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
        position: 'fixed',
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
        textAlign: 'center',
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
        cursor: 'pointer',
        borderRadius: '5px',
        outline: 'none',
    },
    acceptButton: {
        padding: '10px 20px',
        backgroundColor: '#4CAF50',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
    },
    crossOverlay: {
        position: 'absolute',
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