package com.nipun.system.document.version;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class VersionFactory {

    public Version createNewVersion(
            Document document,
            User author
    ) {
        var versionContent = new VersionContent();

        if(document.getDocumentContent() != null)
            versionContent.setContent(document.getDocumentContent());

        return createNewVersion(document, author, versionContent);
    }

    public Version createNewVersion(
            Document document,
            User author,
            String content,
            Branch branch
    ) {
        var versionContent = new VersionContent();
        versionContent.setContent(content);

        var version = createNewVersion(document, author, versionContent);
        version.setBranch(branch);

        return version;
    }

    private Version createNewVersion(
            Document document,
            User author,
            VersionContent content
    ) {
        var version = new Version();

        version.setDocument(document);
        version.setVersionNumber(UUID.randomUUID());
        version.setContent(content);
        version.setAuthor(author);
        version.setTimestamp(LocalDateTime.now());

        return version;
    }
}
