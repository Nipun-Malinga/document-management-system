package com.nipun.system.document.share;

import com.nipun.system.document.share.dtos.SharedDocumentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SharedDocumentMapper {
    SharedDocumentDto toSharedDocumentDto(SharedDocument document);
}
