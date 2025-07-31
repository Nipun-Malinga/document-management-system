package com.nipun.system.document.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTitleRequest {
    @NotBlank(message = "Title can't be a blank")
    @Size(min = 1, max = 255, message = "Title can't be longer than 255 characters")
    private String title;
}
