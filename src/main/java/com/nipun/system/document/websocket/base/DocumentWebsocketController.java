package com.nipun.system.document.websocket.base;

import com.nipun.system.document.websocket.authentication.AuthenticationService;
import com.nipun.system.document.websocket.connection.ConnectionService;
import com.nipun.system.document.websocket.dtos.StatusRequest;
import com.nipun.system.document.websocket.dtos.StatusResponse;
import com.nipun.system.document.websocket.state.StateService;
import com.nipun.system.shared.utils.WebsocketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class DocumentWebsocketController {

    private final AuthenticationService authenticationService;
    private final StateService stateService;
    private final ConnectionService connectionService;
    private final WebsocketUtils websocketUtils;

    @SendTo("/document/{documentId}/broadcastStatus")
    @MessageMapping("/document/{documentId}/accept-changes")
    public StatusResponse broadcastDocumentCurrentState(
            @DestinationVariable UUID documentId,
            @Payload StatusRequest statusDto,
            Principal principal
    ) {
        return null;
    }

    @SubscribeMapping("/document/{documentId}/broadcastStatus")
    public StatusResponse getCurrentState(
            @DestinationVariable UUID documentId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        return null;
    }

    @SubscribeMapping("/document/{documentId}/broadcastUsers")
    public Set<Long> getConnectedUsers(
            @DestinationVariable UUID documentId,
            Principal principal
    ) {
        return null;
    }
}
