package com.nipun.system.document.websocket.positions.textposition;

import java.util.UUID;

public interface TextPositionService {
    void broadcastUserSelectPositions(UUID documentId, UUID branchId, Long userId, TextPosition textPosition);
}
