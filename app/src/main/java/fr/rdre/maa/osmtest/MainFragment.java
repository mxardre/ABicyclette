package fr.rdre.maa.osmtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
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

import static java.lang.Math.max;

public class MainFragment extends Fragment implements OnMapReadyCallback {


    MapView mapView = null;
    private MapboxMap mapboxMap;
    public Marker markerUpdate=null;
    public List<MarkerOptions> markerList=new ArrayList<>();
    public List<String> dataList=new ArrayList<>();
    double zoom=10.;
    LatLng target=null;
    Drawable drawableSrc=null;

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

        Bitmap bitmap = writeOnDrawable(R.drawable.ic_room_black_24dp, " ");
        drawableSrc=new BitmapDrawable(getResources(), bitmap);
        Log.v("ZOOM src", String.valueOf(drawableSrc.getIntrinsicWidth()) + " " + String.valueOf(drawableSrc.getIntrinsicHeight()));

        //Context context = getContext();
        //Toast.makeText(context, "VelibMarker", Toast.LENGTH_SHORT).show();

        //drawableSrc= ContextCompat.getDrawable(getContext(), R.drawable.ic_room_black_24dp);


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



                new StationUpdate().execute();


                Intent detailIntent=new Intent(getActivity(),PreferedActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,marker.getTitle());

                startActivity(detailIntent);

                //markerUpdate.showInfoWindow(mapboxMap, mapView);

