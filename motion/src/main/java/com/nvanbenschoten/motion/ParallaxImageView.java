package com.nvanbenschoten.motion;

import android.content.Context;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
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

    private SensorManager mSensorManager;

    private float mIntensity = 1f;
    private float mTiltSensitivity = 2.5f;
    private float mTiltForwardAdjustment = .3f;

    private Matrix mTranslationMatrix;
    private float mXTranslation = 0;
    private float mYTranslation = 0;
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

    private void init() {
        // Sets scale type
        setScaleType(ScaleType.MATRIX);

        // Instantiate future objects
        mTranslationMatrix = new Matrix();
    }

    public void setIntensity(float intensity) {
        if (intensity <= 1) return;

        mIntensity = intensity;
        configureMatrix();
    }

    public void setTiltSensitivity(float sensitivity) {
        mTiltSensitivity = sensitivity;
    }

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
            mXOffset = (vWidth - dWidth * scale * mIntensity) * 0.5f;
            mYOffset = (vHeight - dHeight * scale * mIntensity) * 0.5f;
        } else {
            scale = (float) vWidth / (float) dWidth;
            mXOffset = (vWidth - dWidth * scale * mIntensity) * 0.5f;
            mYOffset = (vHeight - dHeight * scale * mIntensity) * 0.5f;
        }

        dx = mXOffset + mXTranslation;
        dy = mYOffset + mYTranslation;

        mTranslationMatrix.set(getImageMatrix());
        mTranslationMatrix.setScale(mIntensity * scale, mIntensity * scale);
        mTranslationMatrix.postTranslate(dx, dy);
        setImageMatrix(mTranslationMatrix);
    }

    public void setTranslate(float x, float y) {
        if (Math.abs(x) > 1 || Math.abs(y) > 1) return;

        mXTranslation = x * mXOffset;
        mYTranslation = y * mYOffset;
        configureMatrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        configureMatrix();
    }

    @SuppressWarnings("deprecation")
    public void registerSensorManager(SensorManager sensorManager) {
        mSensorManager = sensorManager;
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void unregisterSensorManager() {
        mSensorManager.unregisterListener(this);
        mSensorManager = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0 || event.values[1] == 0) return;

        event.values[1] /= 90f;
        event.values[2] /= 90f;

        event.values[1] += mTiltForwardAdjustment;
        if (event.values[1] > 1) event.values[1] -= 2;

        event.values[1] *= mTiltSensitivity;
        if (event.values[1] > 1) event.values[1] = 1f;
        if (event.values[1] < -1) event.values[1] = -1f;

        event.values[2] *= mTiltSensitivity;
        if (event.values[2] > 1) event.values[2] = 1f;
        if (event.values[2] < -1) event.values[2] = -1f;

        setTranslate(-event.values[2], -event.values[1]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
