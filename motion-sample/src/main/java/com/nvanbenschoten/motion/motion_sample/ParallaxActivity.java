package com.nvanbenschoten.motion.motion_sample;

import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

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
        private boolean mParallaxSet = true;
        private boolean mPortraitLock = true;

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

            setCurrentImage();

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Adjust the Parallax forward tilt adjustment
            mBackground.setForwardTiltOffset(.35f);

            // Set SeekBar to change parallax intensity
            mSeekBar.setMax(10);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mBackground.setParallaxIntensity(1f + ((float) progress) / 80);
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

            if (mParallaxSet)
                mBackground.registerSensorManager();
        }

        @Override
        public void onPause() {
            mBackground.unregisterSensorManager();
            super.onPause();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.parallax, menu);

            // Add parallax toggle
            final Switch mParallaxToggle = new Switch(getActivity());
            mParallaxToggle.setPadding(0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()), 0);
            mParallaxToggle.setChecked(mParallaxSet);
            mParallaxToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mBackground.registerSensorManager();
                    } else {
                        mBackground.unregisterSensorManager();
                    }

                    mParallaxSet = isChecked;
                }
            });
            MenuItem switchItem = menu.findItem(R.id.action_parallax);
            if (switchItem != null)
                switchItem.setActionView(mParallaxToggle);

            // Set lock/ unlock orientation text
            if (mPortraitLock) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                MenuItem orientationItem = menu.findItem(R.id.action_portrait);
                if (orientationItem != null)
                    orientationItem.setTitle(R.string.action_unlock_portrait);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_switch:
                    mCurrentImage ++;
                    mCurrentImage %= 3;
                    setCurrentImage();
                    return true;

                case R.id.action_portrait:
                    if (mPortraitLock) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        item.setTitle(getString(R.string.action_lock_portrait));
                    } else {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        item.setTitle(getString(R.string.action_unlock_portrait));
                    }

                    mPortraitLock = !mPortraitLock;
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private void setCurrentImage() {
            if (mCurrentImage == 0) {
                mBackground.setImageResource(R.drawable.background_pond);
            } else if (mCurrentImage == 1) {
                mBackground.setImageDrawable(getResources().getDrawable(R.drawable.background_city));
            } else {
                mBackground.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.background_rocket_small));
            }
        }
    }
}
