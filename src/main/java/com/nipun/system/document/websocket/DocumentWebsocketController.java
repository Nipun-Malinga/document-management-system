package com.nipun.system.document.websocket;

import com.nipun.system.document.share.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.authentication.AuthenticationService;
import com.nipun.system.document.websocket.connection.ConnectionService;
import com.nipun.system.document.websocket.dtos.BroadcastContentResponse;
import com.nipun.system.document.websocket.dtos.DocumentStatusRequest;
import com.nipun.system.document.websocket.state.StateService;
import com.nipun.system.shared.utils.UserIdUtils;
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
import java.util.HashSet;
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
    public BroadcastContentResponse broadcastDocumentCurrentState(
            @DestinationVariable UUID documentId,
            @Payload DocumentStatusRequest statusDto,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if (authenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        if (authenticationService.isReadOnlyUser(userId, documentId))
            throw new ReadOnlyDocumentException();

        stateService.setDocumentStatus(documentId, statusDto.getContent());

        return new BroadcastContentResponse(documentId, statusDto.getContent());
    }

    @SubscribeMapping("/document/{documentId}/broadcastStatus")
    public BroadcastContentResponse getCurrentState(
            @DestinationVariable UUID documentId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if (authenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        connectionService.addConnectedUserToCache(documentId, headerAccessor.getSessionId(), userId);

        websocketUtils.broadcastPayload(
                "/document/" + documentId + "/broadcastUsers",
                connectionService.getConnectedUsers(documentId).getUsers()
        );

        return new BroadcastContentResponse(
                documentId, stateService.getDocumentStatusFromCache(documentId));
    }

    @SubscribeMapping("/document/{documentId}/broadcastUsers")
    public Set<Long> getConnectedUsers(
            @DestinationVariable UUID documentId,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if (authenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        var connectedUsers = connectionService.getConnectedUsers(documentId);
        return connectedUsers == null ? new HashSet<>() : connectedUsers.getUsers();
    }
}
