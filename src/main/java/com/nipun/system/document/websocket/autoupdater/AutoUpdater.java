package com.nipun.system.document.websocket.autoupdater;

import com.nipun.system.document.websocket.connection.ConnectionService;
import com.nipun.system.document.websocket.state.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Service
public class AutoUpdater {
    private final StateService stateService;
    private final ConnectionService connectionService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    public void startAutoUpdater(UUID documentId, UUID branchId) {
        String key = documentId + ":" + branchId;

        if (activeTasks.containsKey(key)) return;

        Runnable updater = () -> {
            stateService.updateDocument(documentId, branchId);

            if (connectionService.getConnectedUsers(documentId, branchId).isEmpty())
                stopAutoUpdater(key);
        };

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(updater, 30, 30, TimeUnit.SECONDS);
        activeTasks.put(key, task);
    }

    public void stopAutoUpdater(String key) {
        ScheduledFuture<?> task = activeTasks.remove(key);
        if (task != null) task.cancel(true);
    }
}
