package com.nipun.system.document.websocket.state;

import java.util.UUID;

public interface StateService {
    String setDocumentState(UUID documentId, UUID branchId, Long userId, String content);

    String getDocumentState(UUID documentId, UUID branchId, Long userId);
}
