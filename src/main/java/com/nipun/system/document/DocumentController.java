package com.nipun.system.document;

import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.dtos.*;
import com.nipun.system.document.dtos.common.PaginatedData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(
            @RequestBody @Valid CreateDocumentRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var document = documentService.createDocument(request);

        var uri = uriBuilder.path("/documents/{id}")
                .buildAndExpand(document.getId()).toUri();

        return ResponseEntity.created(uri).body(document);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(
            @PathVariable(name = "id") UUID documentId
    ) {
        var documentDto = documentService.getDocument(documentId);
        return ResponseEntity.ok(documentDto);
    }

    @GetMapping
    public ResponseEntity<PaginatedData> getAllDocuments(
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var paginatedDocumentDtoList = documentService.getAllDocuments(pageNumber, pageSize);
        return ResponseEntity.ok(paginatedDocumentDtoList);
    }

    @PutMapping("/{id}/title")
    public ResponseEntity<DocumentDto> updateDocumentTitle(
            @PathVariable(name = "id") UUID documentId,
            @RequestBody @Valid UpdateTitleRequest request
    ) {
        var documentDto = documentService
                .updateTitle(documentId, request);
        return ResponseEntity.ok(documentDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable(name = "id") UUID documentId
    ) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/content")
    public ResponseEntity<ContentDto> updateDocumentContent(
            @PathVariable(name = "id") UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) throws PatchFailedException {
        var contentDto = documentService.updateContent(documentId, request);

        return ResponseEntity.ok(contentDto);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<ContentDto> getDocumentContent(
        @PathVariable(name = "id") UUID documentId
    ) {
        var contentDto = documentService.getContent(documentId);

        return ResponseEntity.ok(contentDto);
    }
}
