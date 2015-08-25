package com.nvanbenschoten.motion;

import android.hardware.SensorEvent;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
 * Copyright 2015 Nathan VanBenschoten
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
public class SamsungRotationMatrixBugTest {

    @Test
    public void testRotationVectorForEventWithoutAccuracyValue() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        SensorEvent event = TestUtils.mockSensorEvent(new float[]{0.1f, 0.2f, 0.3f, 0.4f});

        float[] rotationVector = sensorInterpreter.getRotationVectorFromSensorEvent(event);
        assertNotNull(rotationVector);
        assertTrue(rotationVector.length <= 4);
    }

    @Test
    public void testRotationVectorForEventWithAccuracyValue() throws Exception {
        SensorInterpreter sensorInterpreter = new SensorInterpreter();
        SensorEvent event = TestUtils.mockSensorEvent(new float[]{0.1f, 0.2f, 0.3f, 0.4f, -1});

        float[] rotationVector = sensorInterpreter.getRotationVectorFromSensorEvent(event);
        assertNotNull(rotationVector);
        assertTrue(rotationVector.length <= 4);
    }

}
