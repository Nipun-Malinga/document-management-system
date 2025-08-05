package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    Document toEntity(UpdateTitleRequest request);

    Document toEntity(CreateDocumentRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "sharedUsers", expression = "java(document.getSharedUsers())")
    DocumentDto toDto(Document document);

    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "userId", source = "sharedUser.id")
    @Mapping(target = "permission", source = "permission")
    SharedDocumentDto toSharedDocumentDto(SharedDocument document);

    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "author", source = "author.id")
    DocumentVersionDto toDto(DocumentVersion version);
}
