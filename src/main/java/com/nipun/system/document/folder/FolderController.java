package com.nipun.system.document.folder;

import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.folder.dtos.FolderRequest;
import com.nipun.system.document.folder.dtos.FolderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/documents")
public class FolderController {
    public final FolderService folderService;

    @PostMapping("/folders")
    public ResponseEntity<FolderResponse> createSubFolder(
            @RequestBody @Valid FolderRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var folderResponse = folderService.createFolder(request);
        var uri = uriBuilder.path("/documents/folders/{folderId}")
                .buildAndExpand(folderResponse.getId())
                .toUri();
        return ResponseEntity.created(uri).body(folderResponse);
    }

    @PostMapping("/folders/{folderId}")
    public ResponseEntity<FolderResponse> createSubFolder(
            @PathVariable(name = "folderId") UUID parentFolderPublicId,
            @RequestBody @Valid FolderRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var folderResponse = folderService.createSubFolder(parentFolderPublicId, request);
        var uri = uriBuilder.path("/documents/folders/${folderId}").buildAndExpand().toUri();
        return ResponseEntity.created(uri).body(folderResponse);
    }

    @PatchMapping("/folders/{folderId}")
    public ResponseEntity<FolderResponse> updateFolder(
            @PathVariable(name = "folderId") UUID parentFolderPublicId,
            @RequestBody @Valid FolderRequest request
    ) {
        var folderResponse = folderService.updateFolder(parentFolderPublicId, request);
        return ResponseEntity.ok(folderResponse);
    }

    @GetMapping("/folders/{folderId}")
    public ResponseEntity<List<DocumentResponse>> getAllDocumentsByFolder(
            @PathVariable(name = "folderId") UUID folderPublicId
    ) {
        var documentDtoList = folderService.getAllDocumentList(folderPublicId);
        return ResponseEntity.ok(documentDtoList);
    }

    @PostMapping("/{documentId}/folders/{folderId}")
    public ResponseEntity<DocumentResponse> addDocumentToFolder(
            @PathVariable(name = "folderId") UUID folderId,
            @PathVariable(name = "documentId") UUID documentId
    ) {
        var documentDto = folderService.addDocumentToFolder(folderId, documentId);
        return ResponseEntity.ok(documentDto);
    }
}
