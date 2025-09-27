package com.nipun.system.document.websocket.mouseposition;

import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.authentication.AuthenticationService;
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

    private final AuthenticationService authenticationService;
    private final MousePositionService mousePositionService;

    @MessageMapping("/document/{documentId}/accept-mouse-positions")
    public void broadcastUserMousePositions(
            @DestinationVariable UUID documentId,
            @Payload MousePosition position,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if(authenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        mousePositionService.broadcastUserMousePositions(position, userId, documentId);
    }
}
