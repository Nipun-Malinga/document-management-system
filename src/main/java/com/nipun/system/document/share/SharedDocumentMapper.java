package com.nipun.system.document.share;

import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SharedDocumentMapper {
    @Mapping(target = "username", source = "sharedUser.username")
    @Mapping(target = "email", source = "sharedUser.email")
    SharedDocumentResponse toSharedDocumentDto(SharedDocument document);
}
