package com.nipun.system.document.base.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nipun.system.document.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Data
public class DocumentResponse {

    private UUID id;

    private String title;

    private Long ownerId;

    private Status status;

    private boolean shared;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
