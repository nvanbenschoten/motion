package com.nvanbenschoten.motion;

import android.content.Context;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;

/*
 * Copyright 2014 Nathan VanBenschoten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ParallaxImageView extends ImageView implements SensorEventListener {

    private static final String TAG = ParallaxImageView.class.getName();

    /**
     * The intensity of the parallax effect, giving the perspective of depth.
     */
    private float mParallaxIntensity = 1.0f;

    /**
     * The sensitivity the parallax effect has towards tilting.
     */
    private float mTiltSensitivity = 2.0f;

    /**
     * The forward tilt offset adjustment to counteract a natural forward phone tilt.
     */
    private float mForwardTiltOffset = 0.3f;

    // Instance variables used during matrix manipulation.
    private SensorManager mSensorManager;
    private Matrix mTranslationMatrix;
    private float mXTranslation;
    private float mYTranslation;
    private float mXOffset;
    private float mYOffset;

    public ParallaxImageView(Context context) {
        super(context);
        init();
    }

    public ParallaxImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParallaxImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initiates the ParallaxImageView with any given attributes.
     */
    private void init() {
        // Sets scale type
        setScaleType(ScaleType.MATRIX);

        // Instantiate future objects
        mTranslationMatrix = new Matrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        configureMatrix();
    }

    /**
     * Sets the intensity of the parallax effect. The stronger the effect, the more distance
     * the image will have to move around.
     *
     * @param parallaxIntensity the new intensity
     */
    public void setParallaxIntensity(float parallaxIntensity) {
        if (parallaxIntensity < 1)
            throw new IllegalArgumentException("Parallax effect must have a intensity of 1.0 or greater");

        mParallaxIntensity = parallaxIntensity;
        configureMatrix();
    }

    /**
     * Sets the parallax tilt sensitivity for the image view. The stronger the sensitivity,
     * the more a given tilt will adjust the image and the smaller needed tilt to reach the
     * image bounds.
     *
     * @param sensitivity the new tilt sensitivity
     */
    public void setTiltSensitivity(float sensitivity) {
        mTiltSensitivity = sensitivity;
    }

    /**
     * Sets the forward tilt offset dimension, allowing for the image to be
     * centered while the phone is "naturally" tilted forwards.
     *
     * @param forwardTiltOffset the new tilt forward adjustment
     */
    public void setForwardTiltOffset(float forwardTiltOffset) {
        if (Math.abs(forwardTiltOffset) > 1)
            throw new IllegalArgumentException("Parallax forward tilt offset must be less than or equal to 1.0");

        mForwardTiltOffset = forwardTiltOffset;
    }

    /**
     * Sets the image view's translation coordinates. These values must be between -1 and 1,
     * representing the transaction percentage from the center.
     *
     * @param x the horizontal translation
     * @param y the vertical translation
     */
    private void setTranslate(float x, float y) {
        if (Math.abs(x) > 1 || Math.abs(y) > 1)
            throw new IllegalArgumentException("Parallax effect cannot translate more than 100% of its off-screen size");

        mXTranslation = x * mXOffset;
        mYTranslation = y * mYOffset;

        configureMatrix();
    }

    /**
     * Configures the ImageView's imageMatrix to allow for movement of the
     * source image.
     */
    private void configureMatrix() {
        if (getDrawable() == null || getWidth() == 0 || getHeight() == 0) return;

        int dWidth = getDrawable().getIntrinsicWidth();
        int dHeight = getDrawable().getIntrinsicHeight();
        int vWidth = getWidth();
        int vHeight = getHeight();

        float scale;
        float dx = 0, dy = 0;

        if (dWidth * vHeight > vWidth * dHeight) {
            scale = (float) vHeight / (float) dHeight;
            mXOffset = (vWidth - dWidth * scale * mParallaxIntensity) * 0.5f;
            mYOffset = (vHeight - dHeight * scale * mParallaxIntensity) * 0.5f;
        } else {
            scale = (float) vWidth / (float) dWidth;
            mXOffset = (vWidth - dWidth * scale * mParallaxIntensity) * 0.5f;
            mYOffset = (vHeight - dHeight * scale * mParallaxIntensity) * 0.5f;
        }

        dx = mXOffset + mXTranslation;
        dy = mYOffset + mYTranslation;

        mTranslationMatrix.set(getImageMatrix());
        mTranslationMatrix.setScale(mParallaxIntensity * scale, mParallaxIntensity * scale);
        mTranslationMatrix.postTranslate(dx, dy);
        setImageMatrix(mTranslationMatrix);
    }

    /**
     * Registers a sensor manager with the parallax ImageView. Should be called in onResume
     * from an Activity or Fragment.
     *
     * @param sensorManager a SensorManager instance.
     */
    @SuppressWarnings("deprecation")
    public void registerSensorManager(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Unregisters the ParallaxImageView's SensorManager. Should be called in onPause from
     * an Activity or Fragment to avoid continuing sensor usage.
     */
    public void unregisterSensorManager() {
        if (mSensorManager == null) return;

        mSensorManager.unregisterListener(this);
        mSensorManager = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0 || event.values[1] == 0) return;

        // Set degrees to a percent out of 1.0
        event.values[1] /= 90f;
        event.values[2] /= 90f;

        // Get the current screen rotation
        if (getContext() == null) return;
        final int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();

        // Adjust for forward tilt based on screen orientation
        switch (rotation) {
            case Surface.ROTATION_90:
                event.values[2] -= mForwardTiltOffset;
                if (event.values[2] < -1) event.values[2] += 2;
                break;

            case Surface.ROTATION_180:
                event.values[1] -= mForwardTiltOffset;
                if (event.values[1] < -1) event.values[1] += 2;
                break;

            case Surface.ROTATION_270:
                event.values[2] += mForwardTiltOffset;
                if (event.values[2] > 1) event.values[2] -= 2;
                break;

            default:
                event.values[1] += mForwardTiltOffset;
                if (event.values[1] > 1) event.values[1] -= 2;
                break;
        }

        // Adjust for tile sensitivity
        event.values[1] *= mTiltSensitivity;
        event.values[2] *= mTiltSensitivity;

        // Clamp values to image bounds
        if (event.values[1] > 1) event.values[1] = 1f;
        if (event.values[1] < -1) event.values[1] = -1f;

        if (event.values[2] > 1) event.values[2] = 1f;
        if (event.values[2] < -1) event.values[2] = -1f;

        // Set translation based on screen orientation
        switch (rotation) {
            case Surface.ROTATION_90:
                setTranslate(-event.values[1], event.values[2]);
                break;

            case Surface.ROTATION_180:
                setTranslate(event.values[1], event.values[2]);
                break;

            case Surface.ROTATION_270:
                setTranslate(event.values[1], -event.values[2]);
                break;

            default:
                setTranslate(-event.values[2], -event.values[1]);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

}
