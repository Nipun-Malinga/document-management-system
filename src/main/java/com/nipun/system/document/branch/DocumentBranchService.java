package com.nipun.system.document.branch;

import com.nipun.system.document.common.Utils;
import com.nipun.system.document.exceptions.*;
import com.nipun.system.document.version.DocumentVersion;
import com.nipun.system.document.version.DocumentVersionContent;
import com.nipun.system.document.version.DocumentVersionRepository;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentBranchService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentBranchRepository documentBranchRepository;
    private final UserRepository userRepository;

    @Transactional
    public DocumentBranch createBranch(UUID documentId, UUID versionId, String branchName) {

        var userId = Utils.getUserIdFromContext();
        var user = userRepository.findById(userId).orElseThrow();

        var version = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(versionId, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = version.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new NoSharedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        var branchContent = new DocumentBranchContent();
        branchContent.setContent(version.getContent().getContent());

        var branch = new DocumentBranch();
        branch.addData(version, branchName, branchContent);

        var newVersionContent = new DocumentVersionContent();
        newVersionContent.setContent(version.getContent().getContent());

        var newVersion = new DocumentVersion();
        newVersion.addData(version.getDocument(), user, newVersionContent);
        newVersion.setBranch(branch);
        newVersion.addBranch(branch);

        documentVersionRepository.save(newVersion);

        return branch;
    }

    public DocumentBranchContent updateBranchContent(
            UUID documentId,
            UUID branchId,
            String content
    ) {
        var userId = Utils.getUserIdFromContext();

        var branch = documentBranchRepository
                .findByPublicIdAndVersionDocumentPublicId(branchId, documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        var document = branch.getVersion().getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new NoSharedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        branch.getContent().setContent(content);

        var user = userRepository.findById(userId).orElseThrow();

        var versionContent = new DocumentVersionContent();
        versionContent.setContent(content);

        var version = new DocumentVersion();
        version.addData(branch.getVersion().getDocument(), user, versionContent);
        version.setBranch(branch);

        documentVersionRepository.save(version);

        return branch.getContent();
    }
}
