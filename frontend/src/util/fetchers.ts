export const get = async (uri:string, jwt:string|null) => {
  const safeUri = new URL(uri, window.location.origin).href;
  return fetch(safeUri, {
    method: "GET",
    headers: {
        Authorization: `Bearer ${jwt}`,
        Accept: "application/json",
        "Content-Type": "application/json",
    },
  });
}

export const patch = async (uri:string, jwt:string|null, data?:any) => {
    const safeUri = new URL(uri, window.location.origin).href;
    return fetch(safeUri, {
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
    const safeUri = new URL(uri, window.location.origin).href;
    return fetch(safeUri, {
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
    const safeUri = new URL(uri, window.location.origin).href;
    return await fetch(safeUri, {
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
    // Aquí concatenamos el ID primero y luego lo pasamos por la validación
    const safeUri = new URL(`${uri}/${encodeURIComponent(String(id))}`, window.location.origin).href;
    return fetch(safeUri, {
      method: "DELETE",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      }
    });
};