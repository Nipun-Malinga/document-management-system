package com.nipun.system.document.version;

import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.document.dtos.version.DiffResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
@Tag(name = "Document Versions", description = "Manage document versions in the system")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get all document versions", description = "Get all document versions by document ID")
    public ResponseEntity<PaginatedData> getAllDocumentVersions(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        var paginatedVersions = documentVersionService
                .getAllDocumentVersions(documentId, pageNumber, pageSize);
        return ResponseEntity.ok(paginatedVersions);
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    @Operation(summary = "Get document version content", description = "Get document version content by document ID and version ID")
    public ResponseEntity<ContentResponse> getDocumentVersionContent(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @Parameter(description = "Document version ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        var versionContentDto = documentVersionService.getVersionContent(versionNumber, documentId);
        return ResponseEntity.ok(versionContentDto);
    }

    @GetMapping("/{id}/versions/diffs")
    @Operation(summary = "Compare Diffs", description = "Compare document version diffs")
    public ResponseEntity<DiffResponse> getVersionDiffs(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestParam(name = "base-version")
            @Parameter(description = "Base version ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID base,
            @RequestParam(name = "compare-version")
            @Parameter(description = "Comparing version ID", example = "be0ff390-f94a-42d1-922a-893feae4aa0a")
            UUID compare
    ) {
        var diffResponseDto = documentVersionService.getVersionDiffs(documentId, base, compare);
        return ResponseEntity.ok(diffResponseDto);
    }

    @PostMapping("/{id}/versions/restore")
    @Operation(summary = "Restore", description = "Restore document to the previous version")
    public ResponseEntity<Void> restoreToPreviousVersion(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        documentVersionService.restoreToPreviousVersion(documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/versions/restore/{versionNumber}")
    @Operation(summary = "Restore to specific", description = "Restore document to the specific version")
    public ResponseEntity<Void> restoreToDocumentSpecificVersion(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "versionNumber")
            @Parameter(description = "Document version ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID versionNumber
    ) {
        documentVersionService.restoreToDocumentSpecificVersion(versionNumber, documentId);
        return ResponseEntity.noContent().build();
    }
}
