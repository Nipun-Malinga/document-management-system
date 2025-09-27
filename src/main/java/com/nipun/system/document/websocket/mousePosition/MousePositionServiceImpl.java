package com.nipun.system.document.websocket.mousePosition;

import com.nipun.system.shared.services.WebsocketService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class MousePositionServiceImpl implements MousePositionService {

    private final WebsocketService websocketService;

    @Override
    public void broadcastUserMousePositions(
            MousePosition request,
            Long userId,
            UUID documentId
    ) {
        var endpoint = "/document/" + documentId + "/user/" + userId +"/accept-mouse-positions";
        websocketService.broadcastPayload(endpoint, request);
    }
}
