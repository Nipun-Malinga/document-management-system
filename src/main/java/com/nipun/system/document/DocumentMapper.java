package com.nipun.system.document;

import com.nipun.system.document.dtos.CreateDocumentRequest;
import com.nipun.system.document.dtos.DocumentDto;
import com.nipun.system.document.dtos.UpdateTitleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    Document toEntity(UpdateTitleRequest request);

    Document toEntity(CreateDocumentRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "id", source = "publicId")
    DocumentDto toDto(Document document);
}
