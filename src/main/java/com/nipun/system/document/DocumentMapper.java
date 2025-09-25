package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "id", source = "publicId")
    DocumentDto toDto(Document document);
}
