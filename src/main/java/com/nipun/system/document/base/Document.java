package com.nipun.system.document.base;

import com.nipun.system.document.Status;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.share.SharedDocument;
import com.nipun.system.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @OneToMany(
            mappedBy = "document",
            cascade = {CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<SharedDocument> sharedDocuments = new HashSet<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "document",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Branch> branches = new HashSet<>();

    public UUID getMainBranchId() {
        return branches.stream()
                .filter(b -> b.getBranchName().equals("main"))
                .findFirst()
                .orElseThrow()
                .getPublicId();
    }

    public boolean isSharedDocument() {
        return sharedDocuments != null && !sharedDocuments.isEmpty();
    }

    public void addSharedDocument(SharedDocument document) {
        this.sharedDocuments.add(document);
    }

    public Set<SharedDocument> getSharedUsers() {
        return this.getSharedDocuments();
    }

    public void removeSharedUser(Long userId) {
        this.getSharedDocuments().removeIf(shDoc -> shDoc.getSharedUser().getId().equals(userId));
    }

    public void addBranch(Branch branch) {
        this.branches.add(branch);
    }

    public Branch getBranch(UUID branchId) {
        return this.getBranches().stream()
                .filter(branch -> branch.getPublicId().equals(branchId))
                .findAny()
                .orElse(null);
    }
}
