package com.nipun.system.document.websocket.positions.mouseposition;

import java.util.UUID;

public interface MousePositionService {
    void broadcastUserMousePositions(UUID documentId, UUID branchId, Long userId, MousePosition request);
}
