package com.github.gfx.googleplaces;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Google Places API client class
 *
 * @see <a href="https://developers.google.com/places/documentation/search">Google Places API</a>
 */
@SuppressWarnings({"unchecked", "unused"})
public class GooglePlaces {
    // Google Places serach url's
    // See https://developers.google.com/places/documentation/search for details
    private static final String API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String NEARBY_SEARCH_PATH = "/nearbysearch/json";
    private static final String RADAR_SEARCH_PATH = "/radarsearch/json";
    private static final String TEXT_SEARCH_PATH = "/textsearch/json";
    private static final String DETAILS_PATH = "/details/json";

    private String userAgent = "Android Google Places Client/1.0";
    private String apiBase = API_BASE;
    private String language = Locale.getDefault().getLanguage();

    private final String apiKey;
    private final HttpRequestFactory requestFactory;

    private Map<String, Bitmap> cache = new LruMap<>(8);

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

    /**
     * @param language A language code listed in https://spreadsheets.google.com/pub?key=p9pdwsai2hDMsLkXsoM05KQ&gid=1
     */
    public GooglePlaces setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    private abstract class RequestBuilderBase<Derived extends RequestBuilderBase, ResultType extends ResultBase> {
        protected final GenericUrl url;

        protected ErrorListener errorListener = DefaultErrorListener.getInstance();

        public RequestBuilderBase(GenericUrl url) {
            this.url = url;
        }

        public Derived setLanguage(String language) {
            url.put("language", language);
            return (Derived) this;
        }

        public Derived setErrorListener(final ErrorListener listener) {
            errorListener = listener;
            return (Derived) this;
        }

        public Derived get(final ResultListener<ResultType> listener) {
            new AsyncTask<Void, Void, ResultType>() {
                @Override
                protected ResultType doInBackground(Void... params) {
                    HttpRequest request = null;
                    try {
                        request = GooglePlaces.this.buildGetRequest(url);
                        final ResultType result = (ResultType) request.execute().parseAs(getResultTypeClass());
                        if (!result.isSuccess()) {
                            result.setError(new RequestError("Request failure", null, request));
                        }
                        return result;
                    } catch (Exception e) {
                        return createErrorResult(new RequestError("Failed to request " + url.getRawPath(), e, request));
                    }
                }

                @Override
                protected void onPostExecute(ResultType searchResult) {
                    if (searchResult.isSuccess()) {
                        listener.onComplete(searchResult);
                    } else {
                        errorListener.onError(searchResult.getError());
                    }
                }
            }.execute((Void) null);
            return (Derived) this;
        }

        abstract protected Class<?> getResultTypeClass();

        abstract protected ResultType createErrorResult(RequestError error);
    }

    private abstract class SearchBuilderBase<Derived extends SearchBuilderBase> extends RequestBuilderBase<Derived, SearchResult> {

        SearchBuilderBase(GenericUrl url) {
            super(url);
        }

        public Derived setMinPrice(int minPrice) {
            assert 0 <= minPrice && minPrice <= 4;
            url.put("mminprice", minPrice);
            return (Derived) this;
        }

        public Derived setMaxPrice(int maxPrice) {
            assert 0 <= maxPrice && maxPrice <= 4;
            url.put("maxprice", maxPrice);
            return (Derived) this;
        }

        /**
         * One or more terms to be matched against the names of Places, separated with a space character. Results will be restricted to those containing the passed name values. Note that a Place may have additional names associated with it, beyond its listed name. The API will try to match the passed name value against all of these names; as a result, Places may be returned in the results whose listed names do not match the search term, but whose associated names do.
         */
        public Derived setName(String name) {
            url.put("name", name);
            return (Derived) this;
        }

        public Derived setRankBy(PlaceListOrder rankBy) {
            url.put("rankby", rankBy.name());
            return (Derived) this;
        }

        public Derived setOpenNow(boolean openNow) {
            url.put("opennow", openNow);
            return (Derived) this;
        }

        public Derived setPageToken(String pageToken) {
            url.put("pagetoken", pageToken);
            return (Derived) this;
        }

        /**
         * This is experimental and is only available to Places API enterprise customers.
         */
        public Derived setZagatSelected(boolean zagatSelected) {
            url.put("zagatselected", zagatSelected);
            return (Derived) this;
        }

        /**
         * @param types List of types listed in https://developers.google.com/places/documentation/supported_types
         */
        public Derived setTypes(String... types) {
            url.put("types", TextUtils.join("|", types));
            return (Derived) this;
        }

        @Override
        protected Class<?> getResultTypeClass() {
            return SearchResult.class;
        }

        @Override
        protected SearchResult createErrorResult(RequestError error) {
            SearchResult errorResult = new SearchResult();
            errorResult.setError(error);
            return errorResult;
        }
    }


    public class NearbySearchBuilder extends SearchBuilderBase<NearbySearchBuilder> {
        /**
         * Creates a request builder for "nearby search". Its parameters are mandatory.
         */
        public NearbySearchBuilder(double latitude, double longitude, double radiusInMeter, boolean sensor) {
            super(new GenericUrl(apiBase + NEARBY_SEARCH_PATH));

            url.put("location", latitude + "," + longitude);
            url.put("radius", radiusInMeter);
            url.put("sensor", sensor);
        }

