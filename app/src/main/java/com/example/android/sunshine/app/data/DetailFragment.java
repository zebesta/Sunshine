package com.example.android.sunshine.app.data;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;

/**
 * Created by chrissebesta on 3/3/16.
 * Populates the detailed weather report once a day is selected from the main weather menu.
 * Introduces additional information including wind speed, wind direction, humidity, and pressure.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] DETAIL_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_CONDITION_ID = 5;
    static final int COL_HUMIDITY = 6;
    static final int COL_WIND_SPEED = 7;
    static final int COL_DEGREES = 8;
    static final int COL_PRESSURE = 9;
    public static final String DETAIL_URI = "Detail URI index";

    private ShareActionProvider mShareActionProvider;
    private static final int MY_LOADER_ID = 666;

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;
    private Uri mUri;

    public static DetailFragment newInstance(int index) {
        DetailFragment f = new DetailFragment();

//        supply index input as an argument
//        Bundle args = new Bundle();
//        args.putInt("index", index);
//        f.setArguments(args);

        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get the arguments that the fragment was initialized with
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
        }
        //TODO: Move the text view and image view id finds here so that they are only found once instead of every time the data is updated.

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (id) {
            case MY_LOADER_ID:
                // Returns a new CursorLoader


                //mUri is now coming from the arguments passed from main activity
                if (mUri != null) {

                    return new CursorLoader(
                            getActivity(),   // Parent activity context
                            mUri, //intent.getData(),        // Table to query
                            DETAIL_COLUMNS,       // Projection to return
                            null,            // No selection clause
                            null,            // No selection arguments
                            null             // Default sort order

                    );
                } else {
                    return null;
                }
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        if(!data.moveToFirst()){
//            return;
//        }
        if (data != null && data.moveToFirst()) {
            boolean isMetric = Utility.isMetric(getActivity());

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            String day = Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE));
            String date = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
            String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
            String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            String desc = data.getString(COL_WEATHER_DESC);
            float humidity = data.getFloat(COL_HUMIDITY);
            float pressure = data.getFloat(COL_PRESSURE);
            float windSpeed = data.getFloat(COL_WIND_SPEED);
            float windDirection = data.getFloat(COL_DEGREES);


            Log.d(LOG_TAG, "The raw wind speed is: " + windSpeed + " and the wind direction is: " + windDirection);
            Log.d(LOG_TAG, "The humidity is: " + humidity + " and the pressure is: " + pressure);
            //String pressure = "SO PRESSURE RIGHT NOW";
            //mForecastStr = (date + " - "+desc + " - " + high +"/"+low);

            //TextView tv = (TextView) getView().findViewById(R.id.detail_text);
            //tv.setText(mForecastStr);

            //set day
            TextView dayView = (TextView) getView().findViewById(R.id.list_item_day_textview);
            dayView.setText(day);
            //set date
            TextView dateView = (TextView) getView().findViewById(R.id.list_item_date_textview);
            dateView.setText(date);
            //set high
            TextView highView = (TextView) getView().findViewById(R.id.list_item_high_textview);
            highView.setText(high);
            //set low
            TextView lowView = (TextView) getView().findViewById(R.id.list_item_low_textview);
            lowView.setText(low);
            //set description
            TextView descView = (TextView) getView().findViewById(R.id.list_item_forecast_textview);
            descView.setText(desc);
            //set icon
            ImageView icon = (ImageView) getView().findViewById(R.id.list_item_icon);
            icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            icon.setContentDescription(desc);

            //set wind
            //TODO: Need to actually pull wind speed and direction from the API request (as well as humidity and Pressure
            //float windSpeed = 10;//data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            //float windDirection = 10;//data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
            TextView windView = (TextView) getView().findViewById(R.id.list_item_wind);
            windView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDirection));

            //set humidity
            TextView humidityView = (TextView) getView().findViewById(R.id.list_item_humidity);
            humidityView.setText("Humidity: " + humidity);

            //set pressure
            TextView pressureView = (TextView) getView().findViewById(R.id.list_item_pressure);
            pressureView.setText("Pressure: " + pressure);


            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void onLocationChanged(String location) {
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
        }
    }
}