                return true;
            }
        });

        mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {


            @Override
            public void onCameraChange(CameraPosition position) {


                if (target == null) {
                    target = position.target;
                }


                double targetLat = target.getLatitude();
                double targetLon = target.getLongitude();

                Log.v("TARGET", String.valueOf(zoom));

                if (zoom * 0.99 > position.zoom || position.zoom > zoom * 1.01 || targetLat * 0.9999 > position.target.getLatitude() || position.target.getLatitude() > targetLat * 1.0001 || targetLon * 0.9999 > position.target.getLongitude() || position.target.getLongitude() > targetLon * 1.0001) {
                    Log.v("TARGET", String.valueOf(targetLat) + " " + String.valueOf(targetLon) + " " + String.valueOf(position.target.getLatitude()) + " " + String.valueOf(position.target.getLongitude()));



                    zoom = position.zoom;
                    target = position.target;

                    if(zoom>14) {

                        if (markerList.size() > 0) {

                            //get the actual marker

                            // Create an Icon object for the marker to use
                            //the icon displays the number of bike and the number stand available

                            //Bitmap bitmap= writeOnDrawable(R.drawable.ic_room_black_24dp,"Bikes");
                            //Drawable drawable= new BitmapDrawable(getResources(),bitmap);

                            int width = (int) (drawableSrc.getIntrinsicWidth() * (zoom - 10) * .22);
                            int height = (int) (drawableSrc.getIntrinsicHeight() * (zoom - 10) * .22);


                            Log.v("ZOOM icon", String.valueOf(width) + " " + String.valueOf(height));

                            //Log.v("ZOOM drawable1", String.valueOf(drawable.getIntrinsicWidth())+" "+String.valueOf(drawable.getIntrinsicHeight()));

                            if (width > 0 && height > 0) {

                                if (width > 180) {
                                    width = 180;
                                }
                                if (height > 155) {
                                    height = 155;
                                }

                                Log.v("ZOOM", String.valueOf(zoom) + " " + String.valueOf((zoom - 10) * .2));
                                Log.v("ZOOM markerList", String.valueOf(markerList.size()));


                                Drawable drawable = resizeDrawable(drawableSrc, width, height);
                                IconFactory iconFactory = IconFactory.getInstance(getContext());
                                Icon iconSrc = iconFactory.fromDrawable(drawable);
                                Icon icon = iconSrc;

                                //Bitmap scale=Bitmap.createScaledBitmap(bitmap, width , height, true);
                                //ScaleDrawable scaledrawable = new ScaleDrawable(drawable, 0, width, height);


                                //Drawable d = new BitmapDrawable(getResources(), scaledrawable);


                                //icon = iconFactory.fromBitmap(bhalfsize);
                                VisibleRegion visibleRegion = mapboxMap.getProjection().getVisibleRegion();
                                double latNorth = visibleRegion.latLngBounds.getLatNorth();
                                double latSouth = visibleRegion.latLngBounds.getLatSouth();
                                double lonWest = visibleRegion.latLngBounds.getLonWest();
                                double lonEast = visibleRegion.latLngBounds.getLonEast();


                                for (int i = 0; i < markerList.size(); i++) {

                                    MarkerOptions markerUpdated = markerList.get(i);

                                    double lat = markerUpdated.getPosition().getLatitude();
                                    double lon = markerUpdated.getPosition().getLongitude();

//                                mapboxMap.removeMarker(markerUpdated.getMarker());

                                    markerUpdated.getMarker().remove(); //enleve les marker et n'ecrit que ceux qui sont dans la region visible

                                    if (latSouth < lat && lat < latNorth && lonWest < lon && lon < lonEast)//marche pas dans l'hemisphere sud
                                    {
                                        if (i == 0) {
                                            StationUpdate stationUpdate = new StationUpdate();
                                            stationUpdate.execute();
                                        }

                                        drawable = null;
                                        Bitmap bitmap = writeOnDrawable(R.drawable.ic_room_black_24dp, dataList.get(i));
                                        drawable = new BitmapDrawable(getResources(), bitmap);

                                        drawable = resizeDrawable(drawable, width, height);

                                        Log.v("ZOOM drawable2", String.valueOf(drawable.getIntrinsicWidth()) + " " + String.valueOf(drawable.getIntrinsicHeight()) + " " + dataList.get(i));

                                        //icon =new StationUpdate().execute(String.valueOf(i));

                                        //redefinie l'icon
                                        icon = iconFactory.fromDrawable(drawable);


                                        markerUpdated.setIcon(icon);
                                        mapboxMap.addMarker(markerUpdated);
                                        //markerUpdated.setTopOffsetPixels(-drawable.getIntrinsicHeight()/2);
                                    }


                                }

                            }


                        }
                    }
                    else
                    {
                        if (markerList.size() > 0) {
                            for (int i = 0; i < markerList.size(); i++) {

                                MarkerOptions markerUpdated = markerList.get(i);

                                markerUpdated.getMarker().remove(); //enleve les marker si le zoom est trop loin
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


    public class StationUpdate extends AsyncTask<Void, Void, String> {


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


                final String ODP_available_stands = "available_bike_stands";
                final String ODP_available_bikes = "available_bikes";

                for (int i = 0; i < stationArray.length(); i++) {


                    //Get JSON object of the station
                    JSONObject stationRecords = stationArray.getJSONObject(i);
                    JSONObject stationGeometry = stationRecords.getJSONObject(ODP_geometry);
                    JSONObject stationFields = stationRecords.getJSONObject(ODP_fields);

                    stationNumber = stationFields.getInt(ODP_number);

                    int availableStands = stationFields.getInt(ODP_available_stands);
                    int availableBikes = stationFields.getInt(ODP_available_bikes);

                    //String stationStatus = stationFields.getString(ODP_status);

                    JSONArray coordinateObject = stationGeometry.getJSONArray(ODP_coordinates);

                    lat = coordinateObject.getDouble(0);
                    lon = coordinateObject.getDouble(1);



                    LatLng position = new LatLng(lon, lat);

                    // Create an Icon object for the marker to use
                    IconFactory iconFactory = IconFactory.getInstance(getContext());


                    //Drawable drawable= ContextCompat.getDrawable(getContext(), R.drawable.ic_room_black_24dp);

                    Drawable drawable=resizeDrawable(drawableSrc, 2, 2);
                    Icon icon= iconFactory.fromDrawable(drawable);


                    MarkerOptions markerOptions=new MarkerOptions()
                            .setIcon(icon)
                            .position(position)
                            .title(String.valueOf(stationNumber))
                            .snippet("Open : ..." +
                                    "\n Bikes : ..." +
                                    "\n Stands : ...");



                    Log.v("STATION UPDATE", String.valueOf(i) + " " + String.valueOf(dataList.size()) + " " + String.valueOf(markerList.size()));


                    dataList.set(i, availableStands + "/" + availableBikes);


                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


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

                if( dataList==null){}
                else
                {dataList.clear();}

                final String ODP_available_stands = "available_bike_stands";
                final String ODP_available_bikes = "available_bikes";

                for (int i = 0; i < stationArray.length(); i++) {


                    //Get JSON object of the station
                    JSONObject stationRecords = stationArray.getJSONObject(i);
                    JSONObject stationGeometry = stationRecords.getJSONObject(ODP_geometry);
                    JSONObject stationFields = stationRecords.getJSONObject(ODP_fields);

                    stationNumber = stationFields.getInt(ODP_number);

                    int availableStands = stationFields.getInt(ODP_available_stands);
                    int availableBikes = stationFields.getInt(ODP_available_bikes);

                    //String stationStatus = stationFields.getString(ODP_status);

                    JSONArray coordinateObject = stationGeometry.getJSONArray(ODP_coordinates);

                    lat = coordinateObject.getDouble(0);
                    lon = coordinateObject.getDouble(1);



                    LatLng position = new LatLng(lon, lat);

                    // Create an Icon object for the marker to use
                    IconFactory iconFactory = IconFactory.getInstance(getContext());


                    //Drawable drawable= ContextCompat.getDrawable(getContext(), R.drawable.ic_room_black_24dp);

                    Drawable drawable=resizeDrawable(drawableSrc, 2, 2);
                    Icon icon= iconFactory.fromDrawable(drawable);


                    MarkerOptions markerOptions=new MarkerOptions()
                            .setIcon(icon)
                            .position(position)
                            .title(String.valueOf(stationNumber))
                            .snippet("Open : ..." +
                                    "\n Bikes : ..." +
                                    "\n Stands : ...");

                    mapboxMap.addMarker(markerOptions);

                    dataList.add(availableStands+"/"+availableBikes);
                    markerList.add(markerOptions);

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

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(70);
        paint.setTextAlign(Paint.Align.CENTER);


        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Rect boundsRef = new Rect();
        paint.getTextBounds("99/99", 0, "99/99".length(), boundsRef);

        Bitmap bitmap=Bitmap.createBitmap(max(boundsRef.width(),bm.getWidth()),boundsRef.height()+bm.getHeight(),Bitmap.Config.ARGB_8888);

        int x = bitmap.getWidth()/2;
        int y = boundsRef.height();


        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, x, y, paint);
        canvas.drawBitmap(bm,bitmap.getWidth()/2-bm.getWidth()/2,boundsRef.height(),null);

        return bitmap;
    }
}
