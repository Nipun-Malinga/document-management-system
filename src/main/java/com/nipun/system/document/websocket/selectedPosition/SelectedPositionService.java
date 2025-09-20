package com.nipun.system.document.websocket.selectedPosition;

import com.nipun.system.shared.services.WebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SelectedPositionService {

    private final WebsocketService websocketService;

    public void broadcastUserSelectPositions(
            SelectedPosition selectedPosition,
            Long userId,
            UUID documentId
    ) {
        websocketService
                .broadcastPayload(
                        "/document/" + documentId + "/user/" + userId +"/accept-selected-positions",
                        selectedPosition
                );
    }
}
