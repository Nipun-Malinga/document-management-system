package com.nipun.system.document.dtos.share;

import com.nipun.system.document.share.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareDocumentRequest {
    @NotNull(message = "Permission can't be a null")
    private Permission permission;

}
