package com.nipun.system.document;

import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.dtos.*;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.utils.Utils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final ContentMapper contentMapper;

    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(
            @RequestBody @Valid CreateDocumentRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var document = documentService.createDocument(
                documentMapper.toEntity(request)
        );

        var uri = uriBuilder.path("/documents/{id}")
                .buildAndExpand(document.getPublicId()).toUri();

        return ResponseEntity
                .created(uri)
                .body(documentMapper.toDto(document));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(
            @PathVariable(name = "id") UUID documentId
    ) {
        var document = documentService.getDocument(documentId);
        return ResponseEntity.ok(documentMapper.toDto(document));
    }

    @GetMapping
    public ResponseEntity<PaginatedData> getAllDocuments(
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var documents = documentService.getAllDocuments(pageNumber, pageSize);

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

    @PutMapping("/{id}/title")
    public ResponseEntity<DocumentDto> updateDocumentTitle(
            @PathVariable(name = "id") UUID documentId,
            @RequestBody @Valid UpdateTitleRequest request
    ) {
        var document = documentService
                .updateTitle(documentId, documentMapper.toEntity(request));

        return ResponseEntity.ok(documentMapper.toDto(document));
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
        var content = documentService.updateContent(documentId, contentMapper.toEntity(request));

        return ResponseEntity.ok(contentMapper.toDto(content));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<ContentDto> getDocumentContent(
        @PathVariable(name = "id") UUID documentId
    ) {
        var document = documentService.getContent(documentId);
        return ResponseEntity.ok(contentMapper.toDto(document));
    }

    @SendTo("/document/{documentId}/broadcastStatus")
    @MessageMapping("/document/{documentId}/accept-changes")
    public ContentDto broadcastDocumentCurrentState(
            @DestinationVariable UUID documentId,
            @Payload BroadcastDocumentStatusDto statusDto,
            Principal principal
    ) {
        if(documentService.isAuthorizedUser(Utils.getUserIdFromPrincipal(principal), documentId))
            return new ContentDto(statusDto.getContent());

        throw new UnauthorizedDocumentException();
    }

}
