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

package upstream.web;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import upstream.WebSocketSecurityInterceptor;
import upstream.configuration.jwt.JwtUtils;
import upstream.configuration.services.UserDetailsServiceImpl;
import upstream.player.PlayerService;

class WebSocketSecurityTests {

    private JwtUtils jwtUtils;
    private UserDetailsServiceImpl userDetailsService;
    private PlayerService playerService;
    private WebSocketSecurityInterceptor interceptor;

    @BeforeEach
    void setup() {
        jwtUtils = mock(JwtUtils.class);
        userDetailsService = mock(UserDetailsServiceImpl.class);
        playerService = mock(PlayerService.class);
        interceptor = new WebSocketSecurityInterceptor(jwtUtils, userDetailsService, playerService);
        interceptor.getMatchIdCache().clear();
        interceptor.getUserMatchCache().clear();
    }

    // Accessor with autenticated user
    private StompHeaderAccessor authenticatedAccessor(String command, String destination) {
        StompHeaderAccessor acc = StompHeaderAccessor.create(StompCommand.valueOf(command));
        acc.setLeaveMutable(true);
        acc.setDestination(destination);
        UserDetails user = User.withUsername("user").password("x").authorities("PLAYER").build();
        acc.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        return acc;
    }

    // Accessor without authenticated user
    private StompHeaderAccessor unauthenticatedAccessor(String command, String destination) {
        StompHeaderAccessor acc = StompHeaderAccessor.create(StompCommand.valueOf(command));
        acc.setLeaveMutable(true);
        acc.setDestination(destination);
        return acc;
    }

