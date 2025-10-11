package com.nipun.system.document.websocket.base;

import com.nipun.system.document.websocket.autoupdater.AutoUpdater;
import com.nipun.system.document.websocket.connection.ConnectionService;
import com.nipun.system.document.websocket.state.StateService;
import com.nipun.system.document.websocket.state.dtos.StatusRequest;
import com.nipun.system.document.websocket.state.dtos.StatusResponse;
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
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class DocumentWebsocketController {

    private final StateService stateService;
    private final ConnectionService connectionService;
    private final AutoUpdater autoUpdater;

    @SendTo("/document/{documentId}/branch/{branchId}/broadcastStatus")
    @MessageMapping("/document/{documentId}/branch/{branchId}/accept-changes")
    public StatusResponse broadcastDocumentCurrentState(
            @DestinationVariable("documentId") UUID documentId,
            @DestinationVariable("branchId") UUID branchId,
            @Payload StatusRequest request,
            Principal principal
    ) {
        var status = stateService.setDocumentState(documentId, branchId,
                UserIdUtils.getUserIdFromPrincipal(principal), request.getContent());
        return new StatusResponse(status);
    }

    @SubscribeMapping("/document/{documentId}/branch/{branchId}/broadcastStatus")
    public StatusResponse getCurrentState(
            @DestinationVariable("documentId") UUID documentId,
            @DestinationVariable("branchId") UUID branchId,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        connectionService.registerConnectedUser(
                documentId, branchId,
                headerAccessor.getSessionId(), UserIdUtils.getUserIdFromPrincipal(principal));

        autoUpdater.startAutoUpdater(documentId, branchId);

        var state = stateService.getDocumentState(documentId, branchId, UserIdUtils.getUserIdFromPrincipal(principal));
        return new StatusResponse(state);
    }

    @SubscribeMapping("/document/{documentId}/users")
    public Set<Long> getConnectedUsers(
            @DestinationVariable("documentId") UUID documentId
    ) {
        return connectionService.getAllConnectedUsers(documentId);
    }

    @SubscribeMapping("/document/{documentId}/branch/{branchId}/users")
    public Set<Long> getConnectedUsers(
            @DestinationVariable("documentId") UUID documentId,
            @DestinationVariable("branchId") UUID branchId
    ) {
        return connectionService.getConnectedUsers(documentId, branchId);
    }
}
