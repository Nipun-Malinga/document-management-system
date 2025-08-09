package com.nipun.system.document.share;

import com.nipun.system.document.ContentMapper;
import com.nipun.system.document.DocumentMapper;
import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.UpdateContentRequest;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.share.ShareDocumentRequest;
import com.nipun.system.document.dtos.share.SharedDocumentDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
public class SharedDocumentController {

    private final SharedDocumentService sharedDocumentService;
    private final DocumentMapper documentMapper;
    private final ContentMapper contentMapper;
    private final SharedDocumentMapper sharedDocumentMapper;

    @PostMapping("/{id}/share/{userId}")
    public ResponseEntity<SharedDocumentDto> shareDocument(
            @PathVariable(name = "id") UUID documentId,
            @PathVariable(name = "userId") Long shareUserId,
            @RequestBody @Valid ShareDocumentRequest request
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
    public ResponseEntity<PaginatedData> getAllSharedDocumentsWithUser(
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
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
    public ResponseEntity<ContentDto> accessSharedDocument(
            @PathVariable(name = "id") UUID documentId
    ) {
        return ResponseEntity
                .ok(contentMapper.toDto(
                        sharedDocumentService.accessSharedDocument(documentId)
                ));
    }

    @PatchMapping("/share/{id}")
    public ResponseEntity<ContentDto> updateSharedDocument(
            @PathVariable(name = "id") UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) {
        var content = sharedDocumentService.updateSharedDocument(documentId, contentMapper.toEntity(request));

        return ResponseEntity.ok(contentMapper.toDto(content));
    }
}
