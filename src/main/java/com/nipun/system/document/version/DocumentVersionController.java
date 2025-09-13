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

    @GetMapping("/{id}/versions")
    public ResponseEntity<PaginatedData> getAllDocumentVersions(
            @PathVariable(name = "id") UUID documentId,
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var paginatedVersions = documentVersionService
                .getAllDocumentVersions(documentId, pageNumber, pageSize);
        return ResponseEntity.ok(paginatedVersions);
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    public ResponseEntity<ContentDto> getDocumentVersionContent(
            @PathVariable(name = "id") UUID documentId,
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        var versionContentDto = documentVersionService.getVersionContent(versionNumber, documentId);
        return ResponseEntity.ok(versionContentDto);
    }

    @GetMapping("/{id}/versions/diffs")
    public ResponseEntity<DiffResponse> getVersionDiffs(
            @PathVariable(name = "id") UUID documentId,
            @RequestParam(name = "base-version") UUID base,
            @RequestParam(name = "compare-version") UUID compare
    ) {
        var diffResponseDto = documentVersionService.getVersionDiffs(documentId, base, compare);
        return ResponseEntity.ok(diffResponseDto);
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
