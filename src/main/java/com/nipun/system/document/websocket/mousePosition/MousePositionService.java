package com.nipun.system.document.websocket.mousePosition;

import com.nipun.system.shared.services.WebsocketService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class MousePositionService {

    private final WebsocketService websocketService;

    public void broadcastUserMousePositions(
            MousePosition request,
            Long userId,
            UUID documentId
    ) {
        websocketService
                .broadcastPayload(
                        "/document/" + documentId + "/user/" + userId +"/accept-mouse-positions", request);
    }
}
