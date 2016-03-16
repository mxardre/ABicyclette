package fr.rdre.maa.osmtest;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Sprite;
import com.mapbox.mapboxsdk.annotations.SpriteFactory;
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


public class MainActivity extends ActionBarActivity {

    private MapView mapView = null;
    private LocationManager locationManager;
    private double lat = 0;
    private double lng = 0;
    private Marker markerPosition=null;
    private SpriteFactory spriteFactory =null;
    private Sprite icon =null;
    private Drawable drawable = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /** Create a mapView and give it some properties */
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setCenterCoordinate(new LatLng(48.866667, 2.333333));
        mapView.setZoomLevel(11);


        mapView.addMarker(new MarkerOptions()
                .position(new LatLng(48.866667, 2.333333))
                .title("Hello World!")
                .snippet("Welcome to my marker."));

        mapView.onCreate(savedInstanceState);

        /** Use SpriteFactory, Drawable, and Sprite to load our marker icon
         * and assign it to a marker*/
        spriteFactory = mapView.getSpriteFactory();
        drawable=ContextCompat.getDrawable(this, R.drawable.ic_my_location_black_24dp);
        icon = spriteFactory.fromDrawable(drawable);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

    }

    private  void initializeSpriteFactory() {


    }

    // Define a listener that responds to location updates

    private final LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {

            lat=location.getLatitude();
            lng=location.getLongitude();


            if (markerPosition != null)
            {markerPosition.remove();}


            markerPosition = mapView.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .snippet("You are here")
                            .icon(icon));


            String message = String.format(
                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );

            Context context = getApplicationContext();
            Toast.makeText(context,message, Toast.LENGTH_LONG).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_location) {

            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,  1000, 0, locationListener);

            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);

            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            lat = loc.getLatitude();
            lng = loc.getLongitude();
            return true;
        }

        if (id == R.id.action_removeMarker) {
            if (markerPosition != null)
            {markerPosition.remove();}
            return true;

        }

        if (id == R.id.action_station) {
            StationLocation stationLocation=new StationLocation();
            stationLocation.execute();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause()  {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }



    public class StationLocation extends AsyncTask<Void, Void, Location[]>
    {

        private Location[] getStationPositionFromJSON(String stationJsonStr)
            throws JSONException{

            String LOG_TAG="GETJSONPOSITION";

            // These are the names of the JSON objects that need to be extracted.
            final String ODP_nhits = "nhits";
            final String ODP_records = "records";
            final String ODP_geometry = "geometry";
            final String ODP_coordinates = "coordinates";


            JSONObject stationJson= new JSONObject(stationJsonStr);
            JSONArray stationArray = stationJson.getJSONArray(ODP_records);

            int nhitsStation = stationJson.getInt(ODP_nhits);


            Location[] resultLoc = new Location[nhitsStation];
            Location tmpLoc=null;
            double lat =0;
            double lon =0;

            for(int i=0; i<stationArray.length(); i++)
            {
                //Get JSON object of the station
                JSONObject stationRecords= stationArray.getJSONObject(i);
                JSONObject stationGeometry = stationRecords.getJSONObject(ODP_geometry);
                JSONArray coordinateObject = stationGeometry.getJSONArray(ODP_coordinates);

                lat = coordinateObject.getDouble(0);
                lon = coordinateObject.getDouble(1);


                tmpLoc.setLatitude(lat);
                tmpLoc.setLongitude(lon);

                resultLoc[i] = tmpLoc;
            }

            return resultLoc;
        }

        @Override
        protected Location[] doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String stationJsonStr ;

            String LOG_TAG="FetchLocation";

            try
            {
                // Construct the URL for the ABicylette query
                // Possible parameters are avaiable at OWM's forecast API page, at
                final String VELIB_BASE_URL = "http://opendata.paris.fr/api/records/1.0/search/?dataset=stations-velib-disponibilites-en-temps-reel&rows=2000&start=1&sort=-number&facet=banking&facet=bonus&facet=status&facet=contract_name";
                Uri builtUri = Uri.parse(VELIB_BASE_URL).buildUpon().build();

                URL url = new URL(builtUri.toString());
                Log.v( LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                //Read the stream
                InputStream stream=urlConnection.getInputStream();
                StringBuffer buffer=new StringBuffer();
                if (stream == null) {
                    // Nothing to do.b
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(stream));

                String line;

                while ((line=reader.readLine())!=null)
                {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length()==0)
                {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                stationJsonStr = buffer.toString();
               // Log.v(LOG_TAG, "OPEN_DATA_API: " + stationJsonStr);

            }
            catch (IOException e)
            {
                Log.e("Tag", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            }
            finally
            {
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

            try
            {
                return getStationPositionFromJSON(stationJsonStr);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(Location[] result)
        {
            String LOG_TAG="EXECUTES";

            if (result!=null)
            {
                for (int i=0; i<result.length; i++)
                {
//!!!ca doit surement pas etre le bon moyen de mettre des marker sur une map
                    //l'app bug
                    mapView.addMarker(new MarkerOptions()
                            .position(new LatLng(result[i].getLatitude(), result[i].getLongitude())));
                    Log.v(LOG_TAG, "COORDINATES: " + result[0]);
                }


            }
        }

    }


}

