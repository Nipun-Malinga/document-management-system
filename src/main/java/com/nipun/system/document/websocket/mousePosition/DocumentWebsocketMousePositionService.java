package com.nipun.system.document.websocket.mousePosition;

import java.util.UUID;

public interface DocumentWebsocketMousePositionService {
    void broadcastUserMousePositions(MousePosition request, Long userId, UUID documentId);
}
