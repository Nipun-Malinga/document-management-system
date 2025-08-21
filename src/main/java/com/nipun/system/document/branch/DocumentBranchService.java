package com.nipun.system.document.branch;

import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.exceptions.*;
import com.nipun.system.document.utils.Utils;
import com.nipun.system.document.version.DocumentVersion;
import com.nipun.system.document.version.DocumentVersionContent;
import com.nipun.system.document.version.DocumentVersionRepository;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentBranchService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentBranchRepository documentBranchRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public DocumentBranch createBranch(UUID documentId, UUID versionId, String branchName) {

        var userId = Utils.getUserIdFromContext();
        var user = userRepository.findById(userId).orElseThrow();

        var version = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(versionId, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = version.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        var fetchedBranch = documentBranchRepository.findByBranchName(branchName).orElse(null);

        if(fetchedBranch != null && fetchedBranch.isBranchTitleExistsAlready(branchName))
            throw new BranchTitleAlreadyExistsException();

        var branchContent = new DocumentBranchContent();
        branchContent.setContent(version.getContent().getContent());

        var branch = new DocumentBranch();
        branch.addData(version, branchName, branchContent, document);

        var newVersionContent = new DocumentVersionContent();
        newVersionContent.setContent(version.getContent().getContent());

        var newVersion = new DocumentVersion();
        newVersion.addData(version.getDocument(), user, newVersionContent);
        newVersion.setBranch(branch);

        documentVersionRepository.save(newVersion);

        return branch;
    }

    public DocumentBranchContent getBranchContent(UUID documentId, UUID branchId) {
        var userId = Utils.getUserIdFromContext();

        var branch = documentBranchRepository
                .findByPublicIdAndVersionDocumentPublicId(branchId, documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        var document = branch.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        return branch.getContent();
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
            throw new UnauthorizedDocumentException();

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

    public Page<DocumentBranch> getAllBranches(
            UUID documentId,
            int pageNumber,
            int size
    ) {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentBranchRepository.findAllByDocumentId(document.getId(), pageRequest);
    }

    public void deleteBranch(
            UUID documentId,
            UUID branchId
    ) {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        documentBranchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .ifPresent(documentBranchRepository::delete);
    }

    public Page<DocumentVersion> getAllBranchVersions(
            UUID documentId,
            UUID branchId,
            int pageNumber,
            int size
    ) {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest =  PageRequest.of(pageNumber, size);

        return documentVersionRepository
                .findAllByDocumentPublicIdAndBranchPublicId(
                        documentId,
                        branchId,
                        pageRequest
                );
    }

    public void mergeToMainBranch(UUID documentId, UUID branchId) throws PatchFailedException {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        var branch = documentBranchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .orElseThrow(DocumentBranchNotFoundException::new);

        document.getContent().setContent(Utils.patchDocument(
                document.getContent().getContent(),
                branch.getContent().getContent()
        ));

        documentRepository.save(document);
    }

    public void mergeSpecificBranches(
            UUID documentId, UUID branchId, UUID mergeBranchId
    ) throws PatchFailedException {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        var branch = document
                .getDocumentBranches()
                .stream()
                .filter(item ->
                        item.getPublicId().equals(branchId))
                .findFirst()
                .orElseThrow(DocumentBranchNotFoundException::new);

        var mergeBranch = document
                .getDocumentBranches()
                .stream()
                .filter(item ->
                        item.getPublicId().equals(mergeBranchId))
                .findFirst()
                .orElseThrow(DocumentBranchNotFoundException::new);

        branch.getContent().setContent(Utils.patchDocument(
                              branch.getContent().getContent(),
                              mergeBranch.getContent().getContent()
        ));

        documentRepository.save(document);
    }
}
