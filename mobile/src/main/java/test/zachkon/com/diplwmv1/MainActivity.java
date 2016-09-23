package test.zachkon.com.diplwmv1;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private TextView accelx;
    private TextView accely;
    private TextView accelz;
    private TextView gyrox;
    private TextView gyroy;
    private TextView gyroz;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.myText);
        mTextView2 = (TextView) findViewById(R.id.myText2);

        accelx = (TextView) findViewById(R.id.accelx);
        accely = (TextView) findViewById(R.id.accely);
        accelz = (TextView) findViewById(R.id.accelz);
        gyrox = (TextView) findViewById(R.id.gyrox);
        gyroy = (TextView) findViewById(R.id.gyroy);
        gyroz = (TextView) findViewById(R.id.gyroz);


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
                    float[] accelArray = dataMap.getFloatArray(ACCELERO_MESSAGE_PATH);
                    accelx.setText(Float.toString(accelArray[0]));
                    accely.setText(Float.toString(accelArray[1]));
                    accelz.setText(Float.toString(accelArray[2]));
                }
                else if (item.getUri().getPath().compareTo(GYRO_MESSAGE_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    float[] gyroArray = dataMap.getFloatArray(GYRO_MESSAGE_PATH);
                    gyrox.setText(Float.toString(gyroArray[0]));
                    gyroy.setText(Float.toString(gyroArray[1]));
                    gyroz.setText(Float.toString(gyroArray[2]));
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


}






