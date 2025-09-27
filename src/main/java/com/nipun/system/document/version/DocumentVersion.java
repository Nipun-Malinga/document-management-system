package com.nipun.system.document.version;

import com.nipun.system.document.base.Document;
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

    @OneToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "version_content_id")
    private DocumentVersionContent content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author")
    private User author;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @OneToMany(
            mappedBy = "version",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<DocumentBranch> documentBranches = new HashSet<>();

    @ManyToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "branch_id")
    private DocumentBranch branch;

    public String getVersionContent() {
        return content.getContent();
    }
}
