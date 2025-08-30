package com.nipun.system.document.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BroadcastContentDto {
    private UUID documentId;
    private String content;
}
