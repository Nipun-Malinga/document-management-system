package com.nipun.system.document;

import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    /*
        NOTE: Under Development
        TODO: Implement spring security to fetch user id through JWT token.
    */

    public Document createDocument(Document document) {

        /*
            FIXME: Fix the default user adding after implementing spring security.
        */

        var user = userRepository.findById(1L).orElse(null);

        if (user == null)
            throw new UserNotFoundException();

        document.setPublicId(UUID.randomUUID());
        document.setOwner(user);
        document.setContent(new Content());
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        documentRepository.save(document);
        return document;
    }

    public Document getDocument(UUID documentId) {

        /*
            FIXME: Fetch the user id from the JWT
        */

        Long userId = 1L;

        var user = userRepository.findById(userId).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        var document = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElse(null);

        if(document == null)
            throw new DocumentNotFoundException();

        return document;
    }

    public List<Document> getAllDocuments() {

        /*
            FIXME: Fetch the user id from the JWT
        */

        Long userId = 1L;

        var user = userRepository.findById(userId).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        return documentRepository.findAllByOwnerId(1L);
    }

    public Document updateTitle(UUID documentId, Document documentRequest) {

        /*
            FIXME: Fetch the user id from the JWT
        */

        Long userId = 1L;

        var user = userRepository.findById(userId).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        var document = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        document = document.updateTitle(documentRequest);

        documentRepository.save(document);

        return document;
    }

    public void deleteDocument(UUID documentId) {

        /*
            FIXME: Fetch the user id from the JWT
        */

        Long userId = 1L;

        var user = userRepository.findById(userId).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        var document = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        documentRepository.deleteById(document.getId());
    }

    public Content updateContent(UUID documentId, Content content) {

        /*
            FIXME: Fetch the user id from the JWT
        */

        Long userId = 1L;

        var user = userRepository.findById(userId).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        var document = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        System.out.println(content.getContent());

        document.getContent().setContent(content.getContent());

        documentRepository.save(document);

        return document.getContent();
    }

    public Content getContent(UUID documentId) {

        /*
            FIXME: Fetch the user id from the JWT
        */

        Long userId = 1L;

        var user = userRepository.findById(userId).orElse(null);

        if(user == null)
            throw new UserNotFoundException();

        var document = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        return document.getContent();
    }
}
