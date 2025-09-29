package com.nipun.system.document.version.dtos;

import com.nipun.system.document.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVersionRequest {
    @NotNull(message = "Name can't be a null")
    @NotEmpty(message = "Name can't ne empty")
    @Schema(description = "Name of the version")
    String name;

    @NotNull(message = "Status can't be a null")
    @Schema(description = "Status type", examples = {"PUBLIC", "PRIVATE"})
    Status status;
}
