package com.nipun.system.document;

import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.user.User;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public Document createDocument(Document document) {
        var user = getUserFromContext();

        document.setPublicId(UUID.randomUUID());
        document.setOwner(user);
        document.setContent(new Content());
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        documentRepository.save(document);

        return document;
    }

    public Document getDocument(UUID documentId) {

        var user = getUserFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, user.getId())
                .orElse(null);

        if(document == null)
            throw new DocumentNotFoundException();

        return document;
    }

    public List<Document> getAllDocuments() {

        var user = getUserFromContext();

        return documentRepository.findAllByOwnerId(user.getId());
    }

    public Document updateTitle(UUID documentId, Document documentRequest) {

        var user = getUserFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, user.getId())
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        document = document.updateTitle(documentRequest);

        documentRepository.save(document);

        return document;
    }

    public void deleteDocument(UUID documentId) {

        var user = getUserFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, user.getId())
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        documentRepository.deleteById(document.getId());
    }

    public Content updateContent(UUID documentId, Content content) {
        var user = getUserFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, user.getId())
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        System.out.println(content.getContent());

        document.getContent().setContent(content.getContent());

        documentRepository.save(document);

        return document.getContent();
    }

    public Content getContent(UUID documentId) {
        var user = getUserFromContext();
        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, user.getId())
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        return document.getContent();
    }

    private User getUserFromContext() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) authentication.getPrincipal();

        return userRepository.findById(userId).orElseThrow();
    }
}