        public NearbySearchBuilder setKeyword(String keyword) {
            url.put("keyword", keyword);
            return this;
        }

        @Override
        protected Class<?> getResultTypeClass() {
            return SearchResult.class;
        }

        @Override
        protected SearchResult createErrorResult(RequestError error) {
            SearchResult errorResult = new SearchResult();
            errorResult.setError(error);
            return errorResult;
        }
    }

    public class TextSearchBuilder extends SearchBuilderBase<TextSearchBuilder> {
        /**
         * Creates a request builder for "text search". Its parameters are mandatory.
         */
        public TextSearchBuilder(String query, boolean sensor) {
            super(new GenericUrl(apiBase + TEXT_SEARCH_PATH));
            url.put("query", query);
            url.put("sensor", sensor);
        }

        public TextSearchBuilder setLocation(double latitude, double longitude) {
            url.put("location", latitude + "," + longitude);
            return this;
        }

        public TextSearchBuilder setRadius(double radiusInMeter) {
            url.put("radius", radiusInMeter);
            return this;
        }
    }


    public class RadarSearchBuilder extends SearchBuilderBase<RadarSearchBuilder> {
        /**
         * Creates a request builder for "radar search". Its parameters are mandatory.
         */
        public RadarSearchBuilder(double latitude, double longitude, double radiusInMeter, boolean sensor) {
            super(new GenericUrl(apiBase + RADAR_SEARCH_PATH));

            url.put("location", latitude + "," + longitude);
            url.put("radius", radiusInMeter);
            url.put("sensor", sensor);
        }

        public RadarSearchBuilder setKeyword(String keyword) {
            url.put("keyword", keyword);
            return this;
        }
    }

    public class DetailBuilder extends RequestBuilderBase<DetailBuilder, PlaceDetail> {
        DetailBuilder(String reference, boolean sensor) {
            super(new GenericUrl(apiBase + DETAILS_PATH));

            url.put("reference", reference);
            url.put("sensor", sensor);
        }

        @Override
        protected Class<?> getResultTypeClass() {
            return SearchResult.class;
        }

        @Override
        protected PlaceDetail createErrorResult(RequestError error) {
            PlaceDetail errorResult = new PlaceDetail();
            errorResult.setError(error);
            return errorResult;
        }
    }

    public NearbySearchBuilder nearBySearch(double latitude, double longitude, double radiusInMeter, boolean sensor) {
        return new NearbySearchBuilder(latitude, longitude, radiusInMeter, sensor);
    }

    public TextSearchBuilder textSearch(String query, boolean sensor) {
        return new TextSearchBuilder(query, sensor);
    }

    public RadarSearchBuilder radarSearch(double latitude, double longitude, double radiusInMeter, boolean sensor) {
        return new RadarSearchBuilder(latitude, longitude, radiusInMeter, sensor);
    }

    public DetailBuilder detail(String reference, boolean sensor) {
        return new DetailBuilder(reference, sensor);
    }

    private HttpRequest buildGetRequest(GenericUrl url) throws IOException {
        final HttpRequest request = requestFactory.buildGetRequest(url);
        request.getUrl().put("key", apiKey);
        request.getUrl().put("language", language); // it can be overridden
        return request;
    }

    public interface OnGetIconListener {
        void onGetIcon(Bitmap bitmap);
    }

    /**
     * Get icon bitmap from <code>place.icon</code> in the main thread.
     */
    public Bitmap getIconBitmap(final Place place) throws IOException {
        assert place != null;

        if (place.icon != null) {
            final HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(place.icon));
            final HttpResponse response = request.execute();
            if (response.isSuccessStatusCode()) {
                return BitmapFactory.decodeStream(response.getContent());
            }
        }
        return null;
    }

    /**
     * Get icon bitomap from <code>place.icon</code> in background.
     */
    public void getIconBitmap(final Place place, final OnGetIconListener listener) {
        if (place.icon != null) {
            final Bitmap cached = cache.get(place.icon);
            if (cached != null) {
                listener.onGetIcon(Bitmap.createBitmap(cached));
                return;
            }

            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {
                        return getIconBitmap(place);
                    } catch (IOException e) {
                        Log.w("GooglePlaces", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        cache.put(place.icon, Bitmap.createBitmap(bitmap));
                        listener.onGetIcon(bitmap);
                    }
                }
            }.execute((Void) null);
        }
    }

    public interface ResultListener<T> {
        void onComplete(T result);
    }

    public interface ErrorListener {
        void onError(RequestError error);
    }

    // default ErrorListener which throws RuntimeException on errors
    private static class DefaultErrorListener implements ErrorListener {
        private static DefaultErrorListener instance = new DefaultErrorListener();

        public static DefaultErrorListener getInstance() {
            return instance;
        }

        @Override
        public void onError(RequestError error) {
            throw new RuntimeException("Uncaught RequestError", error);
        }
    }
}
