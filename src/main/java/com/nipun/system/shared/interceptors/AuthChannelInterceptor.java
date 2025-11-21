package com.nipun.system.shared.interceptors;

import com.nipun.system.auth.exceptions.InvalidJwtTokenException;
import com.nipun.system.auth.exceptions.JwtTokenNotFoundException;
import com.nipun.system.shared.services.JwtService;
import com.nipun.system.user.websocket.UserWebsocketServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private static final String SESSION_AUTH_KEY = "WS_AUTH";
    private final JwtService jwtService;
    private final UserWebsocketServiceImpl userWebsocketService;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            var authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null) {
                throw new JwtTokenNotFoundException();
            }

            var token = authHeader.replace("Bearer ", "");
            var jwt = jwtService.parseToken(token);

            if (jwt == null || jwt.isExpired()) {
                throw new InvalidJwtTokenException();
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    jwt.getUserId(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getRole()))
            );

            accessor.setUser(authentication);

            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put(SESSION_AUTH_KEY, authentication);
            }

            accessor.setLeaveMutable(true);

            userWebsocketService.addConnectedSessionToCache(
                    (String) message.getHeaders().get("simpSessionId"), accessor.getUser());

            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (accessor.getUser() == null) {
            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
            if (sessionAttrs != null) {
                Object saved = sessionAttrs.get(SESSION_AUTH_KEY);
                if (saved instanceof Authentication auth) {
                    accessor.setUser(auth);
                    accessor.setLeaveMutable(true);
                    return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                }
            }
        }

        return message;
    }
}