package com.nipun.system.document.dtos.share;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AccessSharedDocumentRequest {

    @NotNull(message = "DocumentId can't ne a null value")
    private UUID documentId;
}
