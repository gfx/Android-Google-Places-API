package com.github.gfx.googleplaces.test;

import android.test.AndroidTestCase;

import com.github.gfx.googleplaces.GooglePlaces;
import com.github.gfx.googleplaces.Place;
import com.github.gfx.googleplaces.PlaceListOrder;
import com.github.gfx.googleplaces.RequestError;
import com.github.gfx.googleplaces.SearchResult;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SearchResultTest extends AndroidTestCase {
    private GooglePlaces client;

    private InputStream getMockContentByUrl(final String url) throws IOException {
        int id;

        if (url.contains("/nearbysearch/")) {
            id = R.raw.nearbysearch;
        } else if (url.contains("/textsearch/")) {
            id = R.raw.textsearch;
        } else if (url.contains("/radarsearch/")) {
            id = R.raw.radarsearch;
        } else {
            throw new RuntimeException("Unexpected url:" + url);
        }

        return getContext().getResources().openRawResource(id);
    }

    @Override
    public void setUp() {
        final HttpTransport transport = new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(final String method, final String url) throws IOException {
                return new MockLowLevelHttpRequest() {
                    @Override
                    public LowLevelHttpResponse execute() throws IOException {
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                        response.setStatusCode(200);
                        response.setContentType(Json.MEDIA_TYPE);
                        response.setContent(getMockContentByUrl(url));
                        return response;
                    }
                };
            }
        };

        final String apiKey = "XXX";
        client = new GooglePlaces(apiKey, transport);
    }


    public void testNearbySearch() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        client.nearBySearch(0, 0, 1, false)
            .setLanguage("ja")
            .setTypes("cafe", "bank")
            .setRankBy(PlaceListOrder.PROMINENCE)
            .setOpenNow(true)
            .setMaxPrice(4)
            .setMinPrice(0)
            .setZagatSelected(true)
            .get(new GooglePlaces.ResultListener<SearchResult>() {
                @Override
                public void onComplete(SearchResult placeList) {
                    assertNotNull(placeList);
                    assertEquals(20, placeList.size());

                    int n = 0;
                    for (Place place : placeList) {
                        n++;
                    }
                    assertEquals(20, n);

                    latch.countDown();
                }
            })
            .setErrorListener(new GooglePlaces.ErrorListener() {
                @Override
                public void onError(RequestError error) {
                    fail("not reached");
                }
            });

        boolean timedOut = latch.await(10, TimeUnit.SECONDS);
        assertTrue("nearBySearch calls its callback", timedOut);
    }

    public void testTextSearch() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        client.textSearchBuilder("foo", false)
            .setOpenNow(true)
            .setLanguage("ja")
            .setMinPrice(0)
            .setMaxPrice(4)
            .get(new GooglePlaces.ResultListener<SearchResult>() {
                @Override
                public void onComplete(SearchResult placeList) {
                    assertNotNull(placeList);
                    assertEquals(4, placeList.size());

                    latch.countDown();
                }
            });

        boolean timedOut = latch.await(10, TimeUnit.SECONDS);
        assertTrue("textSearch calls its callback", timedOut);
    }

    public void testRadarSearch() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        client.radarSearchBuilder(0, 0, 100, false)
            .setOpenNow(true)
            .setLanguage("ja")
            .setMinPrice(0)
            .setMaxPrice(4)
            .get(new GooglePlaces.ResultListener<SearchResult>() {
                @Override
                public void onComplete(SearchResult placeList) {
                    assertNotNull(placeList);
                    assertEquals(200, placeList.size());

                    latch.countDown();
                }
            });

        boolean timedOut = latch.await(10, TimeUnit.SECONDS);
        assertTrue("textSearch calls its callback", timedOut);
    }
}
