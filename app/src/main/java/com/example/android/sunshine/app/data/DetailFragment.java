package com.example.android.sunshine.app.data;

import android.content.Intent;
import android.database.Cursor;
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
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
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
    
    private ShareActionProvider mShareActionProvider;
    private static final int MY_LOADER_ID = 666;

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;


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
        if (mForecastStr != null ) {
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


                Intent intent = getActivity().getIntent();


                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        intent.getData(),        // Table to query
                        FORECAST_COLUMNS,       // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order

                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()){
            return;
        }
        boolean isMetric = Utility.isMetric(getActivity());
        String date = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        String desc = data.getString(COL_WEATHER_DESC);
        //mForecastStr = (date + " - "+desc + " - " + high +"/"+low);

        //TextView tv = (TextView) getView().findViewById(R.id.detail_text);
        //tv.setText(mForecastStr);
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
        icon.setImageResource(R.drawable.ic_launcher);
        //set wind
        //float windSpeed = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
        //float windDirection = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));

//        float windSpeed = Weath
//        TextView windView = (TextView) getView().findViewById(R.id.list_item_wind);
//        windView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDirection));






        if (mShareActionProvider!=null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
