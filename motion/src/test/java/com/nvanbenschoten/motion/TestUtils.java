package com.nvanbenschoten.motion;

import android.content.Context;
import android.hardware.SensorEvent;
import android.view.Display;
import android.view.WindowManager;

import org.mockito.Mockito;

import java.lang.reflect.Constructor;

import static org.mockito.Mockito.when;

public class TestUtils {

    /**
     * Creates a mock {@link Context} with the given rotation.
     *
     * @param rotation the screens orientation
     * @return the mock Context
     */
    public static Context mockRotationContext(int rotation) {
        Display display = Mockito.mock(Display.class);
        when(display.getRotation()).thenReturn(rotation);

        WindowManager windowManager = Mockito.mock(WindowManager.class);
        when(windowManager.getDefaultDisplay()).thenReturn(display);

        Context context = Mockito.mock(Context.class);
        when(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager);
        return context;
    }

    /**
     * Creates a mock {@link SensorEvent} with the provided values.
     *
     * @param values the values
     * @return the mock SensorEvent
     * @throws Exception
     */
    public static SensorEvent mockSensorEvent(float[] values) throws Exception {
        Constructor<SensorEvent> c = SensorEvent.class.getDeclaredConstructor(int.class);
        c.setAccessible(true);

        SensorEvent event = c.newInstance(values.length);
        System.arraycopy(values, 0, event.values, 0, values.length);

        return event;
    }

}
