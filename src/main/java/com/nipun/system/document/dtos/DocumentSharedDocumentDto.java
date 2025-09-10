package com.nipun.system.document.dtos;

import com.nipun.system.document.share.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DocumentSharedDocumentDto {
    private Long userId;
    private Permission permission;
}
