package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.DocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    
    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "status", source = "status")
    DocumentResponse toDto(Document document);
}
