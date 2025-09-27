package com.nipun.system.document.websocket;

import com.nipun.system.document.dtos.BroadcastContentDto;
import com.nipun.system.document.dtos.BroadcastDocumentStatusDto;
import com.nipun.system.document.share.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.authentication.DocumentWebsocketAuthenticationService;
import com.nipun.system.document.websocket.connection.DocumentWebsocketConnectionService;
import com.nipun.system.document.websocket.state.DocumentWebsocketStateService;
import com.nipun.system.shared.services.WebsocketService;
import com.nipun.system.shared.utils.UserIdUtils;
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

    private final DocumentWebsocketAuthenticationService documentWebsocketAuthenticationService;
    private final DocumentWebsocketStateService documentWebsocketStateService;
    private final DocumentWebsocketConnectionService documentWebsocketConnectionService;
    private final WebsocketService websocketService;

    @SendTo("/document/{documentId}/broadcastStatus")
    @MessageMapping("/document/{documentId}/accept-changes")
    public BroadcastContentDto broadcastDocumentCurrentState(
            @DestinationVariable UUID documentId,
            @Payload BroadcastDocumentStatusDto statusDto,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if(documentWebsocketAuthenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        if(documentWebsocketAuthenticationService.isReadOnlyUser(userId, documentId))
            throw new ReadOnlyDocumentException();

        documentWebsocketStateService.setDocumentStatus(documentId, statusDto.getContent());

        return new BroadcastContentDto(documentId, statusDto.getContent());
    }

    @SubscribeMapping("/document/{documentId}/broadcastStatus")
    public BroadcastContentDto getCurrentState(
            @DestinationVariable UUID documentId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if(documentWebsocketAuthenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        documentWebsocketConnectionService.addConnectedUserToCache(documentId, headerAccessor.getSessionId(), userId);

        websocketService.broadcastPayload(
                "/document/" + documentId + "/broadcastUsers",
                documentWebsocketConnectionService.getConnectedUsers(documentId).getUsers()
        );

        return new BroadcastContentDto(
                documentId, documentWebsocketStateService.getDocumentStatusFromCache(documentId));
    }

    @SubscribeMapping("/document/{documentId}/broadcastUsers")
    public Set<Long> getConnectedUsers(
            @DestinationVariable UUID documentId,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if(documentWebsocketAuthenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        var connectedUsers = documentWebsocketConnectionService.getConnectedUsers(documentId);
        return connectedUsers == null ? new HashSet<>() : connectedUsers.getUsers();
    }
}
