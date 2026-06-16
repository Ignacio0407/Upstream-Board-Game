export const get = async (uri:string, jwt:string|null) => {
  return fetch(uri, {
    method: "GET",
    headers: {
        Authorization: `Bearer ${jwt}`,
        Accept: "application/json",
        "Content-Type": "application/json",
    },
  });
}

export const patch = async (uri:string, jwt:string|null, data?:any) => {
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

export const put = async (uri:string, jwt:string|null, data?:any) => {
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

export const post = async (uri:string, jwt:string|null, data?:any) => {
    return await fetch(uri, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${jwt}`,
        Accept: 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });
};

export const deleteEntity = async (uri:string, jwt:string|null, id:number) => {
    return fetch(uri + "/" + id, {
      method: "DELETE",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      }
    });
};