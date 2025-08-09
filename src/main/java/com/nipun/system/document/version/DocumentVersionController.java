package com.nipun.system.document.version;

import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.version.DiffResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;
    private final DocumentVersionMapper documentVersionMapper;

    @GetMapping("/{id}/versions")
    public ResponseEntity<PaginatedData> getAllDocumentVersions(
            @PathVariable(name = "id") UUID documentId,
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var versions = documentVersionService
                .getAllDocumentVersions(documentId, pageNumber, pageSize);

        var documentDtos = versions.getContent().stream().map(documentVersionMapper::toDto).toList();

        return ResponseEntity.ok(
                new PaginatedData(
                        documentDtos,
                        pageNumber,
                        pageSize,
                        versions.getTotalPages(),
                        versions.getTotalElements(),
                        versions.hasNext(),
                        versions.hasPrevious()
                )
        );
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    public ResponseEntity<ContentDto> getDocumentVersionContent(
            @PathVariable(name = "id") UUID documentId,
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        var versionContent = documentVersionService.getVersionContent(versionNumber, documentId);

        return ResponseEntity.ok(
                new ContentDto(versionContent.getContent()));
    }

    @GetMapping("/{id}/versions/diffs")
    public ResponseEntity<DiffResponse> getVersionDiffs(
            @PathVariable(name = "id") UUID documentId,
            @RequestParam(name = "base-version") UUID base,
            @RequestParam(name = "compare-version") UUID compare
    ) {
        var diffs = documentVersionService.getVersionDiffs(documentId, base, compare);
        return ResponseEntity.ok(new DiffResponse(diffs));
    }

    @PostMapping("/{id}/versions/restore")
    public ResponseEntity<Void> restoreToPreviousVersion(
            @PathVariable(name = "id") UUID documentId
    ) {
        documentVersionService.restoreToPreviousVersion(documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/versions/restore/{versionNumber}")
    public ResponseEntity<Void> restoreToDocumentSpecificVersion(
            @PathVariable(name = "id") UUID documentId,
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        documentVersionService.restoreToDocumentSpecificVersion(versionNumber, documentId);

        return ResponseEntity.noContent().build();
    }
}
