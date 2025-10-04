package com.nipun.system.document.websocket.positions.mouseposition;

import com.nipun.system.shared.utils.UserIdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class MousePositionController {

    private final MousePositionService mousePositionService;

    @MessageMapping("/document/{documentId}/branch/{branchId}/accept-mouse-positions")
    public void broadcastUserMousePositions(
            @DestinationVariable("documentId") UUID documentId,
            @DestinationVariable("branchId") UUID branchId,
            @Payload MousePosition position,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);
        mousePositionService.broadcastUserMousePositions(documentId, branchId, userId, position);
    }
}
