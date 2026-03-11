package es.us.dp1.l4_01_24_25.upstream.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import es.us.dp1.l4_01_24_25.upstream.configuration.jwt.JwtUtils;
import es.us.dp1.l4_01_24_25.upstream.user.Authority;
import es.us.dp1.l4_01_24_25.upstream.chat.MessageService;
import es.us.dp1.l4_01_24_25.upstream.chat.MessageDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketSecurityTests {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MessageService messageService;

    private String wsUrl;
    private WebSocketStompClient stompClient;

    @BeforeEach
    public void setup() {
        wsUrl = "ws://localhost:" + port + "/ws-upstream";
        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @Test
    public void testConnectionSucceedsWithValidToken() throws Exception {
        String token = jwtUtils.generateTokenFromUsername("player1", Authority.PLAYER);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        StompSession session = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        
        assertTrue(session.isConnected());
        session.disconnect();
    }

    @Test
    public void testConnectionFailsWithoutAuthHeader() {
        assertThrows(Exception.class, () -> {
            stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), new StompHeaders(), new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    public void testConnectionFailsWithInvalidToken() {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer invalid_token");

        assertThrows(Exception.class, () -> {
            stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    public void testConnectionFailsWithInvalidOrigin() {
        String token = jwtUtils.generateTokenFromUsername("player1", Authority.PLAYER);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Origin", "http://malicious-site.com");

        assertThrows(Exception.class, () -> {
            stompClient.connectAsync(wsUrl, httpHeaders, connectHeaders, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    public void testTopicSubscriptionUnauthorizedFails() throws Exception {
        // Ahora player1 NO debería poder suscribirse al chat de una partida en la que no está (ej. match 999)
        String token = jwtUtils.generateTokenFromUsername("player1", Authority.PLAYER);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        StompSession session = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        
        // Esperamos que falle la suscripción (el servidor debería enviar un ERROR o cerrar la conexión)
        assertThrows(Exception.class, () -> {
            session.subscribe("/topic/chat/999", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) { return byte[].class; }
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {}
            });
            // Damos tiempo para que el interceptor actúe
            Thread.sleep(1000);
            if (session.isConnected()) {
                // Si sigue conectado y no hay error, algo falló en la protección
                // Intentamos enviar algo para forzar la verificación si es diferida
                session.send("/topic/chat/999", "test".getBytes());
                Thread.sleep(500);
            }
        });

        session.disconnect();
    }

    @Test
    public void testXSSSanitization() {
        // Test de integración para verificar que el MessageService limpia el HTML malicioso
        MessageDTO dirtyMessage = new MessageDTO();
        dirtyMessage.setPlayerId(1);
        dirtyMessage.setMatchId(1);
        dirtyMessage.setContent("<script>alert('xss')</script>Hello");

        MessageDTO safeMessage = messageService.saveAsDTO(dirtyMessage);

        // Verificamos que el script ha sido eliminado
        assertEquals("Hello", safeMessage.getContent());
    }

    @Test
    public void testMessageSizeLimitEnforced() throws Exception {
        String token = jwtUtils.generateTokenFromUsername("player1", Authority.PLAYER);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        StompSession session = stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        
        byte[] largePayload = new byte[1024 * 1024]; // 1MB
        
        try {
            session.send("/topic/chat/1", largePayload);
            Thread.sleep(1000);
        } catch (Exception e) {
            // Expected
        }
        
        assertTrue(!session.isConnected());
        if (session.isConnected()) {
            session.disconnect();
        }
    }
}
