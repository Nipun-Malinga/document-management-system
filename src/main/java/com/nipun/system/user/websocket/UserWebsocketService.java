package com.nipun.system.user.websocket;

import com.nipun.system.document.utils.Utils;
import com.nipun.system.shared.entities.WebsocketPayload;
import com.nipun.system.user.cache.UserRedisCacheServiceImpl;
import com.nipun.system.user.exceptions.UserIdNotFoundInSessionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserWebsocketService {
    private final UserRedisCacheServiceImpl userRedisCacheService;

    public void addConnectedSessionToCache(String sessionId, Principal principal) {
        var userId = Utils.getUserIdFromPrincipal(principal);
        userRedisCacheService.putConnectedSession(sessionId, userId);
        addConnectedUsersToCache(userId);
    }

    public Boolean isUserOnline(Long userId) {
        Set<Long> users = userRedisCacheService.getConnectedUsers();

        if(users != null) {
            return users.contains(userId);
        }

        return false;
    }

    private Long geConnectedUserIdFromSession(String sessionId) {
        var userId = userRedisCacheService.getConnectedUserIdFromSession(sessionId);

        if(userId == null)
            throw new UserIdNotFoundInSessionException(sessionId);

        return userId;
    }

    private Long removeDisconnectedSessionFromCache(String sessionId) {

        var userId = userRedisCacheService.getConnectedUserIdFromSession(sessionId);

        if(userId == null)
            throw new UserIdNotFoundInSessionException(sessionId);

        removeDisconnectedUserFromCache(userId);

        userRedisCacheService.removeConnectedSession(sessionId);

        return userId;
    }

    private void addConnectedUsersToCache(Long userId) {
        Set<Long> users = userRedisCacheService.getConnectedUsers();

        if (users == null) users = new HashSet<>();

        users.add(userId);

        userRedisCacheService.putConnectedUsers(users);
    }

    private void removeDisconnectedUserFromCache(Long userId) {
        Set<Long> users = userRedisCacheService.getConnectedUsers();

        if(users != null) {
            users.remove(userId);
            userRedisCacheService.putConnectedUsers(users);
        }
    }

    public WebsocketPayload<Boolean> broadcastConnectedUser(String sessionId) {
        var userId = geConnectedUserIdFromSession(sessionId);
        var userStatus = isUserOnline(userId);
        return new WebsocketPayload<>("/user/" + userId + "/status", userStatus);
    }

    public WebsocketPayload<Boolean> removeConnectedUser(String sessionId) {
        var userId = removeDisconnectedSessionFromCache(sessionId);
        var userStatus = isUserOnline(userId);

        return new WebsocketPayload<>("/user/" + userId + "/status", userStatus);
    }
}
