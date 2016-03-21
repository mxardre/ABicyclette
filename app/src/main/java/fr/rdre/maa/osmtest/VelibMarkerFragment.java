package fr.rdre.maa.osmtest;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class VelibMarkerFragment extends Fragment {


    MapView mapView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_velib_marker, container, false);

        String LOG_TAG = "ONCREATE";

        mapView = (MapView) view.findViewById(R.id.velibMarker);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setCenterCoordinate(new LatLng(48.866667, 2.333333));
        mapView.setZoomLevel(11);

        mapView.onCreate(savedInstanceState);


        StationPosition stationPosition = new StationPosition();
        stationPosition.execute();

        Context context = getContext();
        Toast.makeText(context, "VelibMarker", Toast.LENGTH_LONG).show();

        return view;
    }


    public static String locationStringFromLocation(final Location location) {
        return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
    }

    public class StationPosition extends AsyncTask<Void, Void, ArrayList<Location>> {

        protected ArrayList<Location> getStationPositionFromJSON(String stationJsonStr)
                throws JSONException {

            ArrayList<Location> listVelibPosition = new ArrayList<Location>();

            String LOG_TAG = "GETJSONPOSITION";

            // These are the names of the JSON objects that need to be extracted.
            final String ODP_nhits = "nhits";
            final String ODP_records = "records";
            final String ODP_geometry = "geometry";
            final String ODP_coordinates = "coordinates";
            final String ODP_fields = "fields";
            final String ODP_available_stands = "available_bike_stands";
            final String ODP_available_bikes = "available_bikes";
            final String ODP_status = "status";


            JSONObject stationJson = new JSONObject(stationJsonStr);
            JSONArray stationArray = stationJson.getJSONArray(ODP_records);

            double lat = 0;
            double lon = 0;

            for (int i = 0; i < stationArray.length(); i++) {
                //Get JSON object of the station
                JSONObject stationRecords = stationArray.getJSONObject(i);
                JSONObject stationGeometry = stationRecords.getJSONObject(ODP_geometry);
                JSONObject stationFields = stationRecords.getJSONObject(ODP_fields);

                int availableBikes = stationFields.getInt(ODP_available_bikes);
                int availableStands = stationFields.getInt(ODP_available_stands);
                String stationStatus = stationFields.getString(ODP_status);

                //Log.v(LOG_TAG, "List Marker snippet " + "Bikes: "+ String.valueOf(availableBikes)+"\n"+ "Stands: "+ String.valueOf(availableStands));


                JSONArray coordinateObject = stationGeometry.getJSONArray(ODP_coordinates);

                lat = coordinateObject.getDouble(0);
                lon = coordinateObject.getDouble(1);

                Location tmpLoc = new Location("dummyprovider"); //needs to be created at each iteration to not overright the table with the same element.
                tmpLoc.setLongitude(lon);
                tmpLoc.setLatitude(lat);

                listVelibPosition.add(tmpLoc);

                //add the marker in background to avoid the freezing of the app
                LatLng position = new LatLng(lon, lat);
                mapView.addMarker(new MarkerOptions()
                                .position(position)
                        .title(String.valueOf(stationStatus))
                        .snippet("Bikes: "+ String.valueOf(availableBikes)+"\n"+ "Stands: "+ String.valueOf(availableStands))
                );


                //  Log.v(LOG_TAG, "List Marker lat lon " + String.valueOf(listVelibPosition.get(j).getLatitude()) +" "+String.valueOf(listVelibPosition.get(j).getLongitude()));


            }

            return listVelibPosition;
        }


        @Override
        protected ArrayList<Location> doInBackground(Void... params) {

            String LOG_TAG = "ASYNC TASK";

            ArrayList<Location> listVelibPosition;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String stationJsonStr;

            try {
                // Construct the URL for the ABicylette query
                // Possible parameters are avaiable at OWM's forecast API page, at
                final String VELIB_BASE_URL = "http://opendata.paris.fr/api/records/1.0/search/?dataset=stations-velib-disponibilites-en-temps-reel&rows=2000&start=1&sort=-number&facet=banking&facet=bonus&facet=status&facet=contract_name";
                Uri builtUri = Uri.parse(VELIB_BASE_URL).buildUpon().build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                //Read the stream
                InputStream stream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (stream == null) {
                    // Nothing to do.b
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(stream));

                String line;

                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                stationJsonStr = buffer.toString();
                Log.v(LOG_TAG, "OPEN_DATA_API: " + stationJsonStr);

            } catch (IOException e) {
                Log.e("Tag", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }

            }

            try {
                return getStationPositionFromJSON(stationJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            }

            // This will only happen if there was an error getting or parsing the position.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Location> listVelibPosition) {


        }

    }


}
