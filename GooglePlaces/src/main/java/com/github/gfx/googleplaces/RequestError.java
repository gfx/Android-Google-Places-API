package com.github.gfx.googleplaces;

import android.util.Log;

public class RequestError extends Exception {
    private final String reason;
    private final Throwable parent;

    RequestError(String reason, Throwable parent) {
        this.reason = reason;
        this.parent = parent;
    }

    @Override
    public String toString() {
        String stackTrace = Log.getStackTraceString(this);
        if (parent != null) {
            stackTrace += Log.getStackTraceString(parent);
        }
        return String.format("RequestError: %s\n%s", reason, stackTrace);
    }
}
