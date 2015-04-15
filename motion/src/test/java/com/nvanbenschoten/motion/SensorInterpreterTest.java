package com.nvanbenschoten.motion;

import android.content.Context;
import android.hardware.SensorEvent;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SensorInterpreterTest {

    private final float ACCEPTABLE_FLOAT_DELTA = 0.0001f;

    @Test
    public void testInterpretSensorEventPortraitWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setForwardTiltOffset(0);

        Context context = mockRotationContext(Surface.ROTATION_0);
        SensorEvent event = mockSensorEvent(3);
        event.values[0] = 10f;
        event.values[1] = 25f;
        event.values[2] = 40f;

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{10f, -0.55555f, -0.88888f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorEventLandscapeRightWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setForwardTiltOffset(0);

        Context context = mockRotationContext(Surface.ROTATION_90);
        SensorEvent event = mockSensorEvent(3);
        event.values[0] = 10f;
        event.values[1] = 25f;
        event.values[2] = 40f;

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{10f, 0.88888f,  -0.55555f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorEventLandscapeLeftWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setForwardTiltOffset(0);

        Context context = mockRotationContext(Surface.ROTATION_270);
        SensorEvent event = mockSensorEvent(3);
        event.values[0] = 10f;
        event.values[1] = 25f;
        event.values[2] = 40f;

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{10f, -0.88888f, 0.55555f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorEventUpsideDownWithNoOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setForwardTiltOffset(0);

        Context context = mockRotationContext(Surface.ROTATION_180);
        SensorEvent event = mockSensorEvent(3);
        event.values[0] = 10f;
        event.values[1] = 25f;
        event.values[2] = 40f;

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{10f, 0.55555f, 0.88888f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorEventPortraitWithOffset() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(2);
        sensorInterpreter.setForwardTiltOffset(0.1f);

        Context context = mockRotationContext(Surface.ROTATION_0);
        SensorEvent event = mockSensorEvent(3);
        event.values[0] = 10f;
        event.values[1] = 25f;
        event.values[2] = 40f;

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertArrayEquals(new float[]{10f, -0.75555f, -0.88888f}, interpreted, ACCEPTABLE_FLOAT_DELTA);
    }

    @Test
    public void testInterpretSensorClampValues() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        sensorInterpreter.setTiltSensitivity(1.5f);
        sensorInterpreter.setForwardTiltOffset(0);

        Context context = mockRotationContext(Surface.ROTATION_180);
        SensorEvent event = mockSensorEvent(3);
        event.values[0] = 10f;
        event.values[1] = 65f;
        event.values[2] = -80f;

        float[] interpreted = sensorInterpreter.interpretSensorEvent(context, event);

        assertEquals("positive numbers over 1 clamp to 1", 1f, interpreted[1], ACCEPTABLE_FLOAT_DELTA);
        assertEquals("negative numbers under -1 clamp to -1", -1f, interpreted[2], ACCEPTABLE_FLOAT_DELTA);
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

    private SensorEvent mockSensorEvent(int size) throws Exception {
        SensorEvent event = Mockito.mock(SensorEvent.class);

        Field valuesField = SensorEvent.class.getField("values");
        valuesField.setAccessible(true);
        valuesField.set(event, new float[size]);

        return event;
    }

}