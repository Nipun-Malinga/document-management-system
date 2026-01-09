package com.nipun.system.document.branch;

import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.base.dtos.UpdateContentRequest;
import com.nipun.system.document.branch.dtos.BranchResponse;
import com.nipun.system.document.branch.dtos.CreateBranchRequest;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.branch.exceptions.BranchTitleAlreadyExistsException;
import com.nipun.system.document.diff.dtos.DiffResponse;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.ErrorResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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
@RequestMapping("/api/documents")
@Tag(name = "Document Branches", description = "Manage document branches in the system")
public class BranchController {

    private final BranchService branchService;

    @RateLimiter(name = "globalLimiter")
    @PostMapping("/{documentId}/branches/{branchId}")
    @Operation(summary = "Create branch", description = "Creates new document branch in the system")
    public ResponseEntity<BranchResponse> createBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID branchId,
            @RequestBody CreateBranchRequest request
    ) {
        var branchDto = branchService.createBranch(documentId, branchId, request.getBranchName());
        return ResponseEntity.ok(branchDto);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/{documentId}/branches/{branchId}/content")
    @Operation(summary = "Get content", description = "Get document branch content")
    public ResponseEntity<ContentResponse> getBranchContent(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        var branchContentDto = branchService.getBranchContent(documentId, branchId);
        return ResponseEntity.ok(branchContentDto);
    }

    @PutMapping("/{documentId}/branches/{branchId}/content")
    @Operation(summary = "Update branch content", description = "Update document branch content")
    public ResponseEntity<ContentResponse> updateBranchContent(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId,
            @RequestBody UpdateContentRequest request
    ) {
        var content = branchService.updateBranchContent(documentId, branchId, request.getContent());
        return ResponseEntity.ok(new ContentResponse(content.getContent()));
    }

    @RateLimiter(name = "globalLimiter")
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
        var paginatedBranches = branchService.getAllBranches(documentId, pageNumber, pageSize);
        return ResponseEntity.ok(paginatedBranches);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/{documentId}/branches/diffs")
    @Operation(summary = "Compare branch Diffs", description = "Compare document branch diffs")
    public ResponseEntity<DiffResponse> getVersionDiffs(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestParam(name = "base-version")
            @Parameter(description = "Base Branch ID", example = "d361bae1-01ee-4392-811c-57b9593c2460")
            UUID base,
            @RequestParam(name = "compare-version")
            @Parameter(description = "Comparing Branch ID", example = "be0ff390-f94a-42d1-922a-893feae4aa0a")
            UUID compare
    ) {
        var diffResponseDto = branchService.getBranchDiffs(documentId, base, compare);
        return ResponseEntity.ok(diffResponseDto);
    }

    @RateLimiter(name = "globalLimiter")
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
        branchService.mergeBranches(documentId, branchId, mergeBranchId);
        return ResponseEntity.ok().build();
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/{documentId}/branches/count")
    @Operation(summary = "Document branches count", description = "Get user document branches count")
    public ResponseEntity<CountResponse> getDocumentCount(
            @PathVariable(name = "documentId") UUID branchId
    ) {
        return ResponseEntity.ok(branchService.getAllBranchCount(branchId));
    }

    @ExceptionHandler(BranchNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBranchNotFoundException(
            BranchNotFoundException exception
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
