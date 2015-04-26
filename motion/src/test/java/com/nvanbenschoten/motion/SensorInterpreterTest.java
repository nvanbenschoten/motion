package com.nvanbenschoten.motion;

import android.content.Context;
import android.hardware.SensorEvent;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
public class SensorInterpreterTest {

    private final float ACCEPTABLE_FLOAT_DELTA = 0.0001f;

    @Test
    public void testInterpretSensorEventPortraitWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setTargetVector(new float[]{0.5f, 0.6f, 0.7f});

        Context context = mockRotationContext(Surface.ROTATION_0);
        SensorEvent event = mockSensorEvent(new float[]{0.7f, 0.7f, 0.7f});

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{-0.19003f, 0.21814f, 0.02382f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorEventLandscapeRightWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setTargetVector(new float[]{0.5f, 0.6f, 0.7f});

        Context context = mockRotationContext(Surface.ROTATION_90);
        SensorEvent event = mockSensorEvent(new float[]{0.7f, 0.7f, 0.7f});

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{1.0f, 0.04103f, -0.12279f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorEventLandscapeLeftWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setTargetVector(new float[]{0.5f, 0.6f, 0.7f});

        Context context = mockRotationContext(Surface.ROTATION_270);
        SensorEvent event = mockSensorEvent(new float[]{0.7f, 0.7f, 0.7f});

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{-0.92730f, -0.04103f, 0.12279f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorEventUpsideDownWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setTargetVector(new float[]{0.5f, 0.6f, 0.7f});

        Context context = mockRotationContext(Surface.ROTATION_180);
        SensorEvent event = mockSensorEvent(new float[]{0.7f, 0.7f, 0.7f});

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{1.0f, -0.21815f, -0.02382f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorClampValues() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(1.5f);
        sensorInterpreter.setTargetVector(new float[]{0f, 0f, 0f});

        Context context = mockRotationContext(Surface.ROTATION_0);
        SensorEvent event = mockSensorEvent(new float[]{3.1f, 0f, 3.1f});

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertEquals("positive numbers over 1 clamp to 1", 1, interpreted[0], ACCEPTABLE_FLOAT_DELTA);
        assertEquals("negative numbers under -1 clamp to -1", -1, interpreted[2], ACCEPTABLE_FLOAT_DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidTiltSensitivity() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegativeTiltSensitivity() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(-1);
    }

    private Context mockRotationContext(int rotation) {
        Display display = Mockito.mock(Display.class);
        when(display.getRotation()).thenReturn(rotation);

        WindowManager windowManager = Mockito.mock(WindowManager.class);
        when(windowManager.getDefaultDisplay()).thenReturn(display);

        Context context = Mockito.mock(Context.class);
        when(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager);
        return context;
    }

    private SensorEvent mockSensorEvent(float[] values) throws Exception {
        Constructor<SensorEvent> c = SensorEvent.class.getDeclaredConstructor(int.class);
        c.setAccessible(true);

        SensorEvent event = c.newInstance(values.length);
        System.arraycopy(values, 0, event.values, 0, values.length);

        return event;
    }

}