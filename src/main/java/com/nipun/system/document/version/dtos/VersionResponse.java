package com.nipun.system.document.version.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nipun.system.document.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
public class VersionResponse {
    private UUID id;

    private UUID branchId;

    private UUID documentId;

    private Long createdBy;

    private String title;

    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;

}
