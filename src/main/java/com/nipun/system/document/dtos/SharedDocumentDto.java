package com.nipun.system.document.dtos;

import com.nipun.system.document.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class SharedDocumentDto {
    private UUID documentId;
    private Long userId;
    private Permission permission;
}
