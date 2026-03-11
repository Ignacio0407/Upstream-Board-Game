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
    return fetch(uri + "/" + id, {
      method: "DELETE",
      headers: {
          Authorization: `Bearer ${jwt}`,
          Accept: "application/json",
          "Content-Type": "application/json",
      }
    });
};

export const createWebSocket = () : WebSocket => {
      // Obtain dynamic host and protocol
      const host:string = window.location.hostname;
      const protocol:string = window.location.protocol; // http: or https:
      const port:string = window.location.port;

      // In development (localhost:3000), point to :8080
      // In production/ngrok, use the same port as the page
      let socketPort = "";
      let socketProtocol = protocol;
      if (host === "localhost" || host === "127.0.0.1") {
          socketPort = ":8080";
          socketProtocol = "https:"; // Backend now requires TLS
      } else if (port) {
          socketPort = `:${port}`;
      }

      // Build WebSocket URL
      const socketUrl:string = `${socketProtocol}//${host}${socketPort}/ws-upstream`;

      // Create SockJS connection (will use wss:// if protocol is https://)
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