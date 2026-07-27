/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

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
  private readonly jwt: string;
  private subscriptions: Subscription<unknown>[] = [];
  public client: Client;
  private activated: boolean;
  private readonly baseUrl?: string;
  private readonly events: ConnectionEvents;
  private readonly useSockJS: boolean;

  constructor(jwt: string, options?: { baseUrl?: string; events?: ConnectionEvents; useSockJS?: boolean }) {
    this.jwt = jwt;
    this.subscriptions = [];
    this.client = new Client();
    this.activated = false;
    this.baseUrl = options?.baseUrl;
    this.events = options?.events || {};
    this.useSockJS = options?.useSockJS ?? false; // By default pure WS
  }

  // Obtain dynamic URL
  private buildSocketUrl(): string {
    if (this.baseUrl) return this.baseUrl;
    
    const host = window.location.hostname;
    const port = window.location.port;
    
    //const wsProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const wsProtocol = "wss:";
    
    let socketPort = "";
    if (host === "localhost" || host === "127.0.0.1" || host.startsWith("192.168.")) {
        socketPort = ":8080";
    } else if (port) {
        socketPort = `:${port}`;
    }
    
    return `${wsProtocol}//${host}${socketPort}/ws-upstream`;
  }

  initConnection(): void {
    if (this.activated) return;

    this.client = new Client({
      webSocketFactory: () =>
        this.useSockJS
          ? new SockJS(this.buildSocketUrl())
          : new WebSocket(this.buildSocketUrl()),
      connectHeaders: {
        Authorization: `Bearer ${this.jwt}`,
      },
      reconnectDelay: 0,
      onConnect: () => {
        this.resubscribeAll();
        this.events.onConnect?.();
      },
      onStompError: (frame) => {
        console.error('❌ STOMP error', frame);
        this.events.onError?.(frame);
        this.client.deactivate();
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
    const existing = this.subscriptions.some((s) => s.topic === topic);
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