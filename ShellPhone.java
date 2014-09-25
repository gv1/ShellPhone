package com.shellphone.android;

import android.app.Service;
import android.os.IBinder;
import android.net.Uri;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.content.Intent;
import android.telephony.PhoneStateListener;

import java.net.ServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.InetAddress;

public class ShellPhone extends Service {    
    private ServerSocket serverSocket;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    private Handler handler = new Handler();
    String line = null;

    @Override
    public void onCreate() {
        // super.onCreate(savedInstanceState);
        // setContentView(R.layout.main);
	MyPhoneListener phoneListener = new MyPhoneListener();
   	TelephonyManager telephonyManager =
	    (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
   	// receive notifications of telephony state changes
   	telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
	// this.serverThread = new Thread(new ServerThread());
	this.serverThread = new Thread(new LocalServerThread());
	this.serverThread.start();
    }

    private String call(String number) {
	try {
	    // set the data
	    String uri = "tel:"+number;
	    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));	    
	    startActivity(callIntent);
	    return("Done");
	}catch(Exception e) {
	    // Toast.makeText(getApplicationContext(),"Your call has failed...",
	    // Toast.LENGTH_LONG).show();
	    e.printStackTrace();
	    return(number + " Your call has failed...");
	}	
    }

    private String dial(String number) {
	try {
	    String uri = "tel:"+number;
	    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
	    startActivity(dialIntent);
	    return("Done");
	}catch(Exception e) {
	    // Toast.makeText(getApplicationContext(),"Your call has failed...",
	    //		   Toast.LENGTH_LONG).show();
	    e.printStackTrace();
	    return(number + " Your call has failed...");
	}
    }

    private class MyPhoneListener extends PhoneStateListener {
	private boolean onCall = false;
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
	    switch (state) {
	    case TelephonyManager.CALL_STATE_RINGING:
		// phone ringing...
		break;
		
	    case TelephonyManager.CALL_STATE_OFFHOOK:
		// one call exists that is dialing, active, or on hold
		onCall = true;
		break;
		
	    case TelephonyManager.CALL_STATE_IDLE:
                // in initialization of the class and at the end of phone call
		// detect flag from CALL_STATE_OFFHOOK
                if (onCall == true) {
		    onCall = false;
		}
		break;
	    default:
		break;
	    }
	}
    }

    
    private class ServerThread implements Runnable {
	public void run() {
	    Socket server = null;
	    try {
		    serverSocket = new ServerSocket(SERVERPORT);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    while (!Thread.currentThread().isInterrupted()) {
		try {
		    server = serverSocket.accept();	
		    // connected
		    handler.post(new Runnable() {
			    @Override
			    public void run() {
				// on connected
			    }
			});
		    try {
			BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			// BufferedWriter out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			// String line = null;
			while ((line = in.readLine()) != null) {
			    // Log.d("ServerActivity", line);
			    handler.post(new Runnable() {
				    @Override
				    public void run() {
					call(line);
					// update
				    }
				});
			}
			break;
		    } catch (Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
				    // interrupted connection
				}
			    });
			e.printStackTrace();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	    }
	}
	
    }    
    @Override
    public IBinder onBind(Intent intent) {
	// We don't provide binding, so return null
	return null;
    }
    
    @Override
    public void onDestroy() {
	
    }    
    
    private class LocalServerThread  implements Runnable{
	// AF_LOCAL/UNIX
	LocalSocket ls = null;
	LocalSocketAddress lsa = null;

	public void run() {
	    ls = new LocalSocket();
	    lsa = new LocalSocketAddress("/data/local/tmp/ShellPhoneSocket");
	    try {
		ls.bind(lsa);
		ls.connect(lsa);
	    } catch (IOException e) {
		e.printStackTrace();
	    } 
	    while (!Thread.currentThread().isInterrupted()) {
		try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(ls.getInputStream()));
		    while ((line = in.readLine()) != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
				    call(line);
				    // update
				}
			    });
		}
		    break;
		} catch (Exception e) {
		    handler.post(new Runnable() {
			@Override
			public void run() {
			    // interrupted connection
			}
		    });
		    e.printStackTrace();
		}
	    }
	}
    }
}
