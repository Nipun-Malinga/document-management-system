package com.nipun.system.document.branch;

import com.nipun.system.document.Document;
import com.nipun.system.document.version.DocumentVersion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_branches")
public class DocumentBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "branch_name")
    private String branchName;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(cascade = {CascadeType.REMOVE})
    @JoinColumn(name = "version_id")
    private DocumentVersion version;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "content_id")
    private DocumentBranchContent content;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public void addData(DocumentVersion version, String branchName,DocumentBranchContent content, Document document) {
        this.setDocument(document);
        this.setVersion(version);
        this.setContent(content);
        this.setBranchName(branchName);
        this.setPublicId(UUID.randomUUID());
        this.setTimestamp(LocalDateTime.now());
    }

    public boolean isBranchTitleExistsAlready(String branchName) {
        return this.branchName.equalsIgnoreCase(branchName);
    }
}
