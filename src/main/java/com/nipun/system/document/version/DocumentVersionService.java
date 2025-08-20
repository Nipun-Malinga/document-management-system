package com.nipun.system.document.version;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.utils.Utils;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.DocumentVersionNotFoundException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentVersionService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public Page<DocumentVersion> getAllDocumentVersions(UUID documentId, int pageNumber, int size) {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentVersionRepository.findAllByDocumentId(document.getId(), pageRequest);
    }

    public DocumentVersionContent getVersionContent(UUID versionNumber, UUID documentId) {
        var userId = Utils.getUserIdFromContext();

        var documentVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(versionNumber, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = documentVersion.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        return documentVersion.getContent();
    }

    public Map<String, Object> getVersionDiffs(UUID documentId, UUID base, UUID compare) {

        var userId = Utils.getUserIdFromContext();

        var baseVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(base, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);

        if(baseVersion.getDocument().isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        var comparedWithVersion = documentVersionRepository
                .findByVersionNumberAndDocumentPublicId(compare, documentId)
                .orElseThrow(DocumentVersionNotFoundException::new);


        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag(_ -> "~")
                .newTag(_ -> "**")
                .build();


        List<DiffRow> rows = generator.generateDiffRows(
                List.of(comparedWithVersion.getContent().getContent().split("\n")),
                List.of(baseVersion.getContent().getContent().split("\n")));

        return  Map.of("diff", rows);
    }

    @Transactional
    public void restoreToPreviousVersion(UUID documentId) {
        var userId = Utils.getUserIdFromContext();

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

        var userId = Utils.getUserIdFromContext();

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        if(document.isReadOnlyUser(userId)) {
            throw new ReadOnlyDocumentException();
        }

        document.getContent().setContent(documentVersion.getContent().getContent());

        documentRepository.save(document);

        documentVersionRepository.deleteDocumentVersionsAfter(document.getId(), documentVersion.getTimestamp());
    }
}
