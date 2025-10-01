package com.nipun.system.document.websocket.textposition;

import com.nipun.system.shared.utils.WebsocketUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TextPositionServiceImpl implements TextPositionService {

    private final WebsocketUtils websocketUtils;

    @Override
    public void broadcastUserSelectPositions(
            TextPosition textPosition,
            Long userId,
            UUID documentId
    ) {
        var endpoint = "/document/" + documentId + "/user/" + userId + "/accept-selected-positions";
        websocketUtils.broadcastPayload(endpoint, textPosition);
    }
}
