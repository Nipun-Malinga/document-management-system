package com.nipun.system.document.websocket.selectedTextPosition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedTextPosition {
    private int start;
    private int end;
}
