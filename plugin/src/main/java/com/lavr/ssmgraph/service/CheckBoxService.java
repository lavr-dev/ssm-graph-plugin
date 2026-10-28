package com.lavr.ssmgraph.service;

import com.intellij.openapi.components.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service(Service.Level.PROJECT)
public final class CheckBoxService {
    private final List<Consumer<Boolean>> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean catchSsmLaunchEnabled;

    public boolean isCatchSsmLaunchEnabled() {
        return catchSsmLaunchEnabled;
    }

    public void setCatchSsmLaunchEnabled(boolean enabled) {
        catchSsmLaunchEnabled = enabled;
        listeners.forEach(listener -> listener.accept(enabled));
    }

    public Runnable addCatchSsmLaunchListener(Consumer<Boolean> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }
}
