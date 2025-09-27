package com.nipun.system.document.websocket.state;

import java.util.UUID;

public interface StateService {
    void setDocumentStatus(UUID documentId, String status);

    String getDocumentStatusFromCache(UUID documentId);

    void saveDocumentState(UUID documentId);

}
