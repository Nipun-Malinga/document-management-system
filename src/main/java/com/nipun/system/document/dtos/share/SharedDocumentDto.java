package com.nipun.system.document.dtos.share;

import com.nipun.system.document.share.Permission;
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
