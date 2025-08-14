import SockJS from 'sockjs-client';
import { Client, Message, StompSubscription } from '@stomp/stompjs';

interface Subscription {
  topic: string;
  handler: (data: string) => void;
  stompSubscription?: StompSubscription; // Referencia a la suscripción STOMP
}

class WebSocketManager {
  private jwt: string;
  private subscriptions: Subscription[];
  private client: Client;
  private activated: boolean;

  constructor(jwt: string) {
    this.jwt = jwt;
    this.subscriptions = [];
    this.client = new Client();
    this.activated = false;
  }

  initConnection(): void {
    if (this.activated) return;

    const host = window.location.hostname;
    const socketUrl = `http://${host}:8080/ws-upstream`;

    this.client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      connectHeaders: {
        Authorization: `Bearer ${this.jwt}`,
      },
      debug: (str) => {
        // console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      onConnect: () => {
        //console.log('✅ WebSocket connected');
        this.resubscribeAll();
      },
      onWebSocketError: (error: Event) => {
        console.warn('⚠️ WebSocket error', error);
      },
      onStompError: (frame) => {
        console.error('❌ STOMP error', frame);
      },
    });

    this.client.activate();
    this.activated = true;
  }

  private resubscribeAll(): void {
    this.subscriptions.forEach((sub) => {
      const subscription = this.client.subscribe(`/topic/${sub.topic}`, (message: Message) => {
        sub.handler(message.body);
      });
      sub.stompSubscription = subscription; // Save reference
    });
  }

  addSubscription(topic: string, handler: (data: string) => void): void {
    const existing = this.subscriptions.find((s) => s.topic === topic);
    if (existing) {
      console.warn(`Subscription for topic "${topic}" already exists`);
      return;
    }

    const subscription: Subscription = { topic, handler };

    // Suscribe after connecting
    if (this.client && this.client.active) {
      const stompSub = this.client.subscribe(`/topic/${topic}`, (message: Message) => {
        handler(message.body);
      });
      subscription.stompSubscription = stompSub;
    }

    this.subscriptions.push(subscription);
  }

  removeSubscription(topic: string): void {
    const subIndex = this.subscriptions.findIndex((s) => s.topic === topic);
    if (subIndex !== -1) {
      const sub = this.subscriptions[subIndex];
      sub.stompSubscription?.unsubscribe(); // Cancela la suscripción STOMP
      this.subscriptions.splice(subIndex, 1);
    }
  }

  deactivate(): void {
    if (this.client) {
      this.client.deactivate();
      this.activated = false;
    }
    this.subscriptions = [];
  }

  isConnected(): boolean {
    return this.activated && this.client?.active === true;
  }
}

export default WebSocketManager;