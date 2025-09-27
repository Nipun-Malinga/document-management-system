package com.nipun.system.document.branch;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.version.Version;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class BranchFactory {

    public Branch createNewBranch(Version version, String branchName, BranchContent content, Document document) {
        var branch = new Branch();

        branch.setDocument(document);
        branch.setVersion(version);
        branch.setContent(content);
        branch.setBranchName(branchName);
        branch.setPublicId(UUID.randomUUID());
        branch.setTimestamp(LocalDateTime.now());

        return branch;
    }
}
