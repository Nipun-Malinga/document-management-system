package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "sharedUsers", expression = "java(document.getSharedUsers())")
    DocumentDto toDto(Document document);
}
