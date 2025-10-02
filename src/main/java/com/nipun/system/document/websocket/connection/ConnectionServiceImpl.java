package com.nipun.system.document.websocket.connection;

import com.nipun.system.document.websocket.cache.WebsocketCacheService;
import com.nipun.system.document.websocket.state.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConnectionServiceImpl implements ConnectionService {
    private final WebsocketCacheService websocketCacheService;
    private final StateService stateService;
}
