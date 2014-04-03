package com.nvanbenschoten.motion.motion_sample;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nvanbenschoten.motion.ParallaxImageView;


public class ParallaxActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parallax);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ParallaxFragment())
                    .commit();
        }
    }

    /**
     * A fragment containing a simple parallax image view.
     */
    public static class ParallaxFragment extends Fragment {

        private ParallaxImageView mBackground;

        public ParallaxFragment() { }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_parallax, container, false);
            if (rootView == null) return null;

            mBackground = (ParallaxImageView) rootView.findViewById(android.R.id.background);

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mBackground.setIntensity(1.3f);
        }

        @Override
        public void onResume() {
            super.onResume();
            mBackground.registerSensorManager((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE));
        }

        @Override
        public void onPause() {
            super.onPause();
            mBackground.unregisterSensorManager();
        }

    }
}
