package com.nipun.system.document.dtos.version;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
public class DocumentVersionDto {
    private UUID id;

    private UUID documentId;

    private Long author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}
