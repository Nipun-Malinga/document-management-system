package com.nipun.system.document.websocket.permissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Permissions {
    private boolean isUnauthorizedUser;
    private boolean isReadOnlyUser;
}
