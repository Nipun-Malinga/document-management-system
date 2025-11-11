package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.DocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "shared", expression = "java(document.isSharedDocument())")
    @Mapping(target = "branchCount", expression = "java(document.getBranches().size())")
    @Mapping(target = "trashed", source = "trashed")
    DocumentResponse toDto(Document document);
}
