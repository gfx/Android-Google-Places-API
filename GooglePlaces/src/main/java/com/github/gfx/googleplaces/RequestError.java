package com.github.gfx.googleplaces;

import android.util.Log;

public class RequestError extends Exception {
    private final String reason;
    private final Throwable parent;

    RequestError(String reason, Throwable parent) {
        this.reason = reason;
        this.parent = parent;
    }

    public Throwable getParent() {
        return parent;
    }

    @Override
    public String toString() {
        String message = reason;
        if (parent != null) {
            message += ": " + Log.getStackTraceString(parent);
        }
        return String.format("RequestError: %s\n%s", reason, message);
    }
}
