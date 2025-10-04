package com.nipun.system.document.websocket.positions.mouseposition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Data
public class MousePosition {
    private Double positionX;
    private Double positionY;
}
