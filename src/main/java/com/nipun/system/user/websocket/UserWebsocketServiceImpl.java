package com.nipun.system.user.websocket;

import com.nipun.system.shared.entities.WebsocketPayload;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.cache.UserRedisCacheServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserWebsocketServiceImpl implements UserWebsocketService {
    private final UserRedisCacheServiceImpl userRedisCacheService;

    @Override
    public void addConnectedSessionToCache(String sessionId, Principal principal) {
        var userId = UserIdUtils.getUserIdFromPrincipal(principal);
        userRedisCacheService.putConnectedSession(sessionId, userId);
        addConnectedUsersToCache(userId);
    }

    @Override
    public Boolean isUserOnline(Long userId) {
        Set<Long> users = userRedisCacheService.getConnectedUsers();

        if (users != null) {
            return users.contains(userId);
        }

        return false;
    }

    @Override
    public WebsocketPayload<Boolean> broadcastConnectedUser(String sessionId) {
        var userId = userRedisCacheService.getConnectedUserIdFromSession(sessionId);

        if (userId == null) return null;

        var userStatus = isUserOnline(userId);
        return new WebsocketPayload<>("/user/" + userId + "/status", userStatus);
    }

    @Override
    public WebsocketPayload<Boolean> removeConnectedUser(String sessionId) {
        var userId = userRedisCacheService.getConnectedUserIdFromSession(sessionId);

        if (userId == null) return null;

        removeDisconnectedUserFromCache(userId);

        userRedisCacheService.removeConnectedSession(sessionId);

        var userStatus = isUserOnline(userId);

        return new WebsocketPayload<>("/user/" + userId + "/status", userStatus);
    }

    private void addConnectedUsersToCache(Long userId) {
        Set<Long> users = userRedisCacheService.getConnectedUsers();

        if (users == null) users = new HashSet<>();

        users.add(userId);

        userRedisCacheService.putConnectedUsers(users);
    }

    private void removeDisconnectedUserFromCache(Long userId) {
        Set<Long> users = userRedisCacheService.getConnectedUsers();

        if (users != null) {
            users.remove(userId);
            userRedisCacheService.putConnectedUsers(users);
        }
    }
}
