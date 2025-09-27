package com.nipun.system.document.share;

import com.nipun.system.document.base.Document;

public interface SharedDocumentAuthService {
    void checkUserCanWrite(Long userId, Document document);

    boolean isUnauthorizedUser(Long userId, Document document);

    boolean isReadOnlyUser(Long userId, Document document);
}
