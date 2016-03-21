package fr.rdre.maa.osmtest;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        MapView mapView = null;
        LocationManager locationManager;
        double lat = 0;
        double lng = 0;



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        /** Create a mapView and give it some properties */
        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setCenterCoordinate(new LatLng(48.866667, 2.333333));
        mapView.setZoomLevel(11);

        mapView.onCreate(savedInstanceState);




        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);



        Context context = getContext();


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

            mapView.setCenterCoordinate(new LatLng(lat, lng));
            mapView.setZoomLevel(14);




        }



        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };



}
