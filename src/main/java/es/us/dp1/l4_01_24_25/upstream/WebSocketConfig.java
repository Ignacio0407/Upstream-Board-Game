package es.us.dp1.l4_01_24_25.upstream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import es.us.dp1.l4_01_24_25.upstream.configuration.jwt.JwtUtils;
import es.us.dp1.l4_01_24_25.upstream.configuration.services.UserDetailsServiceImpl;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    @Lazy
    private PlayerService playerService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-upstream")
            .setAllowedOriginPatterns(
                "http://localhost:8080",
                "https://localhost:8080",
                "http://localhost:3000",
                "https://localhost:3000",
                "http://127.0.0.1:3000",
                "https://127.0.0.1:3000",
                "http://192.168.*.*:3000",
                "https://192.168.*.*:3000",
                "https://*.ngrok-free.app"
            )
            .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String jwt = authHeader.substring(7);
                        if (jwtUtils.validateJwtToken(jwt)) {
                            String username = jwtUtils.getUserNameFromJwtToken(jwt);
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);
                        } else {
                            throw new org.springframework.messaging.MessageDeliveryException(message, "Invalid JWT token");
                        }
                    } else {
                        throw new org.springframework.messaging.MessageDeliveryException(message, "Missing Authorization header");
                    }
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (destination != null && destination.startsWith("/topic/chat/")) {
                        try {
                            Integer matchId = Integer.parseInt(destination.substring("/topic/chat/".length()));
                            Authentication auth = (Authentication) accessor.getUser();
                            if (auth != null) {
                                String username = auth.getName();
                                if (!playerService.isUserInMatch(username, matchId)) {
                                    throw new org.springframework.messaging.MessageDeliveryException(message, "You are not a player in this match");
                                }
                            } else {
                                throw new org.springframework.messaging.MessageDeliveryException(message, "Not authenticated");
                            }
                        } catch (NumberFormatException e) {
                            // Ignorar si no es un número válido
                        }
                    }
                }
                return message;
            }
        });
    }
}