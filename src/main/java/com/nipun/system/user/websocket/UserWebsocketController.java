package com.nipun.system.user.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class UserWebsocketController {

    private final UserWebsocketService userWebsocketService;

    @SubscribeMapping("/user/{userId}/status")
    public boolean isUserOnline(
            @DestinationVariable Long userId
    ) {
        return userWebsocketService.isUserOnline(userId);
    }
}
