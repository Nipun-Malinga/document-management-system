package com.nipun.system.document.version;

import com.nipun.system.document.Status;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.content.Content;
import com.nipun.system.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_versions")
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @ManyToOne(
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE,
                    CascadeType.REMOVE
            },
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToOne(
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE,
                    CascadeType.REMOVE
            },
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author")
    private User createdBy;

    @Column(name = "name")
    private String versionTitle;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    public String getVersionContent() {
        return content.getContent();
    }
}
