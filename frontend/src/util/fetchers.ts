import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

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
    return fetch(uri + "/" + {id}, {
      method: "DELETE",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      }
    });
};

export const createWebSocket = () : WebSocket => {
      // Obtain dynamic host
      const host:string = window.location.hostname;
      // Build WebSocket URL
      const socketUrl:string = `http://${host}:8080/ws-upstream`;
      // Create SockJS conection 
      return new SockJS(socketUrl);
}

export const createStompClient = (socket:WebSocket , jwt:string) : Client => {
  return new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${jwt}`,
        },
        debug: (str) => console.log(str),
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        },
        onWebSocketError: (error) => console.error('WebSocket error:', error),
        reconnectDelay: 5000, // optional: automatic reconection 
      });
}