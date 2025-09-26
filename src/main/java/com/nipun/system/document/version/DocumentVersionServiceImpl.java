package com.nipun.system.document.version;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.version.DiffResponse;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.DocumentVersionNotFoundException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.share.SharedDocumentAuthService;
import com.nipun.system.shared.utils.UserIdUtils;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentVersionServiceImpl implements DocumentVersionService{

    /*
        TODO:
         Combine All THE CONTENT TABLES INTO ONE.
         CREATE A SEPARATE BRANCH FOR MAIN DOCUMENT AND
          REPLACE EXISTING VERSION RESTORE FUNCTIONS.
    */

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    private final DocumentVersionMapper documentVersionMapper;

    private final DiffService diffService;
    private final SharedDocumentAuthService sharedDocumentAuthService;


    @Override
    public PaginatedData getAllDocumentVersions(UUID documentId, int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var versions =  documentVersionRepository
                .findAllByDocumentId(document.getId(), pageRequest);

        var documentDtoList = versions.getContent()
                .stream()
                .map(documentVersionMapper::toDto)
                .toList();

        return new PaginatedData(
                documentDtoList,
                pageNumber,
                size,
                versions.getTotalPages(),
                versions.getTotalElements(),
                versions.hasNext(),
                versions.hasPrevious()
        );
    }

    @Cacheable(value = "document_version_contents", key = "#documentId + ':' + #versionNumber")
    @Override
    public ContentDto getVersionContent(UUID versionNumber, UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var documentVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(versionNumber, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = documentVersion.getDocument();

        if(sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return new ContentDto(documentVersion.getVersionContent());
    }

    @Cacheable(value = "document_version_diffs", key = "#documentId + ':' + #base + ':' + #compare")
    @Override
    public DiffResponse getVersionDiffs(UUID documentId, UUID base, UUID compare) {

        var userId = UserIdUtils.getUserIdFromContext();

        var baseVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(base, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = baseVersion.getDocument();

        if(sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        var comparedWithVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(compare, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var diffRowDtoList = diffService.getVersionDiffs(baseVersion.getVersionContent(), comparedWithVersion.getVersionContent());

        return  new DiffResponse(Map.of("diffs", diffRowDtoList));
    }

    @Transactional
    @Override
    public void restoreToPreviousVersion(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var version = documentVersionRepository
                .findFirstByDocumentIdOrderByTimestampDesc(document.getId())
                .orElseThrow(DocumentVersionNotFoundException::new);

        document.setDocumentContent(version.getVersionContent());

        documentRepository.save(document);

        documentVersionRepository.delete(version);
    }

    @Transactional
    @Override
    public void restoreToDocumentSpecificVersion(UUID versionNumber, UUID documentId) {
        var version = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(versionNumber, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = documentRepository
                .findById(version.getDocument().getId())
                .orElseThrow(DocumentNotFoundException::new);

        var userId = UserIdUtils.getUserIdFromContext();

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        document.setDocumentContent(version.getVersionContent());

        documentRepository.save(document);

        documentVersionRepository.rollbackMainDocToPreviousVersion(document.getId(), version.getTimestamp());
    }
}
