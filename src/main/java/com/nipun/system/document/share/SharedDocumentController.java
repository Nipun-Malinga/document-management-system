package com.nipun.system.document.share;

import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.ContentMapper;
import com.nipun.system.document.DocumentMapper;
import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.UpdateContentRequest;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.share.ShareDocumentRequest;
import com.nipun.system.document.dtos.share.SharedDocumentDto;
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
@Tag(name = "Share Document", description = "Manage document permissions")
public class SharedDocumentController {

    private final SharedDocumentService sharedDocumentService;
    private final DocumentMapper documentMapper;
    private final ContentMapper contentMapper;
    private final SharedDocumentMapper sharedDocumentMapper;

    @PostMapping("/{id}/share/{userId}")
    @Operation(summary = "Share document", description = "Share document among user")
    public ResponseEntity<SharedDocumentDto> shareDocument(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "userId")
            @Parameter(description = "The ID of the user", example = "1")
            Long shareUserId,
            @RequestBody
            @Valid
            ShareDocumentRequest request
    ) {
        var sharedDocument = sharedDocumentService
                .shareDocument(
                        shareUserId,
                        documentId,
                        request.getPermission()
                );
        return ResponseEntity.ok(sharedDocumentMapper.toSharedDocumentDto(sharedDocument));
    }

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

        var documents = sharedDocumentService.getAllSharedDocumentsWithUser(pageNumber, pageSize);

        var documentDtos = documents
                .getContent()
                .stream()
                .map(documentMapper::toDto)
                .toList();
        return ResponseEntity.ok(
                new PaginatedData(
                        documentDtos,
                        pageNumber,
                        pageSize,
                        documents.getTotalPages(),
                        documents.getTotalElements(),
                        documents.hasNext(),
                        documents.hasPrevious()
                ));
    }

    @GetMapping("/{id}/share/access")
    @Operation(summary = "Shared document", description = "Access the shared document content")
    public ResponseEntity<ContentDto> accessSharedDocument(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        return ResponseEntity
                .ok(contentMapper.toDto(
                        sharedDocumentService.accessSharedDocument(documentId)
                ));
    }

    @PatchMapping("/{id}/share/update")
    @Operation(summary = "Update shared document", description = "Update shared document content")
    public ResponseEntity<ContentDto> updateSharedDocument(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) throws PatchFailedException {
        var content = sharedDocumentService.updateSharedDocument(documentId, contentMapper.toEntity(request));
        return ResponseEntity.ok(contentMapper.toDto(content));
    }

    @PostMapping("/{id}/share/remove")
    @Operation(summary = "Remove document access", description = "Remove own document access")
    public ResponseEntity<Void> removeDocumentAccess(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        sharedDocumentService.removeDocumentAccess(documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share/remove/{sharedUserId}")
    @Operation(summary = "Remove document access", description = "Remove document access as owner")
    public ResponseEntity<Void> removeDocumentAccess(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "sharedUserId")
            @Parameter(description = "Shared user ID", example = "1")
            Long shardUserId
    ) {
        sharedDocumentService.removeDocumentAccess(documentId, shardUserId);
        return ResponseEntity.noContent().build();
    }

}
