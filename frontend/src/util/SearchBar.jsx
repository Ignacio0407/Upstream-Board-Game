import React, { useState } from 'react';

export default function SearchBar ({ fetchById, fetchByName, fetchByNames }) { 
  // 1. Crear el estado para almacenar el valor del input
  const [input, setInput] = useState("");

  // 2. Definir la lógica de identificación de métodos y validación del input
  const handleSearch = () => {
    if (/^\d+$/.test(input)) { // Si es solo un número
      fetchById(input); 
    } else if (input.includes(",")) { // Si hay comas, busca múltiples nombres
      fetchByNames(input.split(","));
    } else { // De lo contrario, asume que es un nombre
      fetchByName(input);
    }
  };

    return (
        <div>
          <input 
            type="text" 
            value={input} 
            onChange={(e) => setInput(e.target.value)} 
            placeholder="Buscar partida" 
          />
          <button onClick={handleSearch}>Buscar</button>
        </div>
      );
};