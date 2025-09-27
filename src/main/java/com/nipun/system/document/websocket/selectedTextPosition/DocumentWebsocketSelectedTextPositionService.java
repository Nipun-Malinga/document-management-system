package com.nipun.system.document.websocket.selectedTextPosition;

import java.util.UUID;

public interface DocumentWebsocketSelectedTextPositionService {
    void broadcastUserSelectPositions(SelectedTextPosition selectedTextPosition, Long userId, UUID documentId);
}
