package com.framework.android.tool;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by Whitelaning on 2015/9/30.
 */
public class ShakeListenerUtils implements SensorEventListener {
    private Activity context;
    private ShakeListener mShakeListener;

    public ShakeListener getmShakeListener() {
        return mShakeListener;
    }

    public void setmShakeListener(ShakeListener mShakeListener) {
        this.mShakeListener = mShakeListener;
    }

    public ShakeListenerUtils(Activity context) {
        super();
        this.context = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        //values[0]:X轴，values[1]：Y轴，values[2]：Z轴
        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {

			/*正常情况下，任意轴数值最大就在9.8~10之间，
              只有在突然摇动手机的时候，瞬时加速度才会突然增大或减少。
			  监听任一轴的加速度大于18即可.
			*/
            if ((Math.abs(values[0]) > 18 ||
                    Math.abs(values[1]) > 18 ||
                    Math.abs(values[2]) > 18)) {
                if (mShakeListener != null) {
                    mShakeListener.phoneShake();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //当传感器精度改变时回调该方法，Do nothing.
    }

    public interface ShakeListener {
        void phoneShake();
    }
}
