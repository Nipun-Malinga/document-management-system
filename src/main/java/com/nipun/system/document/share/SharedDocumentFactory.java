package com.nipun.system.document.share;

import com.nipun.system.document.base.Document;
import com.nipun.system.user.User;
import org.springframework.stereotype.Component;

@Component
public class SharedDocumentFactory {
    public SharedDocument createNewSharedDocument(User sharedUser, Document document) {
        var sharedDocument = new SharedDocument();

        sharedDocument.setSharedUser(sharedUser);
        sharedDocument.setDocument(document);

        return sharedDocument;
    }
}
