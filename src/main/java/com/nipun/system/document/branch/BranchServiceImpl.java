package com.nipun.system.document.branch;

import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.branch.exceptions.BranchTitleAlreadyExistsException;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.diff.exceptions.PatchFailedException;
import com.nipun.system.document.branch.dtos.BranchResponse;
import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.version.exceptions.VersionNotFoundException;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.document.share.SharedDocumentAuthService;
import com.nipun.system.document.version.*;
import com.nipun.system.shared.utils.UserIdUtils;
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
public class BranchServiceImpl implements BranchService {

    private final VersionRepository versionRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    private final BranchMapper branchMapper;
    private final VersionMapper versionMapper;

    private final DiffService diffService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    private final BranchFactory branchFactory;
    private final VersionFactory versionFactory;

    @Transactional
    @Override
    public BranchResponse createBranch(UUID documentId, UUID versionId, String branchName) {

        var userId = UserIdUtils.getUserIdFromContext();
        var user = userRepository.findById(userId).orElseThrow();

        var version = versionRepository
                .findByVersionNumberAndDocumentPublicId(versionId, documentId)
                .orElseThrow(VersionNotFoundException::new);

        var versionContent = version.getVersionContent();
        var document = version.getDocument();

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var fetchedBranch = branchRepository.findByBranchName(branchName).orElse(null);

        if(fetchedBranch != null && fetchedBranch.isBranchTitleExistsAlready(branchName))
            throw new BranchTitleAlreadyExistsException();

        var branchContent = new BranchContent();
        branchContent.setContent(versionContent);

        var newBranch = branchFactory.createNewBranch(version, branchName, branchContent, document);
        var newVersion = versionFactory.createNewVersion(document, user, versionContent, newBranch);

        versionRepository.save(newVersion);

        return branchMapper.toDto(newBranch);
    }

    @Cacheable(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    @Override
    public ContentResponse getBranchContent(UUID documentId, UUID branchId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var branch = branchRepository
                .findByPublicIdAndVersionDocumentPublicId(branchId, documentId)
                .orElseThrow(BranchNotFoundException::new);

        var document = branch.getDocument();

        if(sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return new ContentResponse(branch.getBranchContent());
    }

    @CachePut(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    @Override
    public ContentResponse updateBranchContent(
            UUID documentId,
            UUID branchId,
            String content
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var branch = branchRepository
                .findByPublicIdAndVersionDocumentPublicId(branchId, documentId)
                .orElseThrow(BranchNotFoundException::new);

        var document = branch.getVersion().getDocument();

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        branch.setBranchContent(content);

        var user = userRepository.findById(userId).orElseThrow();

        var version = versionFactory.createNewVersion(document, user, content, branch);

        versionRepository.save(version);

        return new ContentResponse(branch.getBranchContent());
    }

    @Override
    public PaginatedData getAllBranches(
            UUID documentId,
            int pageNumber,
            int size
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var branches = branchRepository
                .findAllByDocumentId(document.getId(), pageRequest);

        var branchDtoList = branches.getContent()
                .stream()
                .map(branchMapper::toDto)
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
    @Override
    public void deleteBranch(
            UUID documentId,
            UUID branchId
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(BranchNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .ifPresent(branchRepository::delete);
    }

    @Override
    public PaginatedData getAllBranchVersions(
            UUID documentId,
            UUID branchId,
            int pageNumber,
            int size
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(BranchNotFoundException::new);

        if(sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var versionList =  versionRepository
                .findAllByDocumentPublicIdAndBranchPublicId(
                        documentId,
                        branchId,
                        pageRequest
                );

        var versionDtoList = versionList.getContent()
                .stream()
                .map(versionMapper::toDto)
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

    @CacheEvict(value = "document_contents", key = "#documentId")
    @Override
    public void mergeToMainBranch(UUID documentId, UUID branchId) throws PatchFailedException {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(BranchNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var branch = branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .orElseThrow(BranchNotFoundException::new);

        document.getContent().setContent(
                diffService.patchDocument(document.getDocumentContent(), branch.getBranchContent())
        );

        documentRepository.save(document);
    }

    @CacheEvict(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    @Override
    public void mergeSpecificBranches(
            UUID documentId, UUID branchId, UUID mergeBranchId
    ) throws PatchFailedException {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(BranchNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var branch = document
                .getBranches()
                .stream()
                .filter(item ->
                        item.getPublicId().equals(branchId))
                .findFirst()
                .orElseThrow(BranchNotFoundException::new);

        var mergeBranch = document
                .getBranches()
                .stream()
                .filter(item ->
                        item.getPublicId().equals(mergeBranchId))
                .findFirst()
                .orElseThrow(BranchNotFoundException::new);

        var patchedContent = diffService
                .patchDocument(branch.getBranchContent(), mergeBranch.getBranchContent());

        branch.setBranchContent(patchedContent);

        documentRepository.save(document);
    }
}
