package com.nipun.system.document.base;

import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.share.SharedDocument;
import com.nipun.system.document.version.Version;
import com.nipun.system.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private Set<Version> versions = new HashSet<>();

    @OneToMany(
            mappedBy = "document",
            cascade = {CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Branch> branches = new HashSet<>();

    public void addContent(String content) {
        getContent().setContent(content);
        setUpdatedAt(LocalDateTime.now());
    }

    public String getDocumentContent() {
        return getContent().getContent();
    }

    public void setDocumentContent(String content) {
        getContent().setContent(content);
    }

    public Document updateTitle(String title) {
        setTitle(title);
        setUpdatedAt(LocalDateTime.now());
        return this;
    }

    public void addDocumentVersion(Version version) {
        versions.add(version);
    }

    public void removeVersion(Version version) {versions.remove(version);}

    public void addSharedDocument(SharedDocument document) {
        sharedDocuments.add(document);
    }

    public Set<SharedDocument> getSharedUsers() {
        return getSharedDocuments();
    }

    public void removeSharedUser(Long userId) {
        getSharedDocuments().removeIf(shDoc -> shDoc .getSharedUser().getId().equals(userId));
    }
}
