package com.nipun.system.document.version;

import com.nipun.system.document.Status;
import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.branch.BranchRepository;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.diff.DiffUtils;
import com.nipun.system.document.diff.dtos.DiffResponse;
import com.nipun.system.document.share.SharedDocumentAuthService;
import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.version.dtos.VersionResponse;
import com.nipun.system.document.version.exceptions.VersionNotFoundException;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Service
public class VersionServiceImpl implements VersionService {

    private final DocumentRepository documentRepository;
    private final VersionRepository versionRepository;

    private final VersionMapper versionMapper;

    private final DiffUtils diffUtils;
    
    private final DiffService diffService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    private final VersionFactory versionFactory;

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;


    @Transactional
    @Override
    public VersionResponse createNewVersion(
            UUID documentId,
            UUID branchId,
            String title,
            Status status
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var user = userRepository.findById(userId).orElseThrow();

        var branch = document.getBranch(branchId);

        if (branch == null)
            throw new BranchNotFoundException();

        var version = versionFactory.createNewVersion(branch, user, branch.getBranchContent(), title, status);

        version = versionRepository.save(version);

        return versionMapper.toDto(version);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedData getAllDocumentVersions(UUID documentId, int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId).orElseThrow(DocumentNotFoundException::new);

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        Page<Version> versions = sharedDocumentAuthService.isUnauthorizedUser(userId, document)
                ? versionRepository.findAllByBranchDocumentIdAndStatusPublic(document.getId(), pageRequest)
                : versionRepository.findAllByBranchDocumentId(document.getId(), pageRequest);

        var versionDtoList = versions.stream()
                .map(versionMapper::toDto)
                .toList();

        return new PaginatedData(
                versionDtoList,
                pageNumber,
                size,
                versions.getTotalPages(),
                versions.getTotalElements(),
                versions.hasNext(),
                versions.hasPrevious()
        );
    }

    @Cacheable(value = "document_version_contents", key = "{#documentId, #versionId}")
    @Transactional(readOnly = true)
    @Override
    public ContentResponse getVersionContent(UUID documentId, UUID versionId) {
        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var version = versionRepository
                .findDocumentBranchVersion(versionId, document.getId())
                .orElseThrow(VersionNotFoundException::new);

        if (version.getStatus().equals(Status.PUBLIC))
            return new ContentResponse(version.getVersionContent());

        var userId = UserIdUtils.getUserIdFromContext();

        if (sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return new ContentResponse(version.getVersionContent());
    }

    @CacheEvict(value = "document_version_contents", key = "{#documentId, #versionId}")
    @Transactional
    @Override
    public void mergeVersionToBranch(UUID documentId, UUID branchId, UUID versionId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var branch = branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .orElseThrow(BranchNotFoundException::new);

        var version = versionRepository
                .findByPublicIdAndBranchDocumentId(versionId, document.getId())
                .orElseThrow(VersionNotFoundException::new);

        var patchedDocument = diffService.patchDocument(
                branch.getBranchContent(),
                version.getVersionContent()
        );

        branch.setBranchContent(patchedDocument);

        branchRepository.save(branch);
    }

    @Cacheable(value = "document_version_diffs", key = "{#documentId, #base, #compare}")
    @Transactional(readOnly = true)
    @Override
    public DiffResponse getVersionDiffs(UUID documentId, UUID base, UUID compare) {
        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var baseVersion = versionRepository
                .findDocumentBranchVersion(base, document.getId())
                .orElseThrow(VersionNotFoundException::new);

        var compareVersion = versionRepository
                .findDocumentBranchVersion(compare, document.getId())
                .orElseThrow(VersionNotFoundException::new);

        if (baseVersion.getStatus().equals(Status.PUBLIC) && compareVersion.getStatus().equals(Status.PUBLIC))
            return diffUtils.buildDiffResponse(baseVersion.getVersionContent(), compareVersion.getVersionContent());

        var userId = UserIdUtils.getUserIdFromContext();

        if (sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return diffUtils.buildDiffResponse(baseVersion.getVersionContent(), compareVersion.getVersionContent());
    }
}
