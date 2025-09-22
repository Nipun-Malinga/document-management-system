package com.nipun.system.document.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Request object for creating a document")
public class CreateDocumentRequest {

    @NotBlank(message = "Title can't be a blank")
    @Size(min = 1, max = 255, message = "Title can't be longer than 255 characters")
    @Schema(description = "Title of the document", example = "Project Plan")
    private String title;
}