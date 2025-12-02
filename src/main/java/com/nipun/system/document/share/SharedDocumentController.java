package com.nipun.system.document.share;

import com.nipun.system.document.share.dtos.ShareDocumentRequest;
import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
@Tag(name = "Share Document", description = "Manage document permissions")
public class SharedDocumentController {

    private final SharedDocumentService sharedDocumentService;

    @RateLimiter(name = "globalLimiter")
    @PostMapping("/{documentId}/share")
    @Operation(summary = "Share document", description = "Share document among user")
    public ResponseEntity<SharedDocumentResponse> shareDocument(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestBody
            @Valid
            ShareDocumentRequest request
    ) {
        var sharedDocumentDto = sharedDocumentService.shareDocument(documentId, request);
        return ResponseEntity.ok(sharedDocumentDto);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/{documentId}/share/users")
    @Operation(summary = "Get all shared users", description = "Get all shared users with the document")
    public ResponseEntity<List<SharedDocumentResponse>> getAllSharedUsers(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        var sharedUsers = sharedDocumentService.getAllSharedUsers(documentId);
        return ResponseEntity.ok(sharedUsers);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/share")
    @Operation(summary = "Get all shared documents", description = "Get all shared documents with the user")
    public ResponseEntity<PaginatedData> getAllSharedDocumentsWithUser(
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        var paginatedDocuments = sharedDocumentService.getAllSharedDocumentsWithUser(pageNumber, pageSize);
        return ResponseEntity.ok(paginatedDocuments);
    }

    @RateLimiter(name = "globalLimiter")
    @PostMapping("/{documentId}/share/remove")
    @Operation(summary = "Remove document access", description = "Remove own document access")
    public ResponseEntity<Void> removeDocumentAccess(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        sharedDocumentService.removeDocumentAccess(documentId);
        return ResponseEntity.noContent().build();
    }

    @RateLimiter(name = "globalLimiter")
    @PostMapping("/{documentId}/share/remove/{sharedUserId}")
    @Operation(summary = "Remove document access", description = "Remove document access as owner")
    public ResponseEntity<Void> removeDocumentAccess(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "sharedUserId")
            @Parameter(description = "Shared user ID", example = "1")
            Long shardUserId
    ) {
        sharedDocumentService.removeDocumentAccess(documentId, shardUserId);
        return ResponseEntity.noContent().build();
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/shared/count/user")
    @Operation(summary = "Shared document count", description = "Get shared document count with user")
    public ResponseEntity<CountResponse> getSharedDocumentWithUserCount() {
        return ResponseEntity.ok(sharedDocumentService.getSharedDocumentWithUserCount());
    }
}
