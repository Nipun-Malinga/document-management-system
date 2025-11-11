package com.nipun.system.document.trash;

import com.nipun.system.document.trash.dtos.TrashDocumentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrashMapper {
    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "branchId", source = "branch.publicId")
    TrashDocumentResponseDto toTrashDocumentDto(Trash trash);
}
