package com.nipun.system.document;

import com.nipun.system.document.dtos.SharedDocumentDto;
import com.nipun.system.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "content_id")
    private Content content;

    @OneToMany(mappedBy = "document", cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    private Set<SharedDocument> sharedDocuments = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = {CascadeType.PERSIST ,CascadeType.MERGE, CascadeType.REMOVE})
    private Set<DocumentVersion> documentVersions = new HashSet<>();

    public Document updateTitle(Document updatedDocument) {
        this.setTitle(updatedDocument.getTitle());
        this.setUpdatedAt(LocalDateTime.now());
        return this;
    }

    public static Document createDocument(Document document, User user) {
        document.setPublicId(UUID.randomUUID());
        document.setOwner(user);
        document.setContent(new Content());
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        return document;
    }

    public void addSharedDocument(SharedDocument document) {
        sharedDocuments.add(document);
    }

    public List<SharedDocumentDto> getSharedUsers() {
         return this.getSharedDocuments()
                .stream()
                .map(item ->
                        new SharedDocumentDto(
                                item.getDocument().getPublicId(),
                                item.getSharedUser().getId(),
                                item.getPermission())
                )
                .toList();
    }

    public void addDocumentVersion(DocumentVersion documentVersion) {
        documentVersions.add(documentVersion);
    }

    public boolean isUnauthorizedUser(Long userId) {
        return !isOwner(userId) &&  !isSharedUser(userId);
    }

    private boolean isOwner(Long userId) {
        return !Objects.equals(this.getOwner().getId(), userId);
    }

    private boolean isSharedUser(Long userId) {
        return !this.getSharedUsers()
                .stream()
                .map(user ->
                    Objects.equals(user.getUserId(), userId))
                .findFirst()
                .orElse(false);
    }
}
