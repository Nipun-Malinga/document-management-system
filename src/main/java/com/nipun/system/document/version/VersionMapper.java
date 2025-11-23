package com.nipun.system.document.version;

import com.nipun.system.document.version.dtos.VersionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VersionMapper {
    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "branchId", source = "branch.publicId")
    @Mapping(target = "documentId", source = "branch.document.publicId")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "title", source = "versionTitle")
    @Mapping(target = "created_at", source = "created_at")
    VersionResponse toDto(Version version);
}
