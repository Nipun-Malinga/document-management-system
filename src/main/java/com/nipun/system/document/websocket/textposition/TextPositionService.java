package com.nipun.system.document.websocket.textposition;

import java.util.UUID;

public interface TextPositionService {
    void broadcastUserSelectPositions(TextPosition textPosition, Long userId, UUID documentId);
}
