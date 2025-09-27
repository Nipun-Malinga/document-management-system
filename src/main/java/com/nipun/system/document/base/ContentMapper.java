package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.base.dtos.UpdateContentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContentMapper {
    @Mapping(target = "content", source = "content")
    Content toEntity(UpdateContentRequest request);
    ContentResponse toDto(Content content);
}
