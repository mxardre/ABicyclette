package fr.rdre.maa.osmtest;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Sprite;
import com.mapbox.mapboxsdk.annotations.SpriteFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    public MainFragment() {
        // Required empty public constructor
    }

    Marker markerPosition=null;
    SpriteFactory spriteFactory =null;
    Sprite icon =null;
    Drawable drawable = null;
    ImageButton imageButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        LocationManager locationManager;


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        /** Create a mapView and give it some properties */
        MapView mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setCenterCoordinate(new LatLng(48.866667, 2.333333));
        mapView.setZoomLevel(11);

        mapView.onCreate(savedInstanceState);


        /* location manager */
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        //find the last position
        for (String provider : locationManager.getProviders(true)) {
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
                break;
            }
        }

        //check if provider are enabled
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage("gps_network_not_enabled");
            dialog.setPositiveButton("open_location_settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }


        //Request position from enable provider
        if (gps_enabled == true) {
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        } else if (network_enabled == true) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        } else {
            Context context = getContext();
            CharSequence text = "No location available";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        imageButton = (ImageButton) view.findViewById(R.id.CenterPosition);
        if (imageButton != null)
        {
            addListenerOnButton(imageButton, location);
        }
        else
        {
            Context context = getContext();
            CharSequence text = "Image button null";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        return view;

    }


    private LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {

            String message = String.format(
                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );


//            Context context = getContext();
            //Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            double lat=location.getLatitude();
            double lng=location.getLongitude();


            View view= getView().findViewById(R.id.fragment_main);
            MapView mapView = (MapView) view.findViewById(R.id.mapview);


            if (markerPosition != null)
            {markerPosition.remove();}


            /** Use SpriteFactory, Drawable, and Sprite to load our marker icon
             * and assign it to a marker*/
            spriteFactory = mapView.getSpriteFactory();
            drawable= ContextCompat.getDrawable(getActivity(), R.drawable.ic_my_location_black_24dp);
            icon = spriteFactory.fromDrawable(drawable);

            markerPosition= mapView.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .snippet("You are here")
                    .icon(icon));

        }



        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    public void addListenerOnButton( ImageButton imageButton, final Location location) {

        imageButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                View view= getView().findViewById(R.id.fragment_main);
                MapView mapView = (MapView) view.findViewById(R.id.mapview);
                mapView.setZoomLevel(14);
                mapView.setCenterCoordinate(new LatLng(location.getLatitude(),location.getLongitude()));

                Context context = getContext();
                CharSequence text = "Update position";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

        });
    }
}
