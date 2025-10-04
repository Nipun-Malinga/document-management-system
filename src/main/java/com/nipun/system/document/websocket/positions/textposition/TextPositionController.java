package com.nipun.system.document.websocket.positions.textposition;

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
public class TextPositionController {

    private final TextPositionService textPositionService;

    @MessageMapping("/document/{documentId}/branch/{branchId}/accept-selected-positions")
    public void acceptUserTextSelectPositions(
            @DestinationVariable("documentId") UUID documentId,
            @DestinationVariable("branchId") UUID branchId,
            @Payload TextPosition textPosition,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);
        textPositionService.broadcastUserSelectPositions(documentId, branchId, userId, textPosition);
    }
}
