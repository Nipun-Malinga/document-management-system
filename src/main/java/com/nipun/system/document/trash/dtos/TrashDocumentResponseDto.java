package com.nipun.system.document.trash.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrashDocumentResponseDto {
    @Schema(description = "Trash ID", example = "Hello everyone")
    private long id;

    @Schema(description = "Trashed document ID", example = "Hello everyone")
    private UUID documentId;

    @Schema(description = "Trashed branch ID", example = "Hello everyone")
    private UUID branchId;

    @Schema(description = "Trashed date", example = "Hello everyone")
    private LocalDateTime added_date;
}
