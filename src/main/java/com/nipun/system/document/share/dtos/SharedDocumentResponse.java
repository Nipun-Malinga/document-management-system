package com.nipun.system.document.share.dtos;

import com.nipun.system.document.share.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
@Schema(description = "Response object for user permission")
public class SharedDocumentResponse {
    private UUID documentId;
    private Long userId;
    private Permission permission;
}
