package com.nipun.system.user.cache;

import java.util.Set;

public interface UserCacheService {

    Long getConnectedUserIdFromSession(String sessionId);

    void putConnectedSession(String sessionId, Long userId);

    void removeConnectedSession(String sessionId);

    Set<Long> getConnectedUsers();

    void putConnectedUsers(Set<Long> users);
}
