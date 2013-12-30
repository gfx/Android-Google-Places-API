package com.github.gfx.googleplaces;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @see <a href="https://developers.google.com/places/documentation/search">Google Places API</a>
 */
public class GooglePlaces {
    // Google Places serach url's
    // See https://developers.google.com/places/documentation/search for details
    private static final String API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String NEARBY_SEARCH_PATH = "/nearbysearch/json?";
    private static final String RADER_SEARCH_PATH = "/radersearch/json?";
    private static final String TEXT_SEARCH_PATH = "/textsearch/json?";
    private static final String DETAILS_PATH = "/details/json?";

    private String userAgent = "Android Google Places Client/1.0";
    private String apiBase = API_BASE;

    private final String apiKey;
    private final HttpRequestFactory requestFactory;

    public GooglePlaces(String googleApiKey) {
        this(googleApiKey, new NetHttpTransport());
    }

    public GooglePlaces(String googleApiKey, HttpTransport transport) {
        apiKey = googleApiKey;
        requestFactory = transport.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                final HttpHeaders headers = new HttpHeaders();
                headers.setUserAgent(userAgent);
                request.setHeaders(headers);
                final JsonObjectParser parser = new JsonObjectParser.Builder(new AndroidJsonFactory()).build();
                request.setParser(parser);

            }
        });
    }

    public PlaceList nearBySearch(double latitude, double longitude, double radiusInMeter, boolean sensor) throws RequestError {
        final GenericUrl url = new GenericUrl(apiBase + NEARBY_SEARCH_PATH);
        url.put("location", latitude + "," + longitude);
        url.put("radius", radiusInMeter);
        url.put("sensor", sensor);


        try {
            return get(url).parseAs(PlaceList.class);
        } catch (IOException e) {
            throw new RequestError("Failed to nearBySearch", e);
        }
    }

    private HttpResponse get(GenericUrl url) throws IOException {
        final HttpRequest request = requestFactory.buildGetRequest(url);
        request.getUrl().put("key", apiKey);
        return request.execute();
    }

    public static class PlaceList implements Serializable {
        @Key
        public String status;

        @Key
        public List<Place> results;
    }
}
