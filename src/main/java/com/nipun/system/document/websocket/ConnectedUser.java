package com.nipun.system.document.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConnectedUser {
    private Long userId;
    private UUID documentId;
}
