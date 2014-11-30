package de.hska.iwii.picturecommunity.backend.entities;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds a user with corresponding online state.
 */
public class OnlineState {
    private final User user;
    private final boolean online;

    public OnlineState(User user, boolean online) {
        this.user = user;
        this.online = online;
    }

    public boolean isOnline() {
        Logger log = Logger.getLogger(this.getClass().getName());
        log.log(Level.INFO, "Online state of " + user + " checked.");
        return online;
    }

    public User getUser() {
        return user;
    }
}
