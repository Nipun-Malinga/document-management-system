package com.nipun.system.document.branch;

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
    @JoinColumn(name = "version_id")
    private DocumentVersion version;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "content_id")
    private DocumentBranchContent content;

    private LocalDateTime timestamp;

    public void addData(DocumentVersion version, String branchName,DocumentBranchContent content) {
        this.setVersion(version);
        this.setContent(content);
        this.setBranchName(branchName);
        this.setPublicId(UUID.randomUUID());
        this.setTimestamp(LocalDateTime.now());
    }
}
