package com.nipun.system.document.websocket.connection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConnectedUser {
    private Long userId;
    private UUID documentId;
    private UUID branchId;
}
