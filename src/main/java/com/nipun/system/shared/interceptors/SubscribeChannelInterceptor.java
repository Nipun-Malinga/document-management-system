package com.nipun.system.shared.interceptors;

import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.authentication.DocumentWebsocketAuthenticationService;
import com.nipun.system.shared.utils.UserIdUtils;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@AllArgsConstructor
@Component
public class SubscribeChannelInterceptor implements ChannelInterceptor {

    private final DocumentWebsocketAuthenticationService authenticationService;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Principal principal = accessor.getUser();

            if (destination != null && destination.matches("^/document/.*/broadcastStatus$")) {
                String documentIdStr = destination.split("/")[2];
                UUID documentId = UUID.fromString(documentIdStr);

                var userId = UserIdUtils.getUserIdFromPrincipal(principal);

                if (userId == null || authenticationService.isUnauthorizedUser(userId, documentId)) {
                    throw new UnauthorizedDocumentException();
                }
            }
        }
        return message;
    }
}
