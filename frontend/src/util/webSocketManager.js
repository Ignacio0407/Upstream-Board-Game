import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketManager {
  constructor(jwt) {
    this.jwt = jwt;
    this.subscriptions = []; // [{ topic, handler }]
    this.client = null;
    this.socket = null;
    this.activated = false;
  }

  initConnection() {
    if (this.activated) return; // Avoid duplied connection!

    const host = window.location.hostname;
    const socketUrl = `http://${host}:8080/ws-upstream`;
    this.socket = new SockJS(socketUrl);

    this.client = new Client({
      webSocketFactory: () => this.socket,
      connectHeaders: {
        Authorization: `Bearer ${this.jwt}`
      },
      debug: () => {}, // Logs if required
      reconnectDelay: 5000, // Try reconection in 5s
      onConnect: () => {
        this.subscriptions.forEach(({ topic, handler }) => {
          this.client.subscribe(`/topic/${topic}`, (msg) => {
            handler(msg.body);
          });
        });
      },
      onWebSocketError: (error) => {
        console.warn('⚠️ WebSocket error', error);
      }
    });

    this.client.activate();
    this.activated = true;
  }

  addSubscription(topic, handler) {
    this.subscriptions.push({ topic, handler });

    // If we are connected, add directly
    if (this.client && this.client.connected) {
      this.client.subscribe(`/topic/${topic}`, (msg) => {
        handler(msg.body);
      });
    }
  }

  deactivate() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.activated = false;
    }
    this.subscriptions = [];
  }
}

export default WebSocketManager;