package com.nipun.system.document.folder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByOwnerIdAndParentFolderPublicId(Long ownerId, UUID parentFolderPublicId);

    Optional<Folder> findByPublicId(UUID folderId);
}
