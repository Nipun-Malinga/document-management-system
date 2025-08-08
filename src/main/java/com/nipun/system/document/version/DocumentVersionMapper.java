package com.nipun.system.document.version;

import com.nipun.system.document.dtos.version.DocumentVersionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentVersionMapper {
    @Mapping(target = "id", source = "versionNumber")
    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "author", source = "author.id")
    DocumentVersionDto toDto(DocumentVersion version);
}
