package com.nipun.system.document.websocket.mouseposition;

import java.util.UUID;

public interface MousePositionService {
    void broadcastUserMousePositions(MousePosition request, Long userId, UUID documentId);
}
