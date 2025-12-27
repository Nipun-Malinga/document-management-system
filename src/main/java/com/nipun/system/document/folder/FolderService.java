package com.nipun.system.document.folder;

import com.nipun.system.document.base.DocumentMapper;
import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.folder.dtos.FolderRequest;
import com.nipun.system.document.folder.dtos.FolderResponse;
import com.nipun.system.document.folder.exceptions.FolderNotFoundException;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class FolderService {
    private final FolderRepository folderRepository;
    private final FolderMapper folderMapper;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    public FolderResponse createFolder(FolderRequest request) {
        var folder = folderMapper.toEntity(request);

        var userId = UserIdUtils.getUserIdFromContext();
        var user = userRepository.findById(userId).orElseThrow(FolderNotFoundException::new);

        folder.setPublicId(UUID.randomUUID());
        folder.setOwner(user);
        folder.setCreatedAt(LocalDateTime.now());

        return folderMapper.toDto(folderRepository.save(folder));
    }

    public FolderResponse createSubFolder(UUID parentFolderPublicId, FolderRequest request) {
        var userId = UserIdUtils.getUserIdFromContext();

        var parentFolder = folderRepository.findByOwnerIdAndParentFolderPublicId(userId, parentFolderPublicId).orElseThrow(FolderNotFoundException::new);

        var folder = folderMapper.toEntity(request);
        parentFolder.addSubFolder(folder);
        folder.setCreatedAt(LocalDateTime.now());

        return folderMapper.toDto(folderRepository.save(folder));
    }

    public FolderResponse updateFolder(UUID folderPublicId, FolderRequest request) {
        var userId = UserIdUtils.getUserIdFromContext();

        var folder = folderRepository.findByOwnerIdAndParentFolderPublicId(userId, folderPublicId).orElseThrow(FolderNotFoundException::new);

        folder.setName(request.getName());

        return folderMapper.toDto(folderRepository.save(folder));
    }

    public List<DocumentResponse> getAllDocumentList(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var documents = documentRepository.findByFolderPublicIdAndOwnerId(documentId, userId);

        return documents.stream().map(documentMapper::toDto).toList();
    }

    public DocumentResponse addDocumentToFolder(UUID folderId, UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var documents = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElseThrow(DocumentNotFoundException::new);

        var folder = folderRepository.findByPublicId(folderId).orElseThrow(FolderNotFoundException::new);

        documents.setFolder(folder);

        return documentMapper.toDto(documentRepository.save(documents));
    }
}
