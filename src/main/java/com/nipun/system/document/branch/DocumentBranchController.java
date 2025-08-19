package com.nipun.system.document.branch;

import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.UpdateContentRequest;
import com.nipun.system.document.dtos.branch.CreateBranchRequest;
import com.nipun.system.document.dtos.branch.DocumentBranchDto;
import com.nipun.system.document.exceptions.BranchTitleAlreadyExistsException;
import com.nipun.system.document.exceptions.DocumentBranchNotFoundException;
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

    @PostMapping("/{documentId}/branches/versions/{versionId}")
    public ResponseEntity<DocumentBranchDto> createBranch(
            @PathVariable(name = "documentId") UUID documentId,
            @PathVariable(name = "versionId") UUID versionId,
            @RequestBody CreateBranchRequest request
            ) {
        var branch = documentBranchService.createBranch(documentId, versionId, request.getBranchName());

        return ResponseEntity.ok(documentBranchMapper.toDto(branch));
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
