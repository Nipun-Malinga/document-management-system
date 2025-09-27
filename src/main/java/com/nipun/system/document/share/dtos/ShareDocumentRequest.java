package com.nipun.system.document.share.dtos;

import com.nipun.system.document.share.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for user permission")
public class ShareDocumentRequest {
    @NotNull(message = "Permission can't be a null")
    @Schema(description = "Permission type", examples = {"READ_ONLY", "READ_WRITE"})
    private Permission permission;

}
