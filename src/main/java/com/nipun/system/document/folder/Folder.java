package com.nipun.system.document.folder;

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
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @ManyToOne
    @JoinColumn(name = "parent_folder")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<Folder> subFolders = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "owner_id")
    private User owner;

    public void addSubFolder(Folder folder) {
        folder.setParentFolder(this);
        subFolders.add(folder);
    }
}
