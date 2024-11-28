import axios from 'axios';
import tokenService from '../services/token.service';

const jwt = tokenService.getLocalAccessToken();
const baseUri = "api/v1/";

export const fetchById = (id, extendedUri) => {
  const uri = `${baseUri}${extendedUri}/${id}`;
  return axios.get(uri, {
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Accept': 'application/json',
      "Content-Type": "application/json",
    },
  });
};

export const fetchByName = (name, extendedUri) => {
  const uri = `${baseUri}${extendedUri}/name/${name}`;
  return axios.get(uri, {
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Accept': 'application/json',
      "Content-Type": "application/json",
    },
  });
};

export const fetchByNames = (namesArray, extendedUri) => {
  const uri = `${baseUri}${extendedUri}/names/${namesArray.join(",")}`;
  return axios.get(uri, {
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Accept': 'application/json',
      "Content-Type": "application/json",
    },
  });
};

export const fetchMatchTiles = (matchId) => {
  const uri = `${baseUri}matchTiles/${matchId}`;
  return axios.get(uri, {
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Accept': 'application/json',
      "Content-Type": "application/json",
    },
  });
};

export const fetchTilesList = () => {
  const uri = `${baseUri}tiles`;
  return axios.get(uri, {
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Accept': 'application/json',
      "Content-Type": "application/json",
    },
  });
};