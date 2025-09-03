package com.nipun.system.document.websocket;

import com.nipun.system.document.dtos.BroadcastContentDto;
import com.nipun.system.document.dtos.BroadcastDocumentStatusDto;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class DocumentWebsocketController {

    private final DocumentWebSocketService documentWebSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    @SendTo("/document/{documentId}/broadcastStatus")
    @MessageMapping("/document/{documentId}/accept-changes")
    public BroadcastContentDto broadcastDocumentCurrentState(
            @DestinationVariable UUID documentId,
            @Payload BroadcastDocumentStatusDto statusDto,
            Principal principal
    ) {
        if(documentWebSocketService.isAuthorizedUser(Utils.getUserIdFromPrincipal(principal), documentId)) {
            documentWebSocketService.setDocumentStatus(documentId, statusDto.getContent());
            return new BroadcastContentDto(documentId, statusDto.getContent());
        }

        throw new UnauthorizedDocumentException();
    }

    @SubscribeMapping("/document/{documentId}/broadcastStatus")
    public BroadcastContentDto getCurrentState(
            @DestinationVariable UUID documentId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        var userId = Utils.getUserIdFromPrincipal(principal);

        if(documentWebSocketService.isAuthorizedUser(userId, documentId)) {
            documentWebSocketService.addConnectedUserToCache(documentId, headerAccessor.getSessionId(), userId);

            messagingTemplate.convertAndSend(
                            "/document/" + documentId + "/broadcastUsers",
                            documentWebSocketService.getConnectedUsers(documentId).getUsers());
            return new BroadcastContentDto(
                    documentId, documentWebSocketService.getDocumentStatusFromCache(documentId));
        }

        throw new UnauthorizedDocumentException();
    }

    @SubscribeMapping("/document/{documentId}/broadcastUsers")
    public Set<Long> getConnectedUsers(
            @DestinationVariable UUID documentId,
            Principal principal
    ) {
        var userId = Utils.getUserIdFromPrincipal(principal);

        if(documentWebSocketService.isAuthorizedUser(userId, documentId))
            return documentWebSocketService.getConnectedUsers(documentId).getUsers();

        throw new UnauthorizedDocumentException();
    }
}
