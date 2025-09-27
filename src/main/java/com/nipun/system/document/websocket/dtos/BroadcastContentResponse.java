package com.nipun.system.document.websocket.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BroadcastContentResponse {
    private UUID documentId;
    private String content;
}
