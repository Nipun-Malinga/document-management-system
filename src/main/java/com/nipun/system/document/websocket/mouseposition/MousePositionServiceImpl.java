package com.nipun.system.document.websocket.mouseposition;

import com.nipun.system.shared.utils.WebsocketUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class MousePositionServiceImpl implements MousePositionService {

    private final WebsocketUtils websocketUtils;

    @Override
    public void broadcastUserMousePositions(
            MousePosition request,
            Long userId,
            UUID documentId
    ) {
        var endpoint = "/document/" + documentId + "/user/" + userId + "/accept-mouse-positions";
        websocketUtils.broadcastPayload(endpoint, request);
    }
}
