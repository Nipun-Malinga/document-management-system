package com.nipun.system.document.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateContentRequest {

    @NotNull(message = "Content can't be null")
    @Size(max = 10000, message = "Content too large")
    private String content;
}
