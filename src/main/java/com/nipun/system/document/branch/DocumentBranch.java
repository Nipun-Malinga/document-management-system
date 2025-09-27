package com.nipun.system.document.branch;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.version.Version;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    private Version version;

    @OneToOne(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "content_id")
    private DocumentBranchContent content;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public boolean isBranchTitleExistsAlready(String branchName) {
        return this.branchName.equalsIgnoreCase(branchName);
    }

    public String getBranchContent() {
        return content.getContent();
    }

    public void setBranchContent(String content) {
        this.content.setContent(content);
    }
}
