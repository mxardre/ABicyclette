package fr.rdre.maa.osmtest;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreferedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreferedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferedFragment extends Fragment {

    private ArrayAdapter<String> mListStationAdapter;

    public PreferedFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        //create a list containing the favorite station
        String[] data = {
                "Station 1 Bikes :..., Stand :...",
                "Station 2 Bikes :..., Stand :...",
        };

        List<String> stationPrefered = new ArrayList<String>(Arrays.asList(data));

        // Now that we have some dummy station data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source  and
        // use it to populate the ListView it's attached to.
        mListStationAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_station, // The name of the layout ID.
                        R.id.list_item_station_textview, // The ID of the textview to populate.
                        stationPrefered);

        View rootView = inflater.inflate(R.layout.fragment_prefered, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_station);
        listView.setAdapter(mListStationAdapter);

        return rootView;
    }


}
