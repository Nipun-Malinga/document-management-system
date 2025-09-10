package com.nipun.system.document;

import com.nipun.system.document.branch.DocumentBranch;
import com.nipun.system.document.dtos.DocumentSharedDocumentDto;
import com.nipun.system.document.share.Permission;
import com.nipun.system.document.share.SharedDocument;
import com.nipun.system.document.utils.Utils;
import com.nipun.system.document.version.DocumentVersion;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "content_id")
    private Content content;

    @OneToMany(
            mappedBy = "document",
            cascade = {CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<SharedDocument> sharedDocuments = new HashSet<>();

    @OneToMany(
            mappedBy = "document",
            cascade = {CascadeType.PERSIST ,CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<DocumentVersion> documentVersions = new HashSet<>();

    @OneToMany(
            mappedBy = "document",
            cascade = {CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<DocumentBranch> documentBranches = new HashSet<>();

    public void addContent(String content) {
        this.getContent().setContent(content);
        this.setUpdatedAt(LocalDateTime.now());
    }

    public boolean isContentNull() {
        return this.getContent().getContent() == null;
    }

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

    public List<DocumentSharedDocumentDto> getSharedUsers() {
        return this.getSharedDocuments()
                .stream()
                .map(item ->
                        new DocumentSharedDocumentDto(
                                item.getSharedUser().getId(),
                                item.getPermission())
                )
                .toList();
    }

    public void addDocumentVersion(Document document, User user) {
        documentVersions.add(Utils.createVersion(document, user));
    }

    public boolean isUnauthorizedUser(Long userId) {
        return !isOwner(userId) &&  !isSharedUser(userId);
    }

    public boolean isReadOnlyUser(Long userId) {
        return getSharedUsers()
                .stream()
                .anyMatch(user ->
                        Objects.equals(user.getUserId(), userId) &&
                                user.getPermission() == Permission.READ_ONLY);
    }

    public void removeSharedUser(Long userId) {
        getSharedDocuments().forEach(sharedDocument -> {
            if (sharedDocument.getSharedUser().getId().equals(userId)) {
                sharedDocuments.remove(sharedDocument);
            }
        });
    }

    private boolean isOwner(Long userId) {
        return Objects.equals(this.getOwner().getId(), userId);
    }

    private boolean isSharedUser(Long userId) {
        return this.getSharedUsers()
                .stream()
                .anyMatch(user -> Objects.equals(user.getUserId(), userId));
    }
}
