package com.nipun.system.document;

import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.UpdateContentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContentMapper {
    @Mapping(target = "content", source = "content")
    Content toEntity(UpdateContentRequest request);
    ContentDto toDto(Content content);
}
