package com.nvanbenschoten.motion.motion_sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nvanbenschoten.motion.ParallaxImageView;

import java.util.Random;


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
        private Runnable mRunnable;

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
            mBackground.setIntensity(1.5f);

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Random r = new Random();
                    float x = r.nextInt(2) == 0 ? -1 : 1;
                    float y = r.nextInt(2) == 0 ? -1 : 1;

                    mBackground.setTranslate(0, 0);
                    new Handler().postDelayed(mRunnable, 2000);
                }
            };

            new Handler().postDelayed(mRunnable, 2000);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.parallax, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    mBackground.setIntensity(1.1f);
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }
}
