/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String POSITION = "position";
    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    Callback mCallback;
    int mPosition;
    int mSavedPosition;
    boolean mUseTodayLayout;
    TextView mEmptyListView;


    private ForecastAdapter mForecastAdapter;
    private static final int MY_LOADER_ID = 666;


    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_HUMIDITY = 9;
    static final int COL_WIND_SPEED = 10;
    static final int COL_WIND_DIRECTION = 11;
    static final int COL_PRESSURE = 12;

    public ForecastFragment() {
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {


        //INITIALIZE LOADER HERE
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, mSavedPosition);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.SetUseTodayLayout(mUseTodayLayout);

        if (savedInstanceState != null) {
            mSavedPosition = savedInstanceState.getInt(POSITION);
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mEmptyListView = (TextView) rootView.findViewById(R.id.empty_listview_forecast);
        listView.setEmptyView(mEmptyListView);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    //update location setting
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    //update Uri with new location setting and date received from cursor
                    Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE));
                    //call the main activities Callback function
                    ((Callback) getActivity()).onItemSelected(weatherUri);
                    mPosition = position;
                    mSavedPosition = position;


                }

            }
        });

        return rootView;
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (loaderID) {
            case MY_LOADER_ID:
                // Returns a new CursorLoader

                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                String locationSetting = Utility.getPreferredLocation(getActivity());
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());

                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        weatherForLocationUri,        // Table to query
                        FORECAST_COLUMNS,           //mProjection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        sortOrder             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);


        //PERFORM ANY UI UPDATES HERE

        ListView lv = (ListView) getView().findViewById(R.id.listview_forecast);
        if (mPosition != ListView.INVALID_POSITION) {
            lv.smoothScrollToPosition(mSavedPosition);
        }
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAG, "There was a change to shared preference: " + key);
        if (key.equals(String.valueOf(R.string.pref_location_status_key))){
            updateEmptyView();
        }

    }


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callback");
        }
    }

    //Method to set whether or not to use the today layout as a seperate layout in the list view
    //dont do in two pane mode
    public void SetUseTodayLayout(boolean useTodayView) {
        mUseTodayLayout = useTodayView;
        if (mForecastAdapter != null) {
            mForecastAdapter.SetUseTodayLayout(useTodayView);
        }
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (null != mForecastAdapter) {
            Cursor c = mForecastAdapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);

                //Data URI to simply open map at coordinates
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                //If you want to drop a pin at coords and label it as weather map location, use this format
                //Uri geoLocation = Uri.parse("geo:0,0?q=" + posLat + "," + posLong+"(Weather Map Location)");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    /**
     * Updates the empty list with a message indicating why the list is not populated
     */
    private void updateEmptyView() {

        if (mForecastAdapter.getCount() == 0) {
            if (mEmptyListView != null) {
                String message = null;
                @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity().getApplicationContext());
                switch(location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_OK:
                        message = String.valueOf(R.string.empty_listview_text);
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = String.valueOf(R.string.empty_forecast_list_server_down);
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = String.valueOf(R.string.empty_forecast_list_server_error);
                        break;
                    default:
                        if(!Utility.isNetworkAvailable(getActivity().getApplicationContext())){
                            message = String.valueOf(R.string.empty_listview_text_nonetwork);
                        }
                        break;
                }
                mEmptyListView.setText(message);
            }
        }
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
