package com.nipun.system.document.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateDocumentRequest {
    @NotBlank(message = "Title can't be a blank")
    @Size(min = 1, max = 255, message = "Title can't be longer than 255 characters")
    private String title;
}