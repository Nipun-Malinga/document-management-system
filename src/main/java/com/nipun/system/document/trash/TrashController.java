package com.nipun.system.document.trash;

import com.nipun.system.document.trash.exceptions.TrashNotFoundException;
import com.nipun.system.document.trash.exceptions.UnauthorizedBranchDeletionException;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.ErrorResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/documents/trash")
@Tag(name = "Document Trash", description = "Manage trashed documents in the system")
public class TrashController {

    private final TrashService trashService;

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Add to trash", description = "Add entier document to trash")
    public ResponseEntity<Void> addDocumentToTrash(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        trashService.addDocumentToTrash(documentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{documentId}/branches/{branchId}")
    @Operation(summary = "Add to trash", description = "Add document branch to trash")
    public ResponseEntity<Void> addBranchToTrash(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        trashService.addBranchToTrash(documentId, branchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "All trashed documents", description = "Get all user trashed documents")
    public ResponseEntity<PaginatedData> getAllTrashedDocuments(
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        return ResponseEntity.ok(trashService.getAllTrashedDocuments(pageNumber, pageSize));
    }

    @GetMapping("/branches")
    @Operation(summary = "All trashed branches", description = "Get all user trashed branches")
    public ResponseEntity<PaginatedData> getAllTrashedBranches(
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        return ResponseEntity.ok(trashService.getAllTrashedBranches(pageNumber, pageSize));
    }

    @PostMapping("/restore/{documentId}")
    @Operation(summary = "Restore document", description = "Restore trashed document")
    public ResponseEntity<Void> restoreDocument(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        trashService.restoreDocument(documentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore/{documentId}/branches/{branchId}")
    @Operation(summary = "Restore branch", description = "Restore trashed branch")
    public ResponseEntity<Void> restoreBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        trashService.restoreBranch(documentId, branchId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{documentId}")
    @Operation(summary = "Delete document", description = "Deletes document from the system")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        trashService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{documentId}/branches/{branchId}")
    @Operation(summary = "Delete branch", description = "Delete branch from document")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        trashService.deleteBranch(documentId, branchId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/count")
    @Operation(summary = "Trashed document count", description = "Get trashed document count")
    public ResponseEntity<CountResponse> getDocumentCount() {
        return ResponseEntity.ok(trashService.getTrashedDocumentCount());
    }

    @GetMapping("/{documentId}/branches/count")
    @Operation(summary = "Trashed branches count", description = "Get trashed branches count")
    public ResponseEntity<CountResponse> getBranchesCount(
            @PathVariable(name = "documentId") UUID documentId
    ) {
        return ResponseEntity.ok(trashService.getTrashedBranchesCount(documentId));
    }

    @ExceptionHandler(UnauthorizedBranchDeletionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedBranchDeletionException(
            UnauthorizedBranchDeletionException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(TrashNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrashNotFoundException(
            TrashNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
