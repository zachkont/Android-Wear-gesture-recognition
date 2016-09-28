package test.zachkon.com.diplwmv1;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks,SensorEventListener {

    private TextView mClockView;
    private Button wearButton;

    private TextView accelx;
    private TextView accely;
    private TextView accelz;
    private TextView gyrox;
    private TextView gyroy;
    private TextView gyroz;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mGyroscopeSensor;
    private Sensor mLinearSensor;
    private GoogleApiClient mApiClient;


    final String TAG = "TEST_TAG";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String GYRO_MESSAGE_PATH = "/gyromessage";
    private static final String ACCELERO_MESSAGE_PATH = "/acceleromessage";
    private static final String LINEAR_MESSAGE_PATH = "/linearmessage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        initGoogleApiClient();

    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mApiClient.connect();
    }

    private void init() {

        mClockView = (TextView) findViewById(R.id.wearText);
        wearButton = (Button) findViewById(R.id.wearButton);

        accelx = (TextView) findViewById(R.id.accelx);
        accely = (TextView) findViewById(R.id.accely);
        accelz = (TextView) findViewById(R.id.accelz);
        gyrox = (TextView) findViewById(R.id.gyrox);
        gyroy = (TextView) findViewById(R.id.gyroy);
        gyroz = (TextView) findViewById(R.id.gyroz);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLinearSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        wearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mClockView.getText() == "YES") {
                    mClockView.setText("NO");
                    sendMessage(WEAR_MESSAGE_PATH, "NO");
                } else {
                    mClockView.setText("YES");
                    sendMessage(WEAR_MESSAGE_PATH, "YES");
                }

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLinearSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    String msg;

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            msg = "x = " + (int)event.values[0];
            accelx.setText(msg);
            msg = "y = " + (int)event.values[1];
            accely.setText(msg);
            msg = "z = " + (int)event.values[2];
            accelz.setText(msg);

            sendDataMap(ACCELERO_MESSAGE_PATH, event.values);
            //Log.d(TAG, "DATA'd");
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            msg = "x = " + (int)event.values[0];
            gyrox.setText(msg);
            msg = "y = " + (int)event.values[1];
            gyroy.setText(msg);
            msg = "z = " + (int)event.values[2];
            gyroz.setText(msg);
            //**************************DONT FORGET
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(GYRO_MESSAGE_PATH);
            putDataMapReq.getDataMap().putFloatArray(GYRO_MESSAGE_PATH, event.values);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mApiClient, putDataReq);
            //Log.d(TAG, msg);
        }
        else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            msg = "LINEAR: x= " + (int)event.values[0] +" y= "+ (int)event.values[1] + " z= " +(int)event.values[2];
            float[] data = new float[4];
            data[0]= event.values[0];
            data[1]= event.values[1];
            data[2]= event.values[2];
            data[3] = event.timestamp;
            sendDataMap(LINEAR_MESSAGE_PATH, data);
            //Log.d(TAG, msg);
        }
        else
            Log.d(TAG, "Unknown sensor type");
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }


    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes()).await();
                }

/*                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mClockView.setText( "WHAT?" );
                    }
                });
*/
            }
        }).start();
    }


    private void sendDataMap (final String path, final float[] values) {

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putFloatArray(path, values);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mApiClient, putDataReq);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "CONNECTED!");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }
}





