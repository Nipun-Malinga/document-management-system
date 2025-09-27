package com.nipun.system.document.websocket.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizedOptions {
    private boolean isUnAuthorizedUser;
    private boolean isReadOnlyUser;
}
