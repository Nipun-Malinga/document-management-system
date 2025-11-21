package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.CreateDocumentRequest;
import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.base.dtos.UpdateDocumentRequest;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Manage documents in the system")
public class DocumentController {

    private final DocumentService documentService;

    @RateLimiter(name = "globalLimiter")
    @PostMapping
    @Operation(summary = "Create document", description = "Creates new document in the system")
    public ResponseEntity<DocumentResponse> createDocument(
            @RequestBody @Valid CreateDocumentRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var document = documentService.createDocument(request);

        var uri = uriBuilder.path("/documents/{documentId}")
                .buildAndExpand(document.getId()).toUri();

        return ResponseEntity.created(uri).body(document);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/{documentId}")
    @Operation(summary = "Get document", description = "Get document by ID")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        var documentDto = documentService.getDocument(documentId);
        return ResponseEntity.ok(documentDto);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping
    @Operation(summary = "Get all documents", description = "Get all user owned documents")
    public ResponseEntity<PaginatedData> getAllDocuments(
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        var paginatedDocumentDtoList = documentService.getAllDocuments(pageNumber, pageSize);
        return ResponseEntity.ok(paginatedDocumentDtoList);
    }

    @RateLimiter(name = "globalLimiter")
    @PutMapping("/{documentId}")
    @Operation(summary = "Update document title", description = "Updates the document title")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestBody @Valid UpdateDocumentRequest request
    ) {
        var documentDto = documentService.updateDocument(documentId, request);
        return ResponseEntity.ok(documentDto);
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/count")
    @Operation(summary = "Document count", description = "Get user non trashed document count")
    public ResponseEntity<CountResponse> getDocumentCount() {
        return ResponseEntity.ok(documentService.getDocumentCount());
    }

    @RateLimiter(name = "globalLimiter")
    @PostMapping("/{documentId}/favorites/toggle")
    @Operation(summary = "Add to favorites", description = "Add document to favorites")
    public ResponseEntity<DocumentResponse> toggleFavorite(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        return ResponseEntity.ok(documentService.toggleFavorite(documentId));
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/favorites/count")
    @Operation(summary = "Favorite documents", description = "Get all user favorite documents")
    public ResponseEntity<CountResponse> getDocumentFavoriteCount() {
        return ResponseEntity.ok(documentService.getDocumentFavoriteCount());
    }
}
