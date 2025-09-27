package com.nipun.system.document.share;

import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SharedDocumentMapper {
    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "userId", source = "sharedUser.id")
    @Mapping(target = "permission", source = "permission")
    SharedDocumentResponse toSharedDocumentDto(SharedDocument document);
}
