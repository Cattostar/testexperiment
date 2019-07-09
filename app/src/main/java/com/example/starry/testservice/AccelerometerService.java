package com.example.starry.testservice;



import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;

public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager sensorManager;//float 太大,不考虑resolution,find the best way of recording
    private final IBinder mBinder = new AccBinder();
   // private static float dataFromAccelerometer = 0;
    private  float accx ;
    private  float accy ;
    private  float accz ;
    private  int index=0;//add index
    private  static float[] reading= new float[10];//change bits
    private long StartTime;
    private float[] gValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private boolean sensorStatus = false;

    public float[] getDataFromAccelerometr() {
        //return dataFromAccelerometer;
        //return values;
        synchronized (this) {
            return reading;
        }
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCreate() {
        //Toast.makeText(this, "Service is created", Toast.LENGTH_SHORT).show();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean sr = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME); // around 50Hz
        boolean sr_1 = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),//change gyroscope
                SensorManager.SENSOR_DELAY_GAME); // SENSOR_DELAY_GAME
        boolean sr_2 = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_GAME); // SENSOR_DELAY_GAME


        sensorStatus= sr && sr_1 && sr_2;//why false???
        StartTime = System.currentTimeMillis();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "实验终止", Toast.LENGTH_LONG).show();
        sensorManager.unregisterListener(this);
        //dataFromAccelerometer = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accx = event.values[0];
            accy = event.values[1];
            accz = event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            gValues = event.values;
        }


        synchronized (this) {
            Long tsLong = System.currentTimeMillis();
            float currTime= tsLong- StartTime; //time tag
            index++;
            reading[0] = magneticFieldValues[0];//raw data
            reading[1] = magneticFieldValues[1];
            reading[2] = magneticFieldValues[2];
            reading[3] = gValues[0];
            reading[4] = gValues[1];
            reading[5] = gValues[2];
            reading[6] = accx;
            reading[7] = accy;
            reading[8] = accz;
            reading[9] = currTime;//ten bits
        }

    }



    public class AccBinder extends Binder {
        AccelerometerService getService() {
            return AccelerometerService.this;
        }
    }

    public boolean getSensorStatus(){

        return this.sensorStatus;
    }

    public void unregistered(){

        if(sensorManager!=null){
            sensorManager.unregisterListener(this);

        }
    }

}
