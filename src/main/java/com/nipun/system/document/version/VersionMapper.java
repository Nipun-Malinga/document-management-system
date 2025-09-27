package com.nipun.system.document.version;

import com.nipun.system.document.version.dtos.VersionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VersionMapper {
    @Mapping(target = "id", source = "versionNumber")
    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "author", source = "author.id")
    VersionResponse toDto(Version version);
}
