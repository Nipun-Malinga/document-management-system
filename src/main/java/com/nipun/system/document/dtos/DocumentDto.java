package com.nipun.system.document.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nipun.system.document.dtos.share.SharedDocumentDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class DocumentDto {

    private UUID id;

    private String title;

    private Long ownerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<SharedDocumentDto> sharedUsers;
}
