package com.nipun.system.document.websocket.textposition;

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
public class TextPositionController {

    private final AuthenticationService authenticationService;
    private final TextPositionService textPositionService;

    @MessageMapping("/document/{documentId}/accept-selected-positions")
    public void acceptUserTextSelectPositions(
            @DestinationVariable UUID documentId,
            @Payload TextPosition textPosition,
            Principal principal
    ) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);

        if(authenticationService.isUnauthorizedUser(userId, documentId))
            throw new UnauthorizedDocumentException();

        textPositionService.broadcastUserSelectPositions(textPosition, userId, documentId);
    }
}
