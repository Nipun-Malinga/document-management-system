package com.nipun.system.document.websocket.selectedTextPosition;

import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.DocumentWebSocketService;
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
public class SelectedTextPositionController {

    private final DocumentWebSocketService documentWebSocketService;
    private final SelectedTextPositionService selectedTextPositionService;

    @MessageMapping("/document/{documentId}/accept-selected-positions")
    public void acceptUserTextSelectPositions(
            @DestinationVariable UUID documentId,
            @Payload SelectedTextPosition selectedTextPosition,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if(documentWebSocketService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        selectedTextPositionService.broadcastUserSelectPositions(selectedTextPosition, userId, documentId);
    }
}
