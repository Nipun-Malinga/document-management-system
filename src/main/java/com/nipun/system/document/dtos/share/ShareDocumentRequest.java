package com.nipun.system.document.dtos.share;

import com.nipun.system.document.share.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ShareDocumentRequest {
    @NotNull(message = "DocumentId can't be a null")
    private UUID documentId;

    @NotNull(message = "ShareUserId can't be a null")
    private Long shareUserId;

    @NotNull(message = "Permission can't be a null")
    private Permission permission;

}
