package com.nipun.system.document.folder;

import com.nipun.system.document.folder.dtos.FolderRequest;
import com.nipun.system.document.folder.dtos.FolderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FolderMapper {
    Folder toEntity(FolderRequest request);

    @Mapping(target = "parentFolderId", source = "parentFolder.id")
    FolderResponse toDto(Folder folder);
}
