package fr.rdre.maa.osmtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PreferedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefered);

        if(savedInstanceState==null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, new PreferedFragment() )
                    .commit();
        }

    }

}
