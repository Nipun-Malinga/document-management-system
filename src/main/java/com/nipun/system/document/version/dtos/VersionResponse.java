package com.nipun.system.document.version.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nipun.system.document.Status;
import com.nipun.system.user.dtos.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VersionResponse {
    private UUID id;

    private UUID branchId;

    private UUID documentId;

    private UserResponse createdBy;

    private String title;

    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
