package fr.rdre.maa.osmtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

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
import java.util.List;
public class MainFragment extends Fragment implements OnMapReadyCallback {


    MapView mapView = null;
    private MapboxMap mapboxMap;
    public Marker markerUpdate=null;
    public List<Marker> markerList=new ArrayList<>();
    double zoom=10.;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_velib_marker, container, false);

        mapView = (MapView) view.findViewById(R.id.velibMarker);
        mapView.getMapAsync(this);

        mapView = (MapView) view.findViewById(R.id.velibMarker);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.onCreate(savedInstanceState);


        StationPosition stationPosition = new StationPosition();
        stationPosition.execute();

        //Context context = getContext();
        //Toast.makeText(context, "VelibMarker", Toast.LENGTH_SHORT).show();


        return view;
    }



    @Override
    public void onMapReady(final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        //48.85341, 2.3488 paris position
        final LatLng[] latLng = {new LatLng(48.85341, 2.3488)};

        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(latLng[0])
                .zoom(zoom)
                .build());



        // Load Marker
        new StationPosition().execute();

        // Put listener on marker
        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                Log.v("Listener", "STATION NUMBER " + marker.getTitle());
                markerUpdate=marker;

                new StationUpdate().execute();

                markerUpdate.showInfoWindow(mapboxMap, mapView);

                return true;
            }
        });

        mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {


            @Override
            public void onCameraChange(CameraPosition position) {

                if (zoom * 0.99 > position.zoom || position.zoom > zoom * 1.01) {
                    zoom = position.zoom;
                    if (markerList.size() > 0) {

                        //get the actual marker
                        Marker marker = markerList.get(0);

                        // Create an Icon object for the marker to use
                        //the icon displays the number of bike and the number stand available
                        IconFactory iconFactory = IconFactory.getInstance(getContext());

                        Bitmap bitmap= writeOnDrawable(R.drawable.ic_room_black_24dp,"Bikes");
                        Drawable drawable= new BitmapDrawable(getResources(),bitmap);
                        int width= (int) (drawable.getIntrinsicWidth() * (zoom-10)*.22);
                        int height= (int) (drawable.getIntrinsicHeight() * (zoom-10)*.22);

                        drawable=resizeDrawable(drawable, width, height);

                        Log.v("ZOOM", String.valueOf(zoom)+" "+String.valueOf((zoom-10)*.2));
                        Log.v("ZOOM markerList", String.valueOf(markerList.size()));
                        //Log.v("ZOOM drawable", String.valueOf(drawable.getIntrinsicWidth())+" "+String.valueOf(drawable.getIntrinsicHeight()));

                        if (width>0 && height>0)
                        {
                            //Bitmap scale=Bitmap.createScaledBitmap(bitmap, width , height, true);
                            //ScaleDrawable scaledrawable = new ScaleDrawable(drawable, 0, width, height);


                            //Drawable d = new BitmapDrawable(getResources(), scaledrawable);

                            Icon icon= iconFactory.fromDrawable(drawable);

                            //icon = iconFactory.fromBitmap(bhalfsize);


                            for (int i = 0; i < markerList.size(); i++) {


                                Marker markerUpdated = markerList.get(i);
                                markerUpdated.setIcon(icon);


                                if(zoom>14) {

                                    LatLng mapLocation = mapboxMap.getCameraPosition().target;
                                    double mapLat = mapLocation.getLatitude();
                                    double mapLon = mapLocation.getLongitude();

                                    LatLng markerLocation = markerUpdated.getPosition();
                                    double markerLat = markerLocation.getLatitude();
                                    double markerLon = markerLocation.getLongitude();

                                    //affiche les snippets si les marker sont a cote du centre de la carte
                                    //calcul la distance entre les points
                                    float [] dist={1000};
                                    Location.distanceBetween(markerLat, markerLon, mapLat, mapLon, dist) ;


                                    if(dist[0]<500)
                                    {

                                        markerUpdate=markerUpdated;
                                        new StationUpdate().execute();

                                    }
                                }
                                if(zoom<14) {
                                    markerUpdated.hideInfoWindow();
                                }

                            }

                        }
                        else
                        {
                            for (int i = 0; i < markerList.size(); i++) {

                                Marker markerOld = markerList.get(i);
                                markerOld.remove();
                                Log.v("ZOOM", "remove marker");

                            }
                        }



                    }
                }
            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    public class StationUpdate extends AsyncTask<Void, Void, String>
    {


        @Override
        protected String doInBackground(Void... args) {



            //String formated in Json containing the query
            String stationUpdateJson="";
            String station_number=markerUpdate.getTitle();
            String snippetStr="";

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String LOG_TAG = "ASYNC TASK 2";


            Log.v(LOG_TAG, "STATION NUMBER " +station_number);


            try {
                // Construct the URL for the ABicylette query
                // The data are refresh in real time
                String VELIB_BASE_URL = "http://public.opendatasoft.com/api/records/1.0/search/?dataset=jcdecaux_bike_data&facet=banking&facet=bonus&facet=status&facet=contract_name&refine.contract_name=Paris";
                VELIB_BASE_URL = VELIB_BASE_URL +  "&refine.number=" + station_number;

                Uri builtUri = Uri.parse(VELIB_BASE_URL).buildUpon().build();

                URL url = new URL(builtUri.toString());


                Log.v(LOG_TAG, "Built URI 2 " + builtUri.toString());

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
                stationUpdateJson = buffer.toString();
                Log.v(LOG_TAG, "OPEN_DATA_API: " + stationUpdateJson);

            } catch (IOException e) {
                Log.e("Tag", "Error ", e);
                // If the code didn't successfully get the station data, there's no point in attemping
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


            //update the marker in background to avoid the freezing of the app

            // These are the names of the JSON objects that need to be extracted.
            final String ODP_records = "records";
            final String ODP_fields = "fields";
            final String ODP_available_stands = "available_bike_stands";
            final String ODP_available_bikes = "available_bikes";
            final String ODP_status = "status";


            JSONObject stationJson = null;
            JSONArray stationArray = null;

            Log.v("EXECUTE 2", "before try");

            try {
                stationJson = new JSONObject(stationUpdateJson);

                stationArray = stationJson.getJSONArray(ODP_records);


                //Get JSON object of the station
                JSONObject stationRecords = stationArray.getJSONObject(0);
                JSONObject stationFields = stationRecords.getJSONObject(ODP_fields);


                int availableBikes = stationFields.getInt(ODP_available_bikes);
                int availableStands = stationFields.getInt(ODP_available_stands);
                String stationStatus = stationFields.getString(ODP_status);

                Log.v("EXECUTE 2", "SNIPPET " +
                        " Bikes : Stands\n"+ String.valueOf(availableBikes) +
                        "  " + String.valueOf(availableStands));




                snippetStr=" Bikes \\ Stands\n"+
                        "   "+ String.valueOf(availableBikes) +  "          " + String.valueOf(availableStands);





            } catch (JSONException e) {
                Log.v("EXECUTE 2", "ERROR UPDATE SNIPPET");
                e.printStackTrace();
            }



            return snippetStr;
        }

        @Override
        protected void onPostExecute(String snippetStr)
        {
            markerUpdate.setSnippet(snippetStr);

        }


    }

    public class StationPosition extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {

            String LOG_TAG = "ASYNC TASK";


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String stationJsonStr;

            try {
                // Construct the URL for the ABicylette query
                // The data are refresh every minute in this api. To get live data use jcdecaux developper api
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
                // If the code didn't successfully get the station data, there's no point in attemping
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

            // This will only happen if there was an error getting or parsing the position.
            return stationJsonStr;
        }

        @Override
        protected void onPostExecute(String stationJsonStr) {


            //add the marker in background to avoid the freezing of the app

            // These are the names of the JSON objects that need to be extracted.
            final String ODP_records = "records";
            final String ODP_geometry = "geometry";
            final String ODP_coordinates = "coordinates";
            final String ODP_fields = "fields";
            final String ODP_number = "number";


            JSONObject stationJson = null;
            JSONArray stationArray = null;

            Log.v("EXECUTE", "before try");

            try {
                stationJson = new JSONObject(stationJsonStr);
                stationArray = stationJson.getJSONArray(ODP_records);

                double lat = 0;
                double lon = 0;
                int stationNumber = 0;

                if( markerList==null){}
                else
                {markerList.clear();}

                for (int i = 0; i < stationArray.length(); i++) {


                    //Get JSON object of the station
                    JSONObject stationRecords = stationArray.getJSONObject(i);
                    JSONObject stationGeometry = stationRecords.getJSONObject(ODP_geometry);
                    JSONObject stationFields = stationRecords.getJSONObject(ODP_fields);

                    stationNumber = stationFields.getInt(ODP_number);

                    //int availableStands = stationFields.getInt(ODP_available_stands);
                    //String stationStatus = stationFields.getString(ODP_status);

                    JSONArray coordinateObject = stationGeometry.getJSONArray(ODP_coordinates);

                    lat = coordinateObject.getDouble(0);
                    lon = coordinateObject.getDouble(1);



                    LatLng position = new LatLng(lon, lat);

                    // Create an Icon object for the marker to use
                    IconFactory iconFactory = IconFactory.getInstance(getContext());


                    Drawable drawable= ContextCompat.getDrawable(getContext(), R.drawable.ic_room_black_24dp);

                    drawable=resizeDrawable(drawable,2,2);
                    Icon icon= iconFactory.fromDrawable(drawable);


                    Marker markerTmp = mapboxMap.addMarker(new MarkerOptions()
                                    .setIcon(icon)
                                    .position(position)
                                    .title(String.valueOf(stationNumber))
                                    .snippet("Open : ..." +
                                            "\n Bikes : ..." +
                                            "\n Stands : ...")
                    );

                    markerList.add(markerTmp);

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

    }


    private Drawable resizeDrawable(Drawable image, int width, int height) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width, height, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    public Bitmap writeOnDrawable(int drawableId, String text){

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Bitmap bitmap=Bitmap.createBitmap(bm.getWidth()*2,bm.getHeight()*2,Bitmap.Config.ARGB_8888);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(70);
        paint.setTextAlign(Paint.Align.CENTER);


        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = bitmap.getWidth()/2;
        int y = bitmap.getHeight()/2;


        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, x, y, paint);
        canvas.drawBitmap(bm,bitmap.getWidth()/4,bitmap.getHeight()/2,null);

        return bitmap;
    }
}
