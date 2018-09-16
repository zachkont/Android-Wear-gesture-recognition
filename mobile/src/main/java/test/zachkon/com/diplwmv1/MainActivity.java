package test.zachkon.com.diplwmv1;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity implements
        DataApi.DataListener,
        MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks{

    private TextView mTextView;
    private TextView mTextView2;

    final String TAG = "TEST_TAG";

    private GoogleApiClient mApiClient;
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String GYRO_MESSAGE_PATH = "/gyromessage";
    private static final String ACCELERO_MESSAGE_PATH = "/acceleromessage";
    private static final String LINEAR_MESSAGE_PATH = "/linearmessage";

    private TextView accelx;
    private TextView accely;
    private TextView accelz;
    private TextView gyrox;
    private TextView gyroy;
    private TextView gyroz;
    private TextView linearx;
    private TextView lineary;
    private TextView linearz;
    private TextView posx;
    private TextView posy;
    private TextView posz;

    private float[] dataArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.myText);
        mTextView2 = (TextView) findViewById(R.id.myText2);

        TextView acceltext = (TextView) findViewById(R.id.textView);
        acceltext.setText("Accelerometer");
        TextView gyrotext = (TextView) findViewById(R.id.textView2);
        gyrotext.setText("Gyrocsope");
        TextView linetext = (TextView) findViewById(R.id.textView3);
        linetext.setText("Linear Accelerometer");
        TextView postext = (TextView) findViewById(R.id.textView4);
        postext.setText("Estimated Position");

        accelx = (TextView) findViewById(R.id.accelx);
        accely = (TextView) findViewById(R.id.accely);
        accelz = (TextView) findViewById(R.id.accelz);
        gyrox = (TextView) findViewById(R.id.gyrox);
        gyroy = (TextView) findViewById(R.id.gyroy);
        gyroz = (TextView) findViewById(R.id.gyroz);
        linearx = (TextView) findViewById(R.id.linex);
        lineary = (TextView) findViewById(R.id.liney);
        linearz = (TextView) findViewById(R.id.linez);
        posx = (TextView) findViewById(R.id.pos1);
        posy = (TextView) findViewById(R.id.pos2);
        posz = (TextView) findViewById(R.id.pos3);




        final Button button = (Button) findViewById(R.id.myButton);

        initGoogleApiClient();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mTextView.getText() == "YES") {
                    mTextView.setText("NO");
                } else {
                    mTextView.setText("YES");
                }
                velocity[0] = velocity[1] = velocity[2] = 0f;
                position[0] = position[1] = position[2] = 0f;

            }
        });

    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();

        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(ACCELERO_MESSAGE_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    dataArray = dataMap.getFloatArray(ACCELERO_MESSAGE_PATH);
                    accelx.setText(Float.toString(dataArray[0]));
                    accely.setText(Float.toString(dataArray[1]));
                    accelz.setText(Float.toString(dataArray[2]));
                }
                else if (item.getUri().getPath().compareTo(GYRO_MESSAGE_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    //**************************************DONT FORGET
                    float[] gyroArray = dataMap.getFloatArray(GYRO_MESSAGE_PATH);
                    gyrox.setText(Float.toString(gyroArray[0]));
                    gyroy.setText(Float.toString(gyroArray[1]));
                    gyroz.setText(Float.toString(gyroArray[2]));
                }
                else if (item.getUri().getPath().compareTo(LINEAR_MESSAGE_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    dataArray = dataMap.getFloatArray(LINEAR_MESSAGE_PATH);
                    linearx.setText(Float.toString(dataArray[0]));
                    lineary.setText(Float.toString(dataArray[1]));
                    linearz.setText(Float.toString(dataArray[2]));
                    findPosition(dataArray);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if( messageEvent.getPath().equalsIgnoreCase( WEAR_MESSAGE_PATH ) ) {
                    mTextView2.setText(new String(messageEvent.getData()));
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.w(TAG, "onConnected()");
        Wearable.MessageApi.addListener( mApiClient, this );
        Wearable.DataApi.addListener(mApiClient,this);
    }

    @Override
    protected void onStop() {
        if ( mApiClient != null ) {
            Wearable.MessageApi.removeListener( mApiClient, this );
            Wearable.DataApi.removeListener(mApiClient,this);
            if ( mApiClient.isConnected() ) {
                mApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if( mApiClient != null )
            mApiClient.unregisterConnectionCallbacks( this );
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public float convertByteArrayToFloat(byte[] b, int offset) {
        return ByteBuffer.wrap(b, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    static final float NS2S = 1.0f / 1000000000.0f;
    float[] last_values = null;
    float[] velocity = null;
    float[] position = null;
    float last_timestamp = 0;

    private void findPosition(float[] dataArray) {

        if(last_values != null){
            float dt = (dataArray[3] - last_timestamp) * NS2S;

            for(int index = 0; index < 3;++index){
                velocity[index] += (dataArray[index] + last_values[index])/2 * dt;
                position[index] += velocity[index] * dt;
            }
        }
        else{
            last_values = new float[3];
            velocity = new float[3];
            position = new float[3];
            velocity[0] = velocity[1] = velocity[2] = 0f;
            position[0] = position[1] = position[2] = 0f;
        }
        System.arraycopy(dataArray, 0, last_values, 0, 3);
        last_timestamp = dataArray[3];

        posx.setText("x= " + Float.toString(position[0]));
        posy.setText("y= " + Float.toString(position[1]));
        posz.setText("z= " + Float.toString(position[2]));

    }

}






