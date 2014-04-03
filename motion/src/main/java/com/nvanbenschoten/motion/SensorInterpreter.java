package com.nvanbenschoten.motion;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorInterpreter {

    public final static int SENSOR_UNAVAILABLE = -1;

    // raw inputs from Android sensors
    float m_Norm_Gravity;           // length of raw gravity vector received in onSensorChanged(...).  NB: should be about 10
    float[] m_NormGravityVector;    // Normalised gravity vector, (i.e. length of this vector is 1), which points straight up into space
    float m_Norm_MagField;          // length of raw magnetic field vector received in onSensorChanged(...).
    float[] m_NormMagFieldValues;   // Normalised magnetic field vector, (i.e. length of this vector is 1)

    // accuracy specifications. SENSOR_UNAVAILABLE if unknown, otherwise SensorManager.SENSOR_STATUS_UNRELIABLE, SENSOR_STATUS_ACCURACY_LOW, SENSOR_STATUS_ACCURACY_MEDIUM or SENSOR_STATUS_ACCURACY_HIGH
    int m_GravityAccuracy;          // accuracy of gravity sensor
    int m_MagneticFieldAccuracy;    // accuracy of magnetic field sensor

    // values calculated once gravity and magnetic field vectors are available
    float[] m_NormEastVector;       // normalised cross product of raw gravity vector with magnetic field values, points east
    float[] m_NormNorthVector;      // Normalised vector pointing to magnetic north
    boolean m_OrientationOK;        // set true if m_azimuth_radians and m_pitch_radians have successfully been calculated following a call to onSensorChanged(...)
    float m_azimuth_radians;        // angle of the device from magnetic north
    float m_pitch_radians;          // tilt angle of the device from the horizontal.  m_pitch_radians = 0 if the device if flat, m_pitch_radians = Math.PI/2 means the device is upright.
    float m_pitch_axis_radians;     // angle which defines the axis for the rotation m_pitch_radians

    public SensorInterpreter(SensorManager sm, SensorEventListener parent) {
        m_NormGravityVector = m_NormMagFieldValues = null;
        m_NormEastVector = new float[3];
        m_NormNorthVector = new float[3];
        m_OrientationOK = false;
    }

    public int Register(SensorManager sensorManager, SensorEventListener sensorEventListener) {
        m_NormGravityVector = new float[3];
        m_NormMagFieldValues = new float[3];
        m_OrientationOK = false;
        int count = 0;
        Sensor SensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (SensorGravity != null) {
            sensorManager.registerListener(sensorEventListener, SensorGravity, SensorManager.SENSOR_DELAY_FASTEST);
            m_GravityAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
            count++;
        } else {
            m_GravityAccuracy = SENSOR_UNAVAILABLE;
        }
        Sensor SensorMagField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (SensorMagField != null) {
            sensorManager.registerListener(sensorEventListener, SensorMagField, SensorManager.SENSOR_DELAY_FASTEST);
            m_MagneticFieldAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
            count++;
        } else {
            m_MagneticFieldAccuracy = SENSOR_UNAVAILABLE;
        }
        return count;
    }

    public void Unregister() {
        m_NormGravityVector = m_NormMagFieldValues = null;
        m_OrientationOK = false;
    }

    public void onSensorChanged(SensorEvent event) {
        int SensorType = event.sensor.getType();
        switch(SensorType) {
            case Sensor.TYPE_GRAVITY:
                if (m_NormGravityVector == null) m_NormGravityVector = new float[3];
                System.arraycopy(event.values, 0, m_NormGravityVector, 0, m_NormGravityVector.length);
                m_Norm_Gravity = (float)Math.sqrt(m_NormGravityVector[0]*m_NormGravityVector[0] + m_NormGravityVector[1]*m_NormGravityVector[1] + m_NormGravityVector[2]*m_NormGravityVector[2]);
                for(int i=0; i < m_NormGravityVector.length; i++) m_NormGravityVector[i] /= m_Norm_Gravity;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (m_NormMagFieldValues == null) m_NormMagFieldValues = new float[3];
                System.arraycopy(event.values, 0, m_NormMagFieldValues, 0, m_NormMagFieldValues.length);
                m_Norm_MagField = (float)Math.sqrt(m_NormMagFieldValues[0]*m_NormMagFieldValues[0] + m_NormMagFieldValues[1]*m_NormMagFieldValues[1] + m_NormMagFieldValues[2]*m_NormMagFieldValues[2]);
                for(int i=0; i < m_NormMagFieldValues.length; i++) m_NormMagFieldValues[i] /= m_Norm_MagField;
                break;
        }
        if (m_NormGravityVector != null && m_NormMagFieldValues != null) {
            // first calculate the horizontal vector that points due east
            float East_x = m_NormMagFieldValues[1]*m_NormGravityVector[2] - m_NormMagFieldValues[2]*m_NormGravityVector[1];
            float East_y = m_NormMagFieldValues[2]*m_NormGravityVector[0] - m_NormMagFieldValues[0]*m_NormGravityVector[2];
            float East_z = m_NormMagFieldValues[0]*m_NormGravityVector[1] - m_NormMagFieldValues[1]*m_NormGravityVector[0];
            float norm_East = (float)Math.sqrt(East_x * East_x + East_y * East_y + East_z * East_z);
            if (m_Norm_Gravity * m_Norm_MagField * norm_East < 0.1f) {  // Typical values are  > 100.
                m_OrientationOK = false; // device is close to free fall (or in space?), or close to magnetic north pole.
            } else {
                m_NormEastVector[0] = East_x / norm_East; m_NormEastVector[1] = East_y / norm_East; m_NormEastVector[2] = East_z / norm_East;

                // next calculate the horizontal vector that points due north
                float M_dot_G = (m_NormGravityVector[0] *m_NormMagFieldValues[0] + m_NormGravityVector[1]*m_NormMagFieldValues[1] + m_NormGravityVector[2]*m_NormMagFieldValues[2]);
                float North_x = m_NormMagFieldValues[0] - m_NormGravityVector[0] * M_dot_G;
                float North_y = m_NormMagFieldValues[1] - m_NormGravityVector[1] * M_dot_G;
                float North_z = m_NormMagFieldValues[2] - m_NormGravityVector[2] * M_dot_G;
                float norm_North = (float)Math.sqrt(North_x * North_x + North_y * North_y + North_z * North_z);
                m_NormNorthVector[0] = North_x / norm_North; m_NormNorthVector[1] = North_y / norm_North; m_NormNorthVector[2] = North_z / norm_North;

                // calculate all the required angles from the rotation matrix
                // NB: see http://math.stackexchange.com/questions/381649/whats-the-best-3d-angular-co-ordinate-system-for-working-with-smartfone-apps
                float sin = m_NormEastVector[1] -  m_NormNorthVector[0], cos = m_NormEastVector[0] +  m_NormNorthVector[1];
                m_azimuth_radians = (float) (sin != 0 && cos != 0 ? Math.atan2(sin, cos) : 0);
                m_pitch_radians = (float) Math.acos(m_NormGravityVector[2]);
                sin = -m_NormEastVector[1] -  m_NormNorthVector[0]; cos = m_NormEastVector[0] -  m_NormNorthVector[1];
                float aximuth_plus_two_pitch_axis_radians = (float)(sin != 0 && cos != 0 ? Math.atan2(sin, cos) : 0);
                m_pitch_axis_radians = (float)(aximuth_plus_two_pitch_axis_radians - m_azimuth_radians) / 2;
                m_OrientationOK = true;
            }
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        int SensorType = sensor.getType();
        switch(SensorType) {
            case Sensor.TYPE_GRAVITY: m_GravityAccuracy = accuracy; break;
            case Sensor.TYPE_MAGNETIC_FIELD: m_MagneticFieldAccuracy = accuracy; break;
        }
    }

}
