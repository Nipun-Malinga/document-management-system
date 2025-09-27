package com.nipun.system.document.websocket.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DocumentStatusRequest {
    private String content;
}
