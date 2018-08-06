package com.joonheelee.mark;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainPageActivity extends Activity {

    private static final String TAG = "bluetooth2";

    Button btnLed1, btnLed2, btnLed3, btnpado, btn;
    TextView txtArduino;
    TextView status;
    RelativeLayout status_bar;
    Handler h;
    LinearLayout blayout;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    private static int flag = 0;

    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:21:13:00:EA:53";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mainpage);

        status = (TextView)findViewById(R.id.status);

        btnLed1 = (Button) findViewById(R.id.left_turn);
        btnLed2 = (Button) findViewById(R.id.right_turn);
        btnLed3 = (Button) findViewById(R.id.jaerong);
        btnpado = (Button) findViewById(R.id.stop);
        btn = (Button)findViewById(R.id.hello);

        blayout = (LinearLayout)findViewById(R.id.layout);
        status_bar = (RelativeLayout)findViewById(R.id.status_bar);

        txtArduino = (TextView) findViewById(R.id.meter);
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);
                        int endOfLineIndex = sb.indexOf("\r\n");
                        if (endOfLineIndex > 0) {
                            String sbprint = sb.substring(0, endOfLineIndex);
                            sb.delete(0, sb.length());
                            txtArduino.setText(sbprint+"cm");

                            int value = Integer.parseInt(sbprint);

                            ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar) ;
                            progress.setProgress(value) ;

                            if(value <= 300){

                                blayout.setBackgroundColor(Color.rgb(255,255,255));

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        blayout.setBackgroundColor(Color.rgb(35,36,41));
                                    }
                                }, 300);


                            }

                            btnLed1.setEnabled(true);
                            btnLed2.setEnabled(true);
                            btnLed3.setEnabled(true);
                            btnpado.setEnabled(true);
                            btn.setEnabled(true);

                        }
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        btnLed1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");
                //Toast.makeText(getBaseContext(), "Turn on First LED", Toast.LENGTH_SHORT).show();
            }
        });
        btnLed2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("2");
                //Toast.makeText(getBaseContext(), "Turn on Second LED", Toast.LENGTH_SHORT).show();
            }
        });
        btnLed3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("3");
                //Toast.makeText(getBaseContext(), "Turn on Third LED", Toast.LENGTH_SHORT).show();
            }
        });
        btnpado.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("4");
                //Toast.makeText(getBaseContext(), "Turn on all LEDs", Toast.LENGTH_SHORT).show();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("5");
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
            status.setText("connected");
            status_bar.setBackground(ContextCompat.getDrawable(this, R.drawable.pink_rounded));

        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}