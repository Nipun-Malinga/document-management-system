package com.nipun.system.document.share.dtos;

import com.nipun.system.document.share.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SharedDocumentResponse {
    private Long userId;
    private String username;
    private String email;
    private Permission permission;
}
