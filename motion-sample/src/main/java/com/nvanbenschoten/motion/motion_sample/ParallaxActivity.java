package com.nvanbenschoten.motion.motion_sample;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

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
     * A fragment containing a simple parallax image view and a SeekBar to adjust the
     * parallax intensity.
     */
    public static class ParallaxFragment extends Fragment {

        private ParallaxImageView mBackground;
        private SeekBar mSeekBar;

        private int mCurrentImage;

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
            mSeekBar = (SeekBar) rootView.findViewById(android.R.id.progress);

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Adjusts the Parallax tilt sensitivity
            mBackground.setTiltSensitivity(2.3f);

            // Adjust the Parallax forward tilt adjustment
            mBackground.setForwardTiltOffset(.35f);

            // Set SeekBar to change parallax intensity
            mSeekBar.setMax(10);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mBackground.setParallaxIntensity(1f + ((float) progress) / 10);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            mSeekBar.setProgress(1);
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

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.parallax, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_switch:
                    if (mCurrentImage == 0) {
                        mBackground.setImageResource(R.drawable.background_ski);
                        mCurrentImage = 1;
                    } else {
                        mBackground.setImageResource(R.drawable.background_city);
                        mCurrentImage = 0;
                    }
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }
}
