package com.nipun.system.document.branch;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.branch.DocumentBranchDto;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.exceptions.*;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.document.version.DocumentVersion;
import com.nipun.system.document.version.DocumentVersionContent;
import com.nipun.system.document.version.DocumentVersionMapper;
import com.nipun.system.document.version.DocumentVersionRepository;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    private final DocumentBranchMapper documentBranchMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final DiffService diffService;

    @Transactional
    public DocumentBranchDto createBranch(UUID documentId, UUID versionId, String branchName) {

        var userId = UserIdUtils.getUserIdFromContext();
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

        return documentBranchMapper.toDto(branch);
    }

    @Cacheable(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    public ContentDto getBranchContent(UUID documentId, UUID branchId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var branch = documentBranchRepository
                .findByPublicIdAndVersionDocumentPublicId(branchId, documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        var document = branch.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        return new ContentDto(branch.getBranchContent());
    }

    @CachePut(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    public ContentDto updateBranchContent(
            UUID documentId,
            UUID branchId,
            String content
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var branch = documentBranchRepository
                .findByPublicIdAndVersionDocumentPublicId(branchId, documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        var document = branch.getVersion().getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        branch.setBranchContent(content);

        var user = userRepository.findById(userId).orElseThrow();

        var versionContent = new DocumentVersionContent();
        versionContent.setContent(content);

        var version = new DocumentVersion();
        version.addData(branch.getVersion().getDocument(), user, versionContent);
        version.setBranch(branch);

        documentVersionRepository.save(version);

        return new ContentDto(branch.getBranchContent());
    }

    public PaginatedData getAllBranches(
            UUID documentId,
            int pageNumber,
            int size
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var branches = documentBranchRepository
                .findAllByDocumentId(document.getId(), pageRequest);

        var branchDtoList = branches.getContent()
                .stream()
                .map(documentBranchMapper::toDto)
                .toList();

        return new PaginatedData(
                branchDtoList,
                branches.getNumber(),
                branches.getSize(),
                branches.getTotalPages(),
                branches.getTotalElements(),
                branches.hasNext(),
                branches.hasPrevious()
        );
    }

    @CacheEvict(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    public void deleteBranch(
            UUID documentId,
            UUID branchId
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

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

    public PaginatedData getAllBranchVersions(
            UUID documentId,
            UUID branchId,
            int pageNumber,
            int size
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentBranchNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest =  PageRequest.of(pageNumber, size);

        var versionList =  documentVersionRepository
                .findAllByDocumentPublicIdAndBranchPublicId(
                        documentId,
                        branchId,
                        pageRequest
                );

        var versionDtoList = versionList.getContent()
                .stream()
                .map(documentVersionMapper::toDto)
                .toList();

        return new PaginatedData(
                versionDtoList,
                versionList.getNumber(),
                versionList.getSize(),
                versionList.getTotalPages(),
                versionList.getTotalElements(),
                versionList.hasNext(),
                versionList.hasPrevious()
        );
    }

    public void mergeToMainBranch(UUID documentId, UUID branchId) throws PatchFailedException {
        var userId = UserIdUtils.getUserIdFromContext();

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

        document.getContent().setContent(
                diffService.patchDocument(document.getContent().getContent(), branch.getContent().getContent())
        );

        documentRepository.save(document);
    }

    public void mergeSpecificBranches(
            UUID documentId, UUID branchId, UUID mergeBranchId
    ) throws PatchFailedException {
        var userId = UserIdUtils.getUserIdFromContext();

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

        branch.getContent().setContent(diffService
                .patchDocument(branch.getContent().getContent(),
                        mergeBranch.getContent().getContent())
        );

        documentRepository.save(document);
    }
}
