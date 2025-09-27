package com.nipun.system.document.websocket.selectedTextPosition;

import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.authentication.DocumentWebsocketAuthenticationService;
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
public class DocumentWebsocketSelectedTextPositionController {

    private final DocumentWebsocketAuthenticationService documentWebsocketAuthenticationService;
    private final DocumentWebsocketSelectedTextPositionService documentWebsocketSelectedTextPositionService;

    @MessageMapping("/document/{documentId}/accept-selected-positions")
    public void acceptUserTextSelectPositions(
            @DestinationVariable UUID documentId,
            @Payload SelectedTextPosition selectedTextPosition,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if(documentWebsocketAuthenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        documentWebsocketSelectedTextPositionService.broadcastUserSelectPositions(selectedTextPosition, userId, documentId);
    }
}
