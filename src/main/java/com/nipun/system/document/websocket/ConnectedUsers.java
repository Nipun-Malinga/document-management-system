package com.nipun.system.document.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@ToString
@Data
@AllArgsConstructor
public class ConnectedUsers {
    private UUID documentId;
    private Set<Long> users;
}
