package com.nipun.system.document.websocket.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StatusRequest {
    private String content;
}
