import React, { useState } from 'react';
import { fetchById, fetchByName, fetchByNames } from '../util/fetchers'; // Cambia a la ruta 
import '../static/css/others/SearchBar.css'
import '@fortawesome/fontawesome-free/css/all.min.css'

/**
 * Crea una barra de búsqueda que actualiza
 * @param {function} setter - define el setter del elemento cuya renderización hay que actualizar
 * @param {string} uri - uri of the element to be represented
 */
export default function SearchBar( { setter, uri } ) {
  const [input, setInput] = useState("");

  const handleSearch = () => {
    if (/^\d+$/.test(input)) {
      fetchById(input, uri)
        .then(response => setter([response.data]))
        .catch(error => console.error(error));
    } else if (input.includes(",") || input.includes(" ")) {
      var namesArray = []
      if (input.includes(" ")) {
        namesArray = input.split(",").map(name => name.trim());
      }
      if (input.includes(" ")) {
        namesArray = input.split(" ").map(name => name.trim());
      }
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