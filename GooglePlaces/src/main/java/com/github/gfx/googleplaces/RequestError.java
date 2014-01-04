package com.github.gfx.googleplaces;

import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

public class RequestError extends Exception {
    private final String reason;
    private final Throwable parent;
    private final HttpRequest request;

    RequestError(String reason, Throwable parent, HttpRequest request) {
        this.reason = reason;
        this.parent = parent;
        this.request = request;
    }

    public Throwable getParent() {
        return parent;
    }

    @Override
    public String toString() {
        String message = reason;
        if (request != null) {
            message += "¥n" + request.getRequestMethod() + " " + request.getUrl();
        }
        if (parent != null) {
            message += ": " + Log.getStackTraceString(parent);
        }
        return String.format("RequestError: %s\n%s", reason, message);
    }
}
