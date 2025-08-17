import React, { useState, useEffect, useRef } from 'react';
import '../static/css/others/SearchBar.css';
import '@fortawesome/fontawesome-free/css/all.min.css';

const splitQuery = (queryStr:string) => {
  const tokens:string[] = [];
  let currentToken = '';
  let inQuotes = false;
  
  for (const char of queryStr) {
    if (char === '"') {
      inQuotes = true;
    } else if (char === ' ' && !inQuotes) {
      if (currentToken) {
        tokens.push(currentToken.trim());
        currentToken = '';
      }
    } else {
      currentToken += char;
    }
  }
  if (currentToken) tokens.push(currentToken.trim());
  return tokens;
};

const splitValues = (valueStr:string) => {
  const values:string[] = [];
  let currentValue = '';
  let inQuotes = false;
  
  for (const char of valueStr) {
    if (char === '"') {
      inQuotes = true;
    } else if (char === ',' && !inQuotes) {
      values.push(currentValue.trim());
      currentValue = '';
    } else {
      currentValue += char;
    }
  }
  if (currentValue) values.push(currentValue.trim());
  
  return values.map(v => v.replace(/"/g, '').trim().toLowerCase());
};

const getNestedValue = (obj, path) => {
  return path.split('.').reduce((acc, part) => acc?.[part], obj);
};

const filterData = (data:[], query:string, attributeMap = {}) => {
  const queryParts = splitQuery(query);
  const filters: Record<string, string[]> = {};
  
  for (const part of queryParts) {
    const [attr, valueStr] = part.split(':');
    if (attr && valueStr) {
      filters[attr.trim()] = splitValues(valueStr);
    }
  }

  return data.filter(item => {
    return Object.entries(filters).every(([attr, values]) => {
      const path = attributeMap[attr] || attr;
      const fieldValue = getNestedValue(item, path)?.toString().toLowerCase().trim() || '';
      return values.some(value => fieldValue.includes(value));
    });
  });
};

export default function SearchBar({ data, setFiltered, placeholder, attributeMap = {} }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [showDropdown, setShowDropdown] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const [showHelp, setShowHelp] = useState(false);

  useEffect(() => {
    if (searchTerm === "") {
      setFiltered(data);
    }
  }, [searchTerm, data, setFiltered]);

  const attributes = React.useMemo(() => {
    if (!data.length) return [];
    const dataKeys = Object.keys(data[0]);
    const mappedKeys = Object.keys(attributeMap);
    const combined = [...new Set([...mappedKeys, ...dataKeys])];
    return combined.filter(key => key.toLowerCase() !== 'id');
  }, [data, attributeMap]);

  const handleChange = (e:React.ChangeEvent<HTMLInputElement>) => setSearchTerm(e.target.value);

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  const toggleHelp = () => {
    setShowHelp(!showHelp);
  };

  const handleAttributeClick = (attribute) => {
    let prefix = searchTerm.trim();
    if (prefix && !prefix.endsWith(':')) prefix += ' ';
    setSearchTerm(prefix + attribute + ':');
    setShowDropdown(false);
    requestAnimationFrame(() => inputRef.current?.focus());
  };

  const handleSearch = () => {
    const filteredData = filterData(data, searchTerm, attributeMap);
    setFiltered(filteredData);
  };

  return (
    <>
    <div className="search-container">
      <div className="search-dropdown-container">
        <i className="fa fa-chevron-down search-dropdown-toggle" onClick={toggleDropdown}></i>
        {showDropdown && (
          <div className="search-dropdown">
            {attributes.map((attr) => (
              <div key={attr} className="search-dropdown-item" onClick={() => handleAttributeClick(attr)}>
                {attr}
              </div>
            ))}
          </div>
        )}
      </div>
      <input
        className="search-bar"
        type="text"
        placeholder={placeholder || "Search..."}
        value={searchTerm}
        onChange={handleChange}
        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        ref={inputRef}
      />
      <button className="search-button" onClick={handleSearch}>
        <i className="fa fa-search"></i>
      </button>
    </div>
    <div className="help-icon-container">
    <i className="fa fa-question-circle help-icon" onClick={toggleHelp}></i>
    {showHelp && (
      <div className="help-text">
        <p>When searching for values with spaces, wrap them in double quotes (e.g., <b>clinic:"Clinic 1"</b>)</p>
      </div>
    )}
  </div>
  </>
  );
};