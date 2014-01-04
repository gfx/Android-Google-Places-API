package com.github.gfx.googleplaces.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.gfx.googleplaces.GooglePlaces;
import com.github.gfx.googleplaces.Place;
import com.github.gfx.googleplaces.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    private static final String TAG = "ItemDetailFragment";
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private PlaceListAdapter adapter;

    private GooglePlaces places;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        places = new GooglePlaces(getResources().getString(R.string.GoogleApiKey))
            .setLanguage("ja");

        adapter = new PlaceListAdapter();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            final ApiType apiType = ApiType.valueOf(getArguments().getString(ARG_ITEM_ID));
            Log.i(TAG, "start GooglePlaces request with apiType=" + apiType);

            switch (apiType) {
                case TEXT_SEARCH:
                    places.textSearchBuilder("寿司", false).get(new GooglePlaces.ResultListener<SearchResult>() {
                        @Override
                        public void onComplete(SearchResult result) {
                            adapter.setPlaceList(result.results);
                        }
                    });

                    break;
                case NEARBY_SEARCH:
                    places.nearBySearch(35.68, 139.76, 500, false).get(new GooglePlaces.ResultListener<SearchResult>() {
                        @Override
                        public void onComplete(SearchResult result) {
                            adapter.setPlaceList(result.results);
                        }
                    });
                    break;
                case RADAR_SEARCH:
                    places.radarSearchBuilder(35.68, 139.76, 500, false).get(new GooglePlaces.ResultListener<SearchResult>() {
                        @Override
                        public void onComplete(SearchResult result) {
                            adapter.setPlaceList(result.results);
                        }
                    });
                    break;
                case DETAILS:
                    break;
            }

        } else {
            Log.wtf("ItemDetailFragment", "No API type supplied!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        final ListView rootView = (ListView) inflater.inflate(R.layout.fragment_item_detail, container, false);
        assert rootView != null;
        rootView.setAdapter(adapter);
        return rootView;
    }

    private class PlaceListAdapter extends BaseAdapter {
        private List<Place> placeList;

        public PlaceListAdapter() {
            this.placeList = new ArrayList<>();
        }

        public void setPlaceList(List<Place> placeList) {
            this.placeList = placeList;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return this.placeList.size();
        }

        @Override
        public Place getItem(int position) {
            return this.placeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.cell_place, null);
            }

            final Place place = getItem(position);

            final ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
            final TextView nameView = (TextView) convertView.findViewById(R.id.name);
            final TextView addressView = (TextView) convertView.findViewById(R.id.address);

            iconView.setImageResource(R.drawable.ic_noimage);
            places.getIconBitmap(place, new GooglePlaces.OnGetIconListener() {
                @Override
                public void onGetIcon(Bitmap bitmap) {
                    iconView.setImageBitmap(bitmap);
                }
            });

            if (place.name != null) {
                nameView.setText(place.name);
            }

            if (place.formatted_address != null) {
                addressView.setText(place.formatted_address);
            } else if (place.vicinity != null) {
                addressView.setText(place.vicinity);
            } else if (place.geometry != null) {
                addressView.setText(place.geometry.location.lat + "," + place.geometry.location.lng);
            }

            return convertView;
        }
    }
}
