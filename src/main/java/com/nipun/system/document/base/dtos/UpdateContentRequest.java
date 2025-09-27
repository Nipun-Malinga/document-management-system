package com.nipun.system.document.base.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request object for update document content")
public class UpdateContentRequest {

    @NotNull(message = "Content can't be null")
    @Size(max = 10000, message = "Content too large")
    @Schema(description = "Content of the document", example = "Hello everyone")
    private String content;
}
