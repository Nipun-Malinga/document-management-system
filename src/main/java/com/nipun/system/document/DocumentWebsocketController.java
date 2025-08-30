package com.nipun.system.document;

import com.nipun.system.document.dtos.BroadcastContentDto;
import com.nipun.system.document.dtos.BroadcastDocumentStatusDto;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class DocumentWebsocketController {
    private final DocumentService documentService;

    @SendTo("/document/{documentId}/broadcastStatus")
    @MessageMapping("/document/{documentId}/accept-changes")
    public BroadcastContentDto broadcastDocumentCurrentState(
            @DestinationVariable UUID documentId,
            @Payload BroadcastDocumentStatusDto statusDto,
            Principal principal
    ) {
        if(documentService.isAuthorizedUser(Utils.getUserIdFromPrincipal(principal), documentId)) {
            documentService.setDocumentStatus(documentId, statusDto.getContent());
            return new BroadcastContentDto(documentId, statusDto.getContent());
        }

        throw new UnauthorizedDocumentException();
    }

    @SubscribeMapping("/document/{documentId}/broadcastStatus")
    public BroadcastContentDto getCurrentState(
            @DestinationVariable UUID documentId,
            Principal principal
    ) {
        if(documentService.isAuthorizedUser(Utils.getUserIdFromPrincipal(principal), documentId)) {
            return new BroadcastContentDto(
                    documentId, documentService.getDocumentStatusFromCache(documentId));
        }

        throw new UnauthorizedDocumentException();
    }
}
