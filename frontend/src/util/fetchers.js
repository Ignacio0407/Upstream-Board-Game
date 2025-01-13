import axios from 'axios';
import tokenService from '../services/token.service';

const jwt = tokenService.getLocalAccessToken();
const baseUri = "api/v1/";

export const fetchById = async (id, extendedUri) => {
  const uri = `${baseUri}${extendedUri}/${id}`;
  return axios.get(uri, {
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Accept': 'application/json',
      "Content-Type": "application/json",
    },
  });
};

export const fetchByName = async (name, extendedUri) => {
  const uri = `${baseUri}${extendedUri}/name/${name}`;
  return axios.get(uri, {
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Accept': 'application/json',
      "Content-Type": "application/json",
    },
  });
};

export const fetchByNames = async (namesArray, extendedUri) => {
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

export const get = async (uri, jwt) => {
  return fetch(uri, {
    method: "GET",
    headers: {
        Authorization: `Bearer ${jwt}`,
        Accept: "application/json",
        "Content-Type": "application/json",
    },
  });
}

export const patch = async (uri, jwt, data = null) => {
  if (!data) {
    return fetch(uri, {
      method: "PATCH",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      },
    })
  } else {
    return fetch(uri, {
      method: "PATCH",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      },
      body: JSON.stringify(data)
    });
  }
}

export const post = async (uri, jwt, data = null) => {
  if (!data) {
    return fetch(uri, {
      method: "POST",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      },
    })
  } else {
    return fetch(uri, {
      method: "POST",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      },
      body: JSON.stringify(data)
    });
  }
}