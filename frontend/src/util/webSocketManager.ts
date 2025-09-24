import SockJS from 'sockjs-client';
import { Client, Message, StompSubscription } from '@stomp/stompjs';

interface Subscription<T = unknown> {
  topic: string;
  handler: (data: T) => void;
  stompSubscription?: StompSubscription;
}

type ConnectionEvents = {
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: unknown) => void;
};

class WebSocketManager {
  private jwt: string;
  private subscriptions: Subscription<unknown>[] = [];
  public client: Client;
  private activated: boolean;
  private baseUrl?: string;
  private events: ConnectionEvents;
  private useSockJS: boolean;

  constructor(jwt: string, options?: { baseUrl?: string; events?: ConnectionEvents; useSockJS?: boolean }) {
    this.jwt = jwt;
    this.subscriptions = [];
    this.client = new Client();
    this.activated = false;
    this.baseUrl = options?.baseUrl;
    this.events = options?.events || {};
    this.useSockJS = options?.useSockJS ?? true; // Por defecto SockJS
  }

  // Obtain dynamic URL
  private buildSocketUrl(): string {
    if (this.baseUrl) return this.baseUrl; // Manual url
    const host:string = window.location.hostname;
    return `http://${host}:8080/ws-upstream`;
  }

  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;

  private reconnectDelay() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn('Max reconnection attempts reached');
      return 0; // Stop reconnections
    }
    this.reconnectAttempts++;
    return Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts)); // 1s, 2s, 4s...
  }

  initConnection(): void {
    if (this.activated) return;

    this.client = new Client({
      webSocketFactory: () =>
        this.useSockJS
          ? new SockJS(this.buildSocketUrl())
          : new WebSocket(this.buildSocketUrl().replace(/^http/, 'ws')),
      connectHeaders: {
        Authorization: `Bearer ${this.jwt}`,
      },
      reconnectDelay: this.reconnectDelay(),
      onConnect: () => {
        this.reconnectAttempts = 0; // Reset uponc connecting
        this.resubscribeAll();
        this.events.onConnect?.();
      },
      onStompError: (frame) => {
        console.error('❌ STOMP error', frame);
        this.events.onError?.(frame);
      },
      onWebSocketError: (error) => {
        console.warn('⚠️ WebSocket error', error);
        this.events.onError?.(error);
      }
      });

      this.client.activate();
      this.activated = true;
  }


  private resubscribeAll(): void {
    this.subscriptions.forEach((sub) => {
      const subscription = this.client.subscribe(`/topic/${sub.topic}`, (message: Message) => {
        try {
          const parsed = JSON.parse(message.body);
          sub.handler(parsed);
        } catch {
          sub.handler(message.body as any);
        }
      });
      sub.stompSubscription = subscription;
    });
  }


  addSubscription<T = unknown>(topic: string, handler: (data: T) => void): void {
    const existing = this.subscriptions.find((s) => s.topic === topic);
    if (existing) {
      console.warn(`Subscription for topic "${topic}" already exists`);
      return;
    }

    // Only safe suscription; don't suscribe yet
    const subscription: Subscription<T> = {topic, handler: (data: T) => {handler(data)}};
    this.subscriptions.push(subscription as Subscription<unknown>);
  }

  removeSubscription(topic: string): void {
    const subIndex = this.subscriptions.findIndex((s) => s.topic === topic);
    if (subIndex !== -1) {
      const sub = this.subscriptions[subIndex];
      sub.stompSubscription?.unsubscribe();
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