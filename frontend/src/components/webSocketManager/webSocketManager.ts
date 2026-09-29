/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
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