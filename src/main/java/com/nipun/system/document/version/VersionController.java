package com.nipun.system.document.version;

import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.diff.dtos.DiffResponse;
import com.nipun.system.document.version.dtos.CreateVersionRequest;
import com.nipun.system.document.version.dtos.VersionResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
@Tag(name = "Document Versions", description = "Manage document versions in the system")
public class VersionController {

    private final VersionService versionService;


    @PostMapping("/{documentId}/branches/{branchId}/versions")
    @Operation(summary = "Create version", description = "Creates a version from specific document branch")
    public ResponseEntity<VersionResponse> createNewVersion(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId,
            @Valid @RequestBody
            CreateVersionRequest request
    ) {
        var newVersion = versionService.createNewVersion(documentId, branchId, request.getName(), request.getStatus());
        return ResponseEntity.ok(newVersion);
    }

    @GetMapping("/{documentId}/versions")
    @Operation(summary = "Get all document versions", description = "Get all document versions by document ID")
    public ResponseEntity<PaginatedData> getAllDocumentVersions(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        var paginatedVersions = versionService
                .getAllDocumentVersions(documentId, pageNumber, pageSize);
        return ResponseEntity.ok(paginatedVersions);
    }

    @GetMapping("/{documentId}/versions/{versionId}")
    @Operation(summary = "Get document version content", description = "Get document version content by document ID and version ID")
    public ResponseEntity<ContentResponse> getDocumentVersionContent(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @Parameter(description = "Document version ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            @PathVariable(name = "versionId")
            UUID versionId
    ) {
        var versionContentDto = versionService.getVersionContent(documentId, versionId);
        return ResponseEntity.ok(versionContentDto);
    }

    @GetMapping("/{documentId}/versions/diffs")
    @Operation(summary = "Compare Diffs", description = "Compare document version diffs")
    public ResponseEntity<DiffResponse> getVersionDiffs(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestParam(name = "base-version")
            @Parameter(description = "Base version ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID base,
            @RequestParam(name = "compare-version")
            @Parameter(description = "Comparing version ID", example = "be0ff390-f94a-42d1-922a-893feae4aa0a")
            UUID compare
    ) {
        var diffResponseDto = versionService.getVersionDiffs(documentId, base, compare);
        return ResponseEntity.ok(diffResponseDto);
    }

    @PostMapping("/{documentId}/branches/{branchId}/merge/versions/{versionId}")
    @Operation(summary = "Restore to specific", description = "Restore document to the specific version")
    public ResponseEntity<Void> mergeVersionToBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID branchId,
            @PathVariable(name = "versionId")
            @Parameter(description = "Document version ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID versionId
    ) {
        versionService.mergeVersionToBranch(documentId, branchId, versionId);
        return ResponseEntity.noContent().build();
    }
}
