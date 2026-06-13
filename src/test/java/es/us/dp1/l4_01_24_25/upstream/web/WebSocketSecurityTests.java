/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio GutiÃ©rrez Serrera
 * 
 * ðŸ‡¬ðŸ‡§ This software was developed by Ignacio GutiÃ©rrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio GutiÃ©rrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * ðŸ‡ªðŸ‡¸ Este software ha sido desarrollado por Ignacio GutiÃ©rrez Serrera.
 * Se permite su uso, modificaciÃ³n y distribuciÃ³n con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explÃ­cito al autor.
 * 
 * Si este cÃ³digo se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crÃ©dito correspondiente 
 * a Ignacio GutiÃ©rrez Serrera.
 * (por ejemplo, en los crÃ©ditos, documentaciÃ³n o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribuciÃ³n 
 * del cÃ³digo fuente o binario.
 * -----------------------------------------------------------------------------
 */

package es.us.dp1.l4_01_24_25.upstream.web;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import es.us.dp1.l4_01_24_25.upstream.WebSocketSecurityInterceptor;
import es.us.dp1.l4_01_24_25.upstream.configuration.jwt.JwtUtils;
import es.us.dp1.l4_01_24_25.upstream.configuration.services.UserDetailsServiceImpl;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

public class WebSocketSecurityTests {

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
        interceptor.matchIdCache.clear();
        interceptor.userMatchCache.clear();
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
        // â”€â”€ 1 segment routes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        "SEND,       /topic/chat/1,                     true,    false,      true",
        "SEND,       /topic/movingSalmon/99,            true,    false,      true",
        "SEND,       /topic/tilesAndSalmons/2,          true,    false,      true",
        "SEND,       /topic/herons/3,                   true,    false,      true",
        "SEND,       /topic/heronsUpdated/3,            true,    false,      true",
        "SEND,       /topic/players/2,                  true,    false,      true",
        "SEND,       /topic/movingSalmonFromSea/99,     true,    false,      true",
        "SUBSCRIBE,  /topic/chat/1,                     true,    false,      true",
        "SUBSCRIBE,  /topic/movingSalmon/2,             true,    false,      true",
        // â”€â”€ 2 segments routes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        "SEND,       /topic/tiles/place/1,              true,    false,      true",
        "SEND,       /topic/tiles/rotation/1,           true,    false,      true",
        "SEND,       /topic/salmons/spawn/1,            true,    false,      true",
        "SUBSCRIBE,  /topic/tiles/place/1,              true,    false,      true",
        // â”€â”€ 3 segments routes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        "SEND,       /topic/salmon/spawn/enter/1,       true,    false,      true",
        "SUBSCRIBE,  /topic/salmon/spawn/enter/1,       true,    false,      true",
        // â”€â”€ (EXACT_PATHS) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        "SUBSCRIBE,  /topic/matches/dashboard,          false,   false,      true",
        "SEND,       /topic/matches/dashboard,          false,   false,      true",
        "SUBSCRIBE,  /topic/match/update/5,             true,    false,      true",
        "SEND,       /topic/match/update/5,             true,    false,      true",
        "SUBSCRIBE,  /topic/match/newPlayer/1,          true,    false,      true",
        "SEND,       /topic/match/newPlayer/1,          true,    false,      true",
        "SUBSCRIBE,  /topic/match/start/1,              true,    false,      true",
        "SEND,       /topic/match/start/1,              true,    false,      true",
        // â”€â”€ User not in match â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        "SEND,       /topic/chat/1,                     false,   false,      false",
        "SUBSCRIBE,  /topic/chat/1,                     false,   false,      false",
        // â”€â”€ Invalid Routes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        "SEND,       /topic/invalid,                    true,    false,      false",
        "SUBSCRIBE,  /topic/invalid,                    true,    false,      false",
        "SEND,       /topic/chat/abc,                   true,    false,      false",  // Not numeric matchId
        "SEND,       /topic/chat/,                      true,    false,      false",  // Empty matchId
        // â”€â”€ Big Payload â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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
        interceptor.userMatchCache.put("user", 2);

        StompHeaderAccessor acc = authenticatedAccessor("SEND", "/topic/chat/1");
        when(playerService.isUserInMatch("user", 1)).thenReturn(false);
        Message<byte[]> msg = MessageBuilder.createMessage("ok".getBytes(), acc.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(msg, null)).isInstanceOf(MessageDeliveryException.class);
    }
}
