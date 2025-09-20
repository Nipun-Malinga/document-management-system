package com.nipun.system.document.websocket.selectedTextPosition;

import com.nipun.system.shared.services.WebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SelectedTextPositionService {

    private final WebsocketService websocketService;

    public void broadcastUserSelectPositions(
            SelectedTextPosition selectedTextPosition,
            Long userId,
            UUID documentId
    ) {
        websocketService
                .broadcastPayload(
                        "/document/" + documentId + "/user/" + userId +"/accept-selected-positions", selectedTextPosition);
    }
}
