package com.nipun.system.document.branch;

import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.branch.dtos.BranchResponse;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.branch.exceptions.BranchTitleAlreadyExistsException;
import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.share.SharedDocumentAuthService;
import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Log4j2
@AllArgsConstructor
@Service
public class BranchServiceImpl implements BranchService {


    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    private final BranchMapper branchMapper;

    private final DiffService diffService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    private final BranchFactory branchFactory;

    @Transactional
    @Override
    public BranchResponse createBranch(UUID documentId, UUID branchId, String branchName) {
        var userId = UserIdUtils.getUserIdFromContext();
        var user = userRepository.findById(userId).orElseThrow();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        if (branchRepository.existsByBranchNameAndDocumentId(branchName, document.getId()))
            throw new BranchTitleAlreadyExistsException();

        var baseBranch = branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .orElseThrow(BranchNotFoundException::new);

        var newBranch = branchFactory.createNewBranch(document, branchName, baseBranch.getBranchContent(), user);

        return branchMapper.toDto(branchRepository.save(newBranch));
    }

    @Override
    public PaginatedData getAllBranches(UUID documentId, int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (sharedDocumentAuthService.isUnauthorizedUser(userId, document))
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

    @Cacheable(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    @Override
    public ContentResponse getBranchContent(UUID documentId, UUID branchId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var branch = branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .orElseThrow(BranchNotFoundException::new);


        if (sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return new ContentResponse(branch.getBranchContent());
    }

    @CachePut(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    @Override
    public ContentResponse updateBranchContent(UUID documentId, UUID branchId, String content) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var branch = branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .orElseThrow(BranchNotFoundException::new);

        var patchedDocument = diffService.patchDocument(branch.getBranchContent(), content);

        branch.setBranchContent(patchedDocument);

        branchRepository.save(branch);

        return new ContentResponse(branch.getBranchContent());
    }

    @CacheEvict(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    @Override
    public void deleteBranch(UUID documentId, UUID branchId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(BranchNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .ifPresent(branchRepository::delete);
    }

    @CacheEvict(value = "document_branch_contents", key = "#documentId + ':' + #branchId")
    @Override
    public void mergeBranches(UUID documentId, UUID branchId, UUID mergeBranchId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(BranchNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var baseBranch = branchRepository
                .findByPublicIdAndDocumentId(branchId, document.getId())
                .orElseThrow(BranchNotFoundException::new);

        var mergeBranch = branchRepository
                .findByPublicIdAndDocumentId(mergeBranchId, document.getId())
                .orElseThrow(BranchNotFoundException::new);

        var patchedDocument = diffService
                .patchDocument(baseBranch.getBranchContent(), mergeBranch.getBranchContent());

        baseBranch.setBranchContent(patchedDocument);

        branchRepository.save(baseBranch);
    }
}
