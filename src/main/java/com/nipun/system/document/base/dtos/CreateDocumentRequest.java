package com.nipun.system.document.base.dtos;

import com.nipun.system.document.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Status can't be a null")
    @Schema(description = "Status type", examples = {"PUBLIC", "PRIVATE"})
    private Status status;
}