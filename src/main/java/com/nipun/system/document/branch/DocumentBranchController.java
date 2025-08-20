package com.nipun.system.document.branch;

import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.UpdateContentRequest;
import com.nipun.system.document.dtos.branch.CreateBranchRequest;
import com.nipun.system.document.dtos.branch.DocumentBranchDto;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.exceptions.BranchTitleAlreadyExistsException;
import com.nipun.system.document.exceptions.DocumentBranchNotFoundException;
import com.nipun.system.document.version.DocumentVersionMapper;
import com.nipun.system.shared.dtos.ErrorResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RequestMapping("/documents")
@RestController
public class DocumentBranchController {

    private final DocumentBranchService documentBranchService;
    private final DocumentBranchMapper documentBranchMapper;
    private final DocumentVersionMapper documentVersionMapper;

    @PostMapping("/{documentId}/branches/versions/{versionId}")
    public ResponseEntity<DocumentBranchDto> createBranch(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "versionId") UUID versionId,
            @RequestBody CreateBranchRequest request
            ) {
        var branch = documentBranchService.createBranch(documentId, versionId, request.getBranchName());

        return ResponseEntity.ok(documentBranchMapper.toDto(branch));
    }

    @GetMapping("/{documentId}/branches/{branchId}")
    public ResponseEntity<ContentDto> getBranchContent(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "branchId") UUID branchId
    ) {
        var branchContent = documentBranchService.getBranchContent(documentId, branchId);

        return ResponseEntity.ok(new ContentDto(branchContent.getContent()));
    }

    @PutMapping("/{documentId}/branches/{branchId}")
    public ResponseEntity<ContentDto> updateBranchContent(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "branchId") UUID branchId,
            @RequestBody UpdateContentRequest request
    ) {
        var content = documentBranchService.updateBranchContent(documentId, branchId, request.getContent());

        return ResponseEntity.ok(new ContentDto(content.getContent()));
    }

    @GetMapping("/{documentId}/branches")
    public ResponseEntity<PaginatedData> getAllBranches(
            @PathVariable(name = "documentId") UUID documentId,
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var branches = documentBranchService.getAllBranches(documentId, pageNumber, pageSize);

        var branchDtos = branches.getContent().stream().map(documentBranchMapper::toDto).toList();

        return ResponseEntity.ok(new PaginatedData(
                branchDtos,
                branches.getNumber(),
                branches.getSize(),
                branches.getTotalPages(),
                branches.getTotalElements(),
                branches.hasNext(),
                branches.hasPrevious()
        ));
    }

    @GetMapping("/{documentId}/branches/{branchId}/versions")
    public ResponseEntity<PaginatedData> getAllBranchVersions(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "branchId") UUID branchId,
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var versions = documentBranchService
                .getAllBranchVersions(documentId, branchId, pageNumber, pageSize);

        var versionDtos = versions.getContent().stream().map(documentVersionMapper::toDto).toList();

        return ResponseEntity.ok(new PaginatedData(
                versionDtos,
                versions.getNumber(),
                versions.getSize(),
                versions.getTotalPages(),
                versions.getTotalElements(),
                versions.hasNext(),
                versions.hasPrevious()
        ));
    }

    @DeleteMapping("/{documentId}/branches/{branchId}")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "branchId") UUID branchId
    ) {
        documentBranchService.deleteBranch(documentId, branchId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentId}/branches/{branchId}/merge")
    public ResponseEntity<Void> mergeToMainBranch(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "branchId") UUID branchId
    ) throws PatchFailedException {
        documentBranchService.mergeToMainBranch(documentId, branchId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{documentId}/branches/{branchId}/merge/{mergeBranchId}")
    public ResponseEntity<Void> mergeToMainBranch(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "mergeBranchId") UUID mergeBranchId
    ) throws PatchFailedException {
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
