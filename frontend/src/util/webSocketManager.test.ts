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

import { describe, it, expect, vi, beforeEach } from 'vitest';
import WebSocketManager from './webSocketManager';

const mockClient = {
    activate: vi.fn(),
    deactivate: vi.fn(),
    subscribe: vi.fn(),
};

vi.mock('@stomp/stompjs', () => {
    return {
        Client: vi.fn().mockImplementation(function (this: any) {
            return mockClient;
        })
    };
});

describe('WebSocketManager', () => {
    let wsManager: WebSocketManager;
    const jwt = 'mock-jwt';

    beforeEach(() => {
        vi.clearAllMocks();
        wsManager = new WebSocketManager(jwt);
    });

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

    it('adds subscriptions and stores them', () => {
        const handler = vi.fn();
        wsManager.addSubscription('test-topic', handler);
        // Accessing private for testing
        expect((wsManager as any).subscriptions).toHaveLength(1);
        expect((wsManager as any).subscriptions[0].topic).toBe('test-topic');
    });

    it('removes subscriptions', () => {
        const handler = vi.fn();
        wsManager.addSubscription('test-topic', handler);
        wsManager.removeSubscription('test-topic');
        expect((wsManager as any).subscriptions).toHaveLength(0);
    });

    it('calls deactivate and clears subscriptions', () => {
        wsManager.initConnection();
        wsManager.addSubscription('topic', () => {});
        wsManager.deactivate();
        expect(mockClient.deactivate).toHaveBeenCalled();
        expect((wsManager as any).subscriptions).toHaveLength(0);
        expect(wsManager.isConnected()).toBe(false);
    });

    it('builds socket URL correctly for localhost', () => {
        // Save original values
        const originalHostname = window.location.hostname;
        const originalPort = window.location.port;

        // Override only the properties needed for the test
        Object.defineProperty(window.location, 'hostname', {value: 'localhost', configurable: true});
        Object.defineProperty(window.location, 'port', {value: '3000', configurable: true});

        const url = (wsManager as any).buildSocketUrl();
        expect(url).toBe('wss://localhost:8080/ws-upstream');

        // Restore original values
        Object.defineProperty(window.location, 'hostname', {value: originalHostname, configurable: true});
        Object.defineProperty(window.location, 'port', {value: originalPort, configurable: true});
    });
});
