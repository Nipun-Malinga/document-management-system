package com.nipun.system.shared.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebsocketPayload <T> {
    private String endpoint;
    private T payload;
}
