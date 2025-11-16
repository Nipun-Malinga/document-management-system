package com.nipun.system.document.template;

import com.nipun.system.document.template.dtos.TemplateRequest;
import com.nipun.system.document.template.dtos.TemplateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemplateMapper {
    Template toEntity(TemplateRequest request);

    @Mapping(source = "id", target = "id")
    TemplateResponse toDto(Template template);
}
