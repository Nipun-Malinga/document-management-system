package com.nipun.system.document.websocket.selectedTextPosition;

import com.nipun.system.shared.services.WebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SelectedTextPositionServiceImpl implements SelectedTextPositionService {

    private final WebsocketService websocketService;

    @Override
    public void broadcastUserSelectPositions(
            SelectedTextPosition selectedTextPosition,
            Long userId,
            UUID documentId
    ) {
        var endpoint = "/document/" + documentId + "/user/" + userId +"/accept-selected-positions";
        websocketService.broadcastPayload(endpoint, selectedTextPosition);
    }
}
