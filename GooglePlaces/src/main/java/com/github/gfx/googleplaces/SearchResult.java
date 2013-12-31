package com.github.gfx.googleplaces;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

// https://developers.google.com/places/documentation/search
public class SearchResult implements Serializable, Iterable<Place>, ResultBase {
    public static final String OK = "OK";
    public static final String ZERO_REZULTS = "ZERO_RESULTS";
    public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public static final String REQUEST_DENIED = "REQUEST_DENIED";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    private RequestError error; // to return an error

    @Key
    public String status;

    @Key
    public List<String> html_attributions;

    @Key
    public String next_page_token;

    @Key
    public List<Place> results;

    // delegate to results

    public int size() {
        return results.size();
    }

    public Place get(int n) {
        return results.get(n);
    }

    @Override
    public Iterator<Place> iterator() {
        return results.iterator();
    }

    @Override
    public RequestError getError() {
        if (error != null) {
            return error;
        }
        else if (! OK.equals(status)) {
            return new RequestError(status, null);
        }
        else {
            return null;
        }
    }

    public void setError(RequestError error) {
        this.error = error;
    }

    @Override
    public boolean isSuccess() {
        return error == null && OK.equals(status);
    }
}
