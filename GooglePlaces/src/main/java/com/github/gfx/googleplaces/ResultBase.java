package com.github.gfx.googleplaces;

public interface ResultBase {
    public RequestError getError();
    public void setError(RequestError error);
    public boolean isSuccess();
}
