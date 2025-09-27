package com.nipun.system.document.share.dtos;

import com.nipun.system.document.share.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class SharedDocumentResponse {
    private UUID documentId;
    private Long userId;
    private Permission permission;
}
