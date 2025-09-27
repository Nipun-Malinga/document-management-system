package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.DocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "id", source = "publicId")
    DocumentResponse toDto(Document document);
}
