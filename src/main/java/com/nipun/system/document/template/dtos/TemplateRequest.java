package com.nipun.system.document.template.dtos;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    @NotEmpty(message = "title cannot be empty")
    @NotNull(message = "title cannot be null")
    @Size(min = 1, max = 30, message = "title must be between 0 and 30 characters")
    @Parameter(name = "Title", description = "Title of the template")
    private String title;

    @NotEmpty(message = "template cannot be empty")
    @NotNull(message = "template cannot be null")
    @Parameter(name = "Template", description = "Template structure of the document")
    private String template;
}
