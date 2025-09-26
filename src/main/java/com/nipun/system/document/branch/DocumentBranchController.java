package com.nipun.system.document.branch;

import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.UpdateContentRequest;
import com.nipun.system.document.dtos.branch.CreateBranchRequest;
import com.nipun.system.document.dtos.branch.DocumentBranchDto;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.document.exceptions.BranchTitleAlreadyExistsException;
import com.nipun.system.document.exceptions.DocumentBranchNotFoundException;
import com.nipun.system.shared.dtos.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
@Tag(name = "Document Branches", description = "Manage document branches in the system")
public class DocumentBranchController {

    private final DocumentBranchService documentBranchService;

    @PostMapping("/{documentId}/branches/versions/{versionId}")
    @Operation(summary = "Create branch", description = "Creates new document branch in the system")
    public ResponseEntity<DocumentBranchDto> createBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "versionId")
            @Parameter(description = "Document version ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID versionId,
            @RequestBody CreateBranchRequest request
    ) {
        var branchDto = documentBranchService.createBranch(documentId, versionId, request.getBranchName());
        return ResponseEntity.ok(branchDto);
    }

    @GetMapping("/{documentId}/branches/{branchId}")
    @Operation(summary = "Get content", description = "Get document branch content")
    public ResponseEntity<ContentDto> getBranchContent(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        var branchContentDto = documentBranchService.getBranchContent(documentId, branchId);
        return ResponseEntity.ok(branchContentDto);
    }

    @PutMapping("/{documentId}/branches/{branchId}")
    @Operation(summary = "Update branch content", description = "Update document branch content")
    public ResponseEntity<ContentDto> updateBranchContent(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId,
            @RequestBody UpdateContentRequest request
    ) {
        var content = documentBranchService.updateBranchContent(documentId, branchId, request.getContent());
        return ResponseEntity.ok(new ContentDto(content.getContent()));
    }

    @GetMapping("/{documentId}/branches")
    @Operation(summary = "Get all branches", description = "Get all document branches")
    public ResponseEntity<PaginatedData> getAllBranches(
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
        var paginatedBranches = documentBranchService.getAllBranches(documentId, pageNumber, pageSize);
        return ResponseEntity.ok(paginatedBranches);
    }

    @GetMapping("/{documentId}/branches/{branchId}/versions")
    @Operation(summary = "Get all branch versions", description = "Get all document branch versions")
    public ResponseEntity<PaginatedData> getAllBranchVersions(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId,
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        var paginatedVersionDtoList = documentBranchService
                .getAllBranchVersions(documentId, branchId, pageNumber, pageSize);
        return ResponseEntity.ok(paginatedVersionDtoList);
    }

    @DeleteMapping("/{documentId}/branches/{branchId}")
    @Operation(summary = "Delete branch", description = "Delete branch from document")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        documentBranchService.deleteBranch(documentId, branchId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentId}/branches/{branchId}/merge")
    @Operation(summary = "Merge to main", description = "Merge current branch to main branch")
    public ResponseEntity<Void> mergeToMainBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        documentBranchService.mergeToMainBranch(documentId, branchId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{documentId}/branches/{branchId}/merge/{mergeBranchId}")
    @Operation(summary = "Merge branch", description = "Merge two specific branches")
    public ResponseEntity<Void> mergeToMainBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId,
            @PathVariable(name = "mergeBranchId")
            @Parameter(description = "Merge branch id", example = "3a5c4c09-2a79-4db4-85b1-8d7176125429")
            UUID mergeBranchId
    ) {
        documentBranchService.mergeSpecificBranches(documentId, branchId, mergeBranchId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(DocumentBranchNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBranchNotFoundException(
            DocumentBranchNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(BranchTitleAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBranchTitleAlreadyExistsException(
            BranchTitleAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
