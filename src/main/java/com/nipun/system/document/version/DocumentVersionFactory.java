package com.nipun.system.document.version;

import com.nipun.system.document.Document;
import com.nipun.system.document.branch.DocumentBranch;
import com.nipun.system.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DocumentVersionFactory {

    public DocumentVersion createNewVersion(
            Document document,
            User author
    ) {
        var versionContent = new DocumentVersionContent();

        if(document.getDocumentContent() != null)
            versionContent.setContent(document.getDocumentContent());

        return createNewVersion(document, author, versionContent);
    }

    public DocumentVersion createNewVersion(
            Document document,
            User author,
            String content,
            DocumentBranch branch
    ) {
        var versionContent = new DocumentVersionContent();
        versionContent.setContent(content);

        var version = createNewVersion(document, author, versionContent);
        version.setBranch(branch);

        return version;
    }

    private DocumentVersion createNewVersion(
            Document document,
            User author,
            DocumentVersionContent content
    ) {
        var version = new DocumentVersion();

        version.setDocument(document);
        version.setVersionNumber(UUID.randomUUID());
        version.setContent(content);
        version.setAuthor(author);
        version.setTimestamp(LocalDateTime.now());

        return version;
    }
}
