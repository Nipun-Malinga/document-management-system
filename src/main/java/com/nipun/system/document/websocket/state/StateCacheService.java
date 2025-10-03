package com.nipun.system.document.websocket.state;

import java.util.UUID;

public interface StateCacheService {
    void setDocumentState(UUID documentId, UUID branchId, String content);

    String getDocumentState(UUID documentId, UUID branchId);
}
