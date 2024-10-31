import axios from 'axios';
import tokenService from '../services/token.service'

const jwt = tokenService.getLocalAccessToken();

export const fetchById = (id) => {
  return axios.get(`/partidas/${id}`, {
    headers: {
      'Authorization': `Bearer ${jwt}`
    }
  });
};

export const fetchByName = (name) => {
  return axios.get(`/partidas/name/${name}`, {
    headers: {
      'Authorization': `Bearer ${jwt}`
    }
  });
};

export const fetchByNames = (names) => {
  const namesArray = names.split(',').map(name => name.trim()).filter(name => name);
  return axios.get(`/partidas/names/${namesArray.join(",")}`, {
    headers: {
      'Authorization': `Bearer ${jwt}`
    }
  });
};