    // 1. CONNECT: JWT authentication
    @ParameterizedTest(name = "[{index}] authHeader=''{0}'' valid={1} â†’ success={2}")
    @CsvSource({
        // authHeader,          jwtValid, shouldSucceed
        "Bearer valid.jwt,      true,     true",   // Valid token
        "Bearer invalid.jwt,    false,    false",  // Invalid token
        "'',                    false,    false",  // No header (empty)
        "Basic dXNlcjpwYXNz,   false,    false",  // header no-Bearer
    })
    void connectAuthentication(String authHeader, boolean jwtValid, boolean shouldSucceed) {
        StompHeaderAccessor acc = StompHeaderAccessor.create(StompCommand.CONNECT);
        acc.setLeaveMutable(true);

        String trimmed = authHeader.trim();
        if (!trimmed.isEmpty()) acc.setNativeHeader("Authorization", trimmed);

        if (trimmed.startsWith("Bearer ")) {
            String token = trimmed.substring(7);
            when(jwtUtils.validateJwtToken(token)).thenReturn(jwtValid);
            if (jwtValid) {
                when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("user");
                UserDetails ud = User.withUsername("user").password("x").authorities("PLAYER").build();
                when(userDetailsService.loadUserByUsername("user")).thenReturn(ud);
            }
        }

        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], acc.getMessageHeaders());

        if (shouldSucceed) {
            assertThatCode(() -> interceptor.preSend(msg, null)).doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> interceptor.preSend(msg, null)).isInstanceOf(MessageDeliveryException.class);
        }
    }

    // 2. SEND / SUBSCRIBE: Routes, authorization and payload validation
    @ParameterizedTest(name = "[{index}] {0} {1} inMatch={2} bigPayload={3} â†’ success={4}")
    @CsvSource({
        // command,    destination,                      inMatch, bigPayload, shouldSucceed
        // 1 segment routes
        "SEND,       /topic/chat/1,                     true,    false,      true",
        "SEND,       /topic/tilesAndSalmons/2,          true,    false,      true",
        "SEND,       /topic/players/2,                  true,    false,      true",
        "SUBSCRIBE,  /topic/chat/1,                     true,    false,      true",
        // 2 segments routes
        "SEND,       /topic/tiles/place/1,              true,    false,      true",
        "SEND,       /topic/tiles/rotation/1,           true,    false,      true",
        "SEND,       /topic/salmons/spawn/1,            true,    false,      true",
        "SUBSCRIBE,  /topic/tiles/place/1,              true,    false,      true",
        "SEND,       /topic/tilesAndSalmonsByMatchId/1, true,    false,      true",
        "SUBSCRIBE,  /topic/tilesAndSalmonsByMatchId/1, true,    false,      true",
        // (EXACT_PATHS)
        "SUBSCRIBE,  /topic/matches/dashboard,          false,   false,      true",
        "SEND,       /topic/matches/dashboard,          false,   false,      true",
        "SUBSCRIBE,  /topic/match/update/5,             true,    false,      true",
        "SEND,       /topic/match/update/5,             true,    false,      true",
        "SUBSCRIBE,  /topic/match/newPlayer/1,          true,    false,      true",
        "SEND,       /topic/match/newPlayer/1,          true,    false,      true",
        "SUBSCRIBE,  /topic/match/start/1,              true,    false,      true",
        "SEND,       /topic/match/start/1,              true,    false,      true",
        // User not in match
        "SEND,       /topic/chat/1,                     false,   false,      false",
        "SUBSCRIBE,  /topic/chat/1,                     false,   false,      false",
        // Invalid Routes
        "SEND,       /topic/invalid,                    true,    false,      false",
        "SUBSCRIBE,  /topic/invalid,                    true,    false,      false",
        "SEND,       /topic/chat/abc,                   true,    false,      false",  // Not numeric matchId
        "SEND,       /topic/chat/,                      true,    false,      false",  // Empty matchId
        // Big Payload
        "SEND,       /topic/chat/1,                     true,    true,       false",
        "SUBSCRIBE,  /topic/chat/1,                     true,    true,       false",
    })
    void sendAndSubscribeValidation(String command, String destination, boolean isInMatch, boolean bigPayload, boolean shouldSucceed) {
        StompHeaderAccessor acc = authenticatedAccessor(command, destination.trim());
        when(playerService.isUserInMatch("user", 1)).thenReturn(isInMatch);
        when(playerService.isUserInMatch("user", 2)).thenReturn(isInMatch);
        when(playerService.isUserInMatch("user", 3)).thenReturn(isInMatch);
        when(playerService.isUserInMatch("user", 5)).thenReturn(isInMatch);
        when(playerService.isUserInMatch("user", 99)).thenReturn(isInMatch);

        byte[] payload = bigPayload ? new byte[70 * 1024] : "ok".getBytes();
        Message<byte[]> msg = MessageBuilder.createMessage(payload, acc.getMessageHeaders());

        if (shouldSucceed) {
            assertThatCode(() -> interceptor.preSend(msg, null)).doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> interceptor.preSend(msg, null)).isInstanceOf(MessageDeliveryException.class);
        }
    }

    // 3. SEND / SUBSCRIBE: unauthenticated user
    // Covers: auth == null in accessor (anonimous session SEND/SUBSCRIBE)
    @ParameterizedTest(name = "[{index}] {0} sin usuario â†’ debe fallar")
    @CsvSource({
        "SEND,      /topic/chat/1",
        "SUBSCRIBE, /topic/chat/1",
    })
    void sendOrSubscribeWithoutAuthenticatedUser_shouldFail(String command, String destination) {
        StompHeaderAccessor acc = unauthenticatedAccessor(command, destination.trim());
        Message<byte[]> msg = MessageBuilder.createMessage("ok".getBytes(), acc.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(msg, null)).isInstanceOf(MessageDeliveryException.class);
    }

    // 4. Rate limit
    // 60 messages allowed, 61 is not allowed
    @ParameterizedTest(name = "[{index}] rate limit in {0} {1}")
    @CsvSource({
        "SEND,      /topic/chat/1",
        "SUBSCRIBE, /topic/chat/1",
    })
    void rateLimitExceeded_shouldFail(String command, String destination) {
        StompHeaderAccessor acc = authenticatedAccessor(command, destination.trim());
        when(playerService.isUserInMatch("user", 1)).thenReturn(true);
        Message<byte[]> msg = MessageBuilder.createMessage("ok".getBytes(), acc.getMessageHeaders());

        for (int i = 0; i < 60; i++) interceptor.preSend(msg, null);

        assertThatThrownBy(() -> interceptor.preSend(msg, null)).isInstanceOf(MessageDeliveryException.class);
    }

    // 5. Cache cross-match
    @Test
    void cachedUserInDifferentMatch_shouldFail() {
        interceptor.getUserMatchCache().put("user", 2);

        StompHeaderAccessor acc = authenticatedAccessor("SEND", "/topic/chat/1");
        when(playerService.isUserInMatch("user", 1)).thenReturn(false);
        Message<byte[]> msg = MessageBuilder.createMessage("ok".getBytes(), acc.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(msg, null)).isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void testNullAccessorAndCommand() {
        Message<byte[]> msgWithoutHeaders = MessageBuilder.createMessage(new byte[0], MessageBuilder.withPayload(new byte[0]).build().getHeaders());
        Message<?> resultWithoutHeaders = interceptor.preSend(msgWithoutHeaders, null);
        
        assertEquals(msgWithoutHeaders, resultWithoutHeaders, "Must return original message if accessor is null");

        StompHeaderAccessor accNoCommand = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accNoCommand.setLeaveMutable(true);
        Message<byte[]> msgNoCommand = MessageBuilder.createMessage(new byte[0], accNoCommand.getMessageHeaders());
        Message<?> resultNoCommand = interceptor.preSend(msgNoCommand, null);
        
        // If command is not CONNECT, SEND or SUBSCRIBE, returns original message
        assertEquals(msgNoCommand, resultNoCommand, "Must return original message if command does not require validation");
    }

    @Test
    void testDestinationNotStartingWithTopic() {
        StompHeaderAccessor acc = authenticatedAccessor("SEND", "/app/chat/1");
        Message<byte[]> msg = MessageBuilder.createMessage("ok".getBytes(), acc.getMessageHeaders());
        assertThatThrownBy(() -> interceptor.preSend(msg, null)).isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void testThreeSegmentRouteKey() {
        StompHeaderAccessor acc = authenticatedAccessor("SEND", "/topic/tiles/place/1");
        when(playerService.isUserInMatch("user", 1)).thenReturn(true);
        Message<byte[]> msg = MessageBuilder.createMessage("ok".getBytes(), acc.getMessageHeaders());
        assertThatCode(() -> interceptor.preSend(msg, null)).doesNotThrowAnyException();
    }

    @Test
    void testRateLimitResetAfterOneMinute() throws Exception {
        StompHeaderAccessor acc = authenticatedAccessor("SEND", "/topic/chat/1");
        when(playerService.isUserInMatch("user", 1)).thenReturn(true);
        Message<byte[]> msg = MessageBuilder.createMessage("ok".getBytes(), acc.getMessageHeaders());

        // Fill rate limit
        for (int i = 0; i < 60; i++) interceptor.preSend(msg, null);

        // Manually simulate time reset in lastReset map
        java.lang.reflect.Field lastResetField = WebSocketSecurityInterceptor.class.getDeclaredField("lastReset");
        lastResetField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Long> lastResetMap = (java.util.Map<String, Long>) lastResetField.get(interceptor);
        lastResetMap.put("user", System.currentTimeMillis() - 61000);

        // Next send should reset counter and succeed
        assertThatCode(() -> interceptor.preSend(msg, null)).doesNotThrowAnyException();
    }

}