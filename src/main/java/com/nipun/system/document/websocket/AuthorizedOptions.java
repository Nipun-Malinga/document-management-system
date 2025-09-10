package com.nipun.system.document.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizedOptions {
    private boolean isAuthorizedUser;
    private boolean isReadOnlyUser;
}
