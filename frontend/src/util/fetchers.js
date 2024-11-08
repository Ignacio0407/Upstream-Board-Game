import axios from 'axios';
import tokenService from '../services/token.service';

const jwt = tokenService.getLocalAccessToken();

const uri = "api/v1/matches";

export const fetchById = (id) => {
  return axios.get(uri+`/${id}`, {
    headers: {
      'Authorization': `Bearer ${jwt}`
    }
  });
};

export const fetchByName = (name) => {
  return axios.get(uri+`/name/${name}`, {
    headers: {
      'Authorization': `Bearer ${jwt}`
    }
  });
};

export const fetchByNames = (namesArray) => {
  return axios.get(uri+`/names/${namesArray.join(",")}`, {
    headers: {
      'Authorization': `Bearer ${jwt}`
    }
  });
};
