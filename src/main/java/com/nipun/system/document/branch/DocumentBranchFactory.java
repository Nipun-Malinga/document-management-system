package com.nipun.system.document.branch;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.version.Version;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DocumentBranchFactory {

    public DocumentBranch createNewBranch(Version version, String branchName, DocumentBranchContent content, Document document) {
        var branch = new DocumentBranch();

        branch.setDocument(document);
        branch.setVersion(version);
        branch.setContent(content);
        branch.setBranchName(branchName);
        branch.setPublicId(UUID.randomUUID());
        branch.setTimestamp(LocalDateTime.now());

        return branch;
    }
}
