package org.gwozdz1uu.heyobackend.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gwozdz1uu.heyobackend.security.JwtService;
import org.gwozdz1uu.heyobackend.service.UserService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        
        // Handle CONNECT command - authenticate and set Principal
        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String username = jwtService.extractUsername(token);
                    
                    if (username != null) {
                        UserDetails userDetails = userService.loadUserByUsername(username);
                        
                        if (jwtService.isTokenValid(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                                );
                            
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);
                            
                            log.info("WebSocket authenticated for user: {}", username);
                        } else {
                            log.warn("Invalid JWT token for WebSocket connection");
                            throw new RuntimeException("Invalid token");
                        }
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication failed", e);
                    throw new RuntimeException("Authentication failed", e);
                }
            } else {
                log.warn("No Authorization header in WebSocket CONNECT");
                throw new RuntimeException("Missing Authorization header");
            }
        }
        
        // For other commands (SEND, SUBSCRIBE, etc.), verify Principal exists
        // If Principal is missing, try to get it from SecurityContext
        if (accessor.getUser() == null && !StompCommand.CONNECT.equals(command)) {
            Principal principal = SecurityContextHolder.getContext().getAuthentication();
            if (principal != null) {
                accessor.setUser(principal);
                log.debug("Restored Principal from SecurityContext for command: {}", command);
            } else {
                log.warn("WebSocket message without authentication: {} - Principal is null", command);
                // Don't block, but log warning - controller will handle it gracefully
            }
        }
        
        return message;
    }
}
