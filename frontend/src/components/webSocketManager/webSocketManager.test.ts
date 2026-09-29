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

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import WebSocketManager from './webSocketManager';
import SockJS from 'sockjs-client';

// 1. Mocking Dependencies
vi.mock('sockjs-client', () => {
  return { default: vi.fn() };
});

let clientConfig: any = {};
const mockClient = {
  activate: vi.fn(),
  deactivate: vi.fn(),
  subscribe: vi.fn(),
  active: false,
};

vi.mock('@stomp/stompjs', () => {
  return {
    Client: vi.fn().mockImplementation(function (this: any, config: any) {
      if (config) {
        clientConfig = config;
      }
      this.activate = mockClient.activate;
      this.deactivate = mockClient.deactivate;
      this.subscribe = mockClient.subscribe;
      
      // Allow dynamic "active" property mocking
      Object.defineProperty(this, 'active', {
        get: () => mockClient.active,
      });
    }),
  };
});

describe('WebSocketManager', () => {
  let wsManager: WebSocketManager;
  const jwt = 'mock-jwt';
  const originalLocation = window.location;

  beforeEach(() => {
    vi.clearAllMocks();
    clientConfig = {};
    mockClient.active = false;
    wsManager = new WebSocketManager(jwt);
  });

  afterEach(() => {
    // Restore window.location after testing URL building
    Object.defineProperty(window, 'location', {
      configurable: true,
      writable: true,
      value: originalLocation,
    });
  });

  // --- Basic Initialization & Connections ---

  it('initializes with correct properties', () => {
    expect(wsManager.client).toBeDefined();
    expect(wsManager.isConnected()).toBe(false);
  });

  it('calls activate on initConnection', () => {
    wsManager.initConnection();
    expect(mockClient.activate).toHaveBeenCalled();
  });

  it('does not reactivate if already activated', () => {
    wsManager.initConnection();
    wsManager.initConnection();
    expect(mockClient.activate).toHaveBeenCalledTimes(1);
  });

  it('returns true for isConnected when activated and client is active', () => {
    wsManager.initConnection();
    mockClient.active = true;
    expect(wsManager.isConnected()).toBe(true);
  });

  // --- URL Builder Edge Cases ---

  const setMockLocation = (hostname: string, port: string) => {
    // @ts-ignore
    delete window.location;
    Object.defineProperty(window, 'location', {
      configurable: true,
      writable: true,
      value: { ...originalLocation, hostname, port },
    });
  };

  it('builds socket URL correctly for localhost (uses port 8080)', () => {
    setMockLocation('localhost', '3000');
    const url = (wsManager as any).buildSocketUrl();
    expect(url).toBe('wss://localhost:8080/ws-upstream');
  });

  it('builds socket URL correctly for non-localhost with port', () => {
    setMockLocation('example.com', '8081');
    const url = (wsManager as any).buildSocketUrl();
    expect(url).toBe('wss://example.com:8081/ws-upstream');
  });

  it('builds socket URL correctly for non-localhost without port', () => {
    setMockLocation('example.com', '');
    const url = (wsManager as any).buildSocketUrl();
    expect(url).toBe('wss://example.com/ws-upstream');
  });

  it('uses baseUrl directly if provided', () => {
    wsManager = new WebSocketManager(jwt, { baseUrl: 'wss://custom.url/ws' });
    expect((wsManager as any).buildSocketUrl()).toBe('wss://custom.url/ws');
  });

  // --- WebSocket Factories ---

  it('uses SockJS when useSockJS is true', () => {
    wsManager = new WebSocketManager(jwt, { useSockJS: true });
    wsManager.initConnection();
    clientConfig.webSocketFactory();
    expect(SockJS).toHaveBeenCalled();
  });

  it('uses native WebSocket when useSockJS is false', () => {
    const mockWebSocket = vi.fn();
    
    vi.stubGlobal('WebSocket', mockWebSocket);

    wsManager = new WebSocketManager(jwt, { useSockJS: false });
    wsManager.initConnection();
    clientConfig.webSocketFactory();

    expect(mockWebSocket).toHaveBeenCalled();
    
    vi.unstubAllGlobals(); 
  });

  // --- Callbacks and Event Handling (Lines 78-79) ---

  it('handles STOMP errors correctly and deactivates client', () => {
    const onErrorSpy = vi.fn();
    wsManager = new WebSocketManager(jwt, { events: { onError: onErrorSpy } });
    wsManager.initConnection();

    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    
    // Simulate STOMP Error
    clientConfig.onStompError('frame data');

    expect(consoleErrorSpy).toHaveBeenCalledWith('❌ STOMP error', 'frame data');
    expect(onErrorSpy).toHaveBeenCalledWith('frame data');
    expect(mockClient.deactivate).toHaveBeenCalled();

    consoleErrorSpy.mockRestore();
  });

  it('handles WebSocket errors correctly', () => {
    const onErrorSpy = vi.fn();
    wsManager = new WebSocketManager(jwt, { events: { onError: onErrorSpy } });
    wsManager.initConnection();

    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    
    // Simulate WS Error
    clientConfig.onWebSocketError('ws disconnected');

    expect(consoleWarnSpy).toHaveBeenCalledWith('⚠️ WebSocket error', 'ws disconnected');
    expect(onErrorSpy).toHaveBeenCalledWith('ws disconnected');

    consoleWarnSpy.mockRestore();
  });

  // --- Subscriptions and Re-subscriptions (Lines 90-108 & 117-126) ---

  it('adds subscriptions and wraps handler successfully', () => {
    const handler = vi.fn();
    wsManager.addSubscription('test-topic', handler);
    const subscriptions = (wsManager as any).subscriptions;
    
    expect(subscriptions).toHaveLength(1);
    expect(subscriptions[0].topic).toBe('test-topic');
  });

  it('prevents duplicate subscriptions and warns (Lines 117-120)', () => {
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    const handler = vi.fn();
    
    wsManager.addSubscription('duplicate-topic', handler);
    wsManager.addSubscription('duplicate-topic', handler);

    expect(consoleWarnSpy).toHaveBeenCalledWith('Subscription for topic "duplicate-topic" already exists');
    expect((wsManager as any).subscriptions).toHaveLength(1); // Only 1 was added
    
    consoleWarnSpy.mockRestore();
  });

  it('resubscribes all topics onConnect and parses JSON properly', () => {
    const onConnectSpy = vi.fn();
    wsManager = new WebSocketManager(jwt, { events: { onConnect: onConnectSpy } });
    
    const handlerSpy = vi.fn();
    wsManager.addSubscription('json-topic', handlerSpy);
    wsManager.initConnection();

    const mockStompSub = { unsubscribe: vi.fn() };
    mockClient.subscribe.mockReturnValue(mockStompSub);

    // Simulate STOMP Connect -> Triggers resubscribeAll
    clientConfig.onConnect();

    expect(onConnectSpy).toHaveBeenCalled();
    expect(mockClient.subscribe).toHaveBeenCalledWith('/topic/json-topic', expect.any(Function));

    // Get the message callback generated by resubscribeAll
    const messageCallback = mockClient.subscribe.mock.calls[0][1];

    // Simulate arriving valid JSON
    messageCallback({ body: '{"payload":"value"}' });
    expect(handlerSpy).toHaveBeenCalledWith({ payload: 'value' });

    // Simulate arriving invalid JSON (Fallback path line 102)
    messageCallback({ body: 'plain-text-data' });
    expect(handlerSpy).toHaveBeenCalledWith('plain-text-data');

    // Verify subscription actually cached the StompSubscription reference
    const subscriptions = (wsManager as any).subscriptions;
    expect(subscriptions[0].stompSubscription).toBe(mockStompSub);
  });

  // --- Unsubscribing & Deactivating (Lines 134-135) ---

  it('unsubscribes using stompSubscription when removing a topic (Lines 134-135)', () => {
    wsManager.initConnection();
    wsManager.addSubscription('topic-to-remove', () => {});

    const mockStompSub = { unsubscribe: vi.fn() };
    mockClient.subscribe.mockReturnValueOnce(mockStompSub);

    // Trigger onConnect to populate sub.stompSubscription
    clientConfig.onConnect();

    wsManager.removeSubscription('topic-to-remove');

    expect(mockStompSub.unsubscribe).toHaveBeenCalled();
    expect((wsManager as any).subscriptions).toHaveLength(0);
  });

  it('does nothing when attempting to remove a non-existent topic', () => {
    wsManager.addSubscription('existing-topic', () => {});
    wsManager.removeSubscription('non-existent-topic');
    expect((wsManager as any).subscriptions).toHaveLength(1);
  });

  it('calls deactivate and safely clears all subscriptions', () => {
    wsManager.initConnection();
    wsManager.addSubscription('topic', () => {});
    
    wsManager.deactivate();
    
    expect(mockClient.deactivate).toHaveBeenCalled();
    expect((wsManager as any).subscriptions).toHaveLength(0);
    expect(wsManager.isConnected()).toBe(false);
  });
});