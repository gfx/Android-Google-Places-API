package com.github.gfx.googleplaces;

public class PlaceDetail implements ResultBase {
    private RequestError error;

    @Override
    public RequestError getError() {
        return error;
    }

    @Override
    public void setError(RequestError error) {
        this.error = error;
    }

    @Override
    public boolean isSuccess() {
        return error != null;
    }
}
