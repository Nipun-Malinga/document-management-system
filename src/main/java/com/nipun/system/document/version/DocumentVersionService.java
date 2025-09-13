package com.nipun.system.document.version;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.version.DiffResponse;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.DocumentVersionNotFoundException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
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
public class DocumentVersionService {

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

    public PaginatedData getAllDocumentVersions(UUID documentId, int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
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
    public ContentDto getVersionContent(UUID versionNumber, UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var documentVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(versionNumber, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        if(documentVersion.getDocument().isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        return new ContentDto(documentVersion.getVersionContent());
    }

    @Cacheable(value = "document_version_diffs", key = "#documentId + ':' + #base + ':' + #compare")
    public DiffResponse getVersionDiffs(UUID documentId, UUID base, UUID compare) {

        var userId = UserIdUtils.getUserIdFromContext();

        var baseVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(base, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        if(baseVersion.getDocument().isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        var comparedWithVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(compare, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var diffRowDtoList = diffService.getVersionDiffs(
                        baseVersion.getContent().getContent(),
                        comparedWithVersion.getContent().getContent());

        return  new DiffResponse(Map.of("diffs", diffRowDtoList));
    }

    @Transactional
    public void restoreToPreviousVersion(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId))
            throw new ReadOnlyDocumentException();

        var version = documentVersionRepository
                .findFirstByDocumentIdOrderByTimestampDesc(document.getId())
                .orElseThrow(DocumentVersionNotFoundException::new);

        document.getContent().setContent(version.getContent().getContent());

        documentRepository.save(document);

        documentVersionRepository.delete(version);
    }

    @Transactional
    public void restoreToDocumentSpecificVersion(UUID versionNumber, UUID documentId) {
        var documentVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(versionNumber, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = documentRepository
                .findById(documentVersion.getDocument().getId())
                .orElseThrow(DocumentNotFoundException::new);

        var userId = UserIdUtils.getUserIdFromContext();

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId)) {
            throw new ReadOnlyDocumentException();
        }

        document.getContent().setContent(documentVersion.getContent().getContent());

        documentRepository.save(document);

        documentVersionRepository.rollbackMainDocToPreviousVersion(document.getId(), documentVersion.getTimestamp());
    }
}
