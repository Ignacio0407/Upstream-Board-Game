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

export const patch = async (uri, jwt, data) => {
    return fetch(uri, {
      method: "PATCH",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      },
      body: JSON.stringify(data)
    });
};

export const put = async (uri, jwt, data) => {
    return fetch(uri, {
      method: "PUT",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      },
      body: JSON.stringify(data)
    });
};

export const post = async (uri, jwt, data) => {
    return await fetch(uri, {
      method: "POST",
      headers: {
        'Authorization': `Bearer ${jwt}`,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });
};

export const deleteEntity = async (uri, jwt, id) => {
    return fetch(uri + "/" + {id}, {
      method: "PATCH",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      },
      body: JSON.stringify(data)
    });
};