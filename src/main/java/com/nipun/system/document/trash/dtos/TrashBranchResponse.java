package com.nipun.system.document.trash.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nipun.system.document.branch.dtos.BranchResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrashBranchResponse {
    @Schema(description = "Trash ID", example = "Hello everyone")
    private long id;

    @Schema(description = "Branch")
    private BranchResponse branch;

    @Schema(description = "Trashed date", example = "Hello everyone")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime added_date;
}
