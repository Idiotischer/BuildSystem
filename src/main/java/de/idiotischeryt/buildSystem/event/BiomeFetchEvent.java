package de.idiotischeryt.buildSystem.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BiomeFetchEvent extends Event {
    private final String biomeName;
    HandlerList handlers = new HandlerList();
    private boolean valid;

    public BiomeFetchEvent(String biomeName, boolean valid) {
        this.biomeName = biomeName;
        this.valid = valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public String getBiomeName() {
        return biomeName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
