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

package upstream;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.lang.Nullable;

import upstream.configuration.jwt.JwtUtils;
import upstream.configuration.services.UserDetailsServiceImpl;
import upstream.player.PlayerService;

public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    // Exception messages are not too detailed to not reveal much information (Security through obscurity)
    // 🛡️ is for SEND/SUBSCRIBE while 🔐 is for CONNECT

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final PlayerService playerService;
    public final Map<String, Integer> matchIdCache = new ConcurrentHashMap<>();
    public final Map<String, Integer> userMatchCache = new ConcurrentHashMap<>();

    private static final List<String> EXACT_PREFIXES = 
    List.of("/topic/matches/dashboard", "/topic/match/newPlayer/", "/topic/match/start", "/topic/match/update");

    private static final Map<String, Integer> ROUTE_SEGMENTS = Map.ofEntries(
        Map.entry("chat", 1),
        Map.entry("movingSalmon", 1),
        Map.entry("tilesAndSalmons", 1),
        Map.entry("tilesAndSalmonsByMatchId", 1),
        Map.entry("herons", 1),
        Map.entry("heronsUpdated", 1),
        Map.entry("players", 1),
        Map.entry("movingSalmonFromSea", 1),
        
        Map.entry("tiles/place", 2),
        Map.entry("tiles/rotation", 2),
        Map.entry("salmons/spawn", 2)
    );
    
    private static final int MAX_MESSAGES_PER_MINUTE = 60;

    private final Map<String, AtomicInteger> messageCount = new ConcurrentHashMap<>();
    private final Map<String, Long> lastReset = new ConcurrentHashMap<>();

    public WebSocketSecurityInterceptor(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, PlayerService playerService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.playerService = playerService;
    }

    // 🛡️ Validate complete destination, prevents manipulated routes
    private Integer validateRouteAndExtractMatchId(String destination, Message<?> message) {
        if (destination == null) {
            throw new MessageDeliveryException(message, "Invalid destination");
        }
        for (String prefix : EXACT_PREFIXES) {
            if (destination.startsWith(prefix)) return null;
        }
        if (!destination.startsWith("/topic/")) {
            throw new MessageDeliveryException(message, "Invalid destination");
        }
        String path = destination.substring("/topic/".length());
        String[] segments = path.split("/");
        // Match the route key in map
        String routeKey = null;
        if (segments.length >= 3 && ROUTE_SEGMENTS.containsKey(segments[0] + "/" + segments[1] + "/" + segments[2])) {
            routeKey = segments[0] + "/" + segments[1] + "/" + segments[2];
        } else if (segments.length >= 2 && ROUTE_SEGMENTS.containsKey(segments[0] + "/" + segments[1])) {
            routeKey = segments[0] + "/" + segments[1];
        } else if (segments.length >= 1 && ROUTE_SEGMENTS.containsKey(segments[0])) {
            routeKey = segments[0];
        }    
        else {
            throw new MessageDeliveryException(message, "Invalid destination");
        }
        Integer idIndex = ROUTE_SEGMENTS.get(routeKey);
        if (idIndex == null) {
            throw new MessageDeliveryException(message, "Invalid destination");
        }
        if (segments.length <= idIndex) {
            throw new MessageDeliveryException(message, "Missing matchId");
        }
        String idPart = segments[idIndex];
        for (int i = 0; i < idPart.length(); i++) {
            if (!Character.isDigit(idPart.charAt(i))) {
                throw new MessageDeliveryException(message, "Invalid matchId");
            }
        }
        return Integer.parseInt(idPart);
    }

    // 🔐 Authorization + Validation
    private void validateMessage(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new MessageDeliveryException(message, "Invalid request");
        }
        Authentication auth = (Authentication) accessor.getUser();
        if (auth == null) {
            throw new MessageDeliveryException(message, "Unauthorized");
        }
        String username = auth.getName();
        this.checkRateLimit(username, message);
        this.validatePayload(message);
        Integer matchId = matchIdCache.computeIfAbsent(destination, dest -> validateRouteAndExtractMatchId(dest, message));
        Integer cachedMatchId = userMatchCache.get(username);
        if (matchId != null && !matchId.equals(cachedMatchId)) {
            this.userInMatch(message, matchId, username);
            userMatchCache.put(username, matchId);
        }
    }

    private void userInMatch(Message<?> message, Integer matchId, String username) {
        if (!Boolean.TRUE.equals(this.playerService.isUserInMatch(username, matchId))) throw new MessageDeliveryException(message, "Forbidden");
    }

    // 🛡️ Rate Limit. Prevents DoS (Denial of Service)
    private void checkRateLimit(String username, Message<?> message) {
        long now = System.currentTimeMillis();
        lastReset.putIfAbsent(username, now);
        messageCount.putIfAbsent(username, new AtomicInteger(0));
        if (now - lastReset.get(username) > 60000) {
            messageCount.get(username).set(0);
            lastReset.put(username, now);
        }
        if (messageCount.get(username).incrementAndGet() > MAX_MESSAGES_PER_MINUTE) {
            throw new MessageDeliveryException(message, "Too many requests");
        }
    }

    // 🛡️ Payload Validation. Prevents malicious JSON, huge payloads and XSS if it is later resent to clients
    private void validatePayload(Message<?> message) {
        if (message.getPayload() instanceof byte[] data && data.length > 64 * 1024) {
            throw new MessageDeliveryException(message, "Invalid payload");
        }
    }

    // 🔐 Authentication (Connect)
    private void handleConnect(StompHeaderAccessor accessor, Message<?> message) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessageDeliveryException(message, "Unauthorized");
        }
        String jwt = authHeader.substring(7);
        if (!this.jwtUtils.validateJwtToken(jwt)) {
            throw new MessageDeliveryException(message, "Unauthorized");
        }
        String username = this.jwtUtils.getUserNameFromJwtToken(jwt);
        UserDetails user = this.userDetailsService.loadUserByUsername(username);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        accessor.setUser(auth);
    }

    @Override
    @Nullable
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;
        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        if (command == StompCommand.CONNECT) {
            this.handleConnect(accessor, message);
        } else if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            this.validateMessage(accessor, message);
            if (accessor.getUser() instanceof Authentication authentication) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        return message;
    }
}