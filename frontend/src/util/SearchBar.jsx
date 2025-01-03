import React, { useState, useEffect } from 'react';
import { fetchById, fetchByName, fetchByNames } from '../util/fetchers'; // Cambia a la ruta 
import '../static/css/others/SearchBar.css'
import '@fortawesome/fontawesome-free/css/all.min.css'

/**
 * Crea una barra de búsqueda que actualiza
 * @param {function} setter - define el setter del elemento cuya renderización hay que actualizar
 * @param {string} uri - uri of the element to be represented
 * @param {function} data - define the original data to be rendered if the search content is erased
 */
export default function SearchBar( { setter, uri, data } ) {
  const [input, setInput] = useState("");

  useEffect(() => {
    if (input === "") {
      setter(data); // Restablecer al listado original
    }
  }, [input, data, setter]);

  const handleSearch = () => {
    
    if (/^\d+$/.test(input)) {
      fetchById(input, uri)
        .then(response => setter([response.data]))
        .catch(error => console.error(error));
    } else if (input.includes(",") || input.includes(" ")) {
      const delimiter = input.includes(",") ? "," : " ";
      const namesArray = input.split(delimiter).map(name => name.trim());
      fetchByNames(namesArray, uri)
        .then(response => setter(response.data))
        .catch(error => console.error(error));
    } else {
      fetchByName(input, uri)
        .then(response => setter([response.data]))
        .catch(error => console.error(error));
    }
  };

  return (
    <div class="search-container">
      <input
        className="search-bar"
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder = {`Search ${uri}`}
      />
      <button className="search-button" onClick={handleSearch}>
      <i className="fa fa-search"></i>
      </button>
    </div>
  );
}