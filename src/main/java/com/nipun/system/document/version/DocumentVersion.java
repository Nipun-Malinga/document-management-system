package com.nipun.system.document.version;

import com.nipun.system.document.Document;
import com.nipun.system.document.branch.DocumentBranch;
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
@Table(name = "document_version")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "version_number")
    private UUID versionNumber;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "version_content_id")
    private DocumentVersionContent content;

    @ManyToOne
    @JoinColumn(name = "author")
    private User author;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "version", cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REMOVE}, orphanRemoval = true)
    private Set<DocumentBranch> documentBranches = new HashSet<>();

    @ManyToOne(cascade = {CascadeType.REMOVE})
    @JoinColumn(name = "branch_id")
    private DocumentBranch branch;

    public void addData(
            Document document,
            User author,
            DocumentVersionContent content
    ) {
        this.setDocument(document);
        this.setVersionNumber(UUID.randomUUID());
        this.setContent(content);
        this.setAuthor(author);
        this.setTimestamp(LocalDateTime.now());
    }

    public void addBranch(DocumentBranch branch) {
        branch.setVersion(this);
        this.documentBranches.add(branch);
    }
}
