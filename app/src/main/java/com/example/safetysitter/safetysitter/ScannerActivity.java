package com.example.safetysitter.safetysitter;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static com.example.safetysitter.safetysitter.RegistrationActivity.TAG;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.AUDIO_BOX_KEY;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.HAS_TOKEN_KEY;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.NAME_KEY;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.PHONE_TO_CONNECT_KEY;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.SIREN_BOX_KEY;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.SMS_BOX_KEY;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.VIDEO_BOX_KEY;

public class ScannerActivity extends Activity {

    public static final String AUDIO_EVENT = "AUDIO_EVENT";
    public static final String VIDEO_EVENT = "VIDEO_EVENT";
    public static final String SENT = "SENT";
    public static final String DELIVERED = "DELIVERED";



    //for send SMS
    PendingIntent sentPI;
    PendingIntent deliveredPI;
    BroadcastReceiver smsSentReceiver;
    BroadcastReceiver smsDeliveredReceiver;



    TextView titleBar;
    String nameToConnect = null;
    String phoneNumberToConnect;
    boolean smsBoxIsChecked;
    boolean audioBoxIsChecked;
    boolean videoBoxIsChecked;
    boolean doSirenIsChecked;
    boolean isHaveToken;

    ImageView previewScannerImageView;
    FrameLayout previewFrameLayout;

    SafetySitterSurfaceView.VideoEventListener videoEventListener = new SafetySitterSurfaceView.VideoEventListener() {
        @Override
        public void videoEventHappened(boolean isHappenedAudioEvent) {
            String videoEvent =  VIDEO_EVENT;
            executeOnEvent(videoEvent);
        }
    };


    AudioRecorderScanner.AudioEventListener audioEventListener = new AudioRecorderScanner.AudioEventListener() {
        @Override
        public void audioEventHappened(boolean isHappenedAudioEvent) {
            if (isHappenedAudioEvent){
                String audioEvent = AUDIO_EVENT;
                executeOnEvent(audioEvent);
            }
        }
    };


    boolean isTheOnCreateInvoked = false;


    //for camera scanner
    Camera scannerCamera;
    SafetySitterSurfaceView safetySitterSurfaceView;
    boolean videoIsScanning = false;

    //for audio scanner
    AudioRecorderScanner audioRecorderScanner;
    boolean audioIsScanning = false;

    //button
    ImageView startAndStopButtonImageView;

    //rotate button
    OrientationEventListener orientationEventListener;
    Thread orientationThread;
    int mOrientation = 0;
    int lastOrientation = 0;
    int previousOrientation = 0;
    RotateAnimation rotateAnimation;
    private void setRotateAnimation(int orientation, int lastOrientation){
        //duration animation.
        int duration = 0;
        if ((orientation - previousOrientation == 180) || (orientation - previousOrientation == -180)){duration = 1000;} else {duration = 500;}

        //rotate way.
        if (previousOrientation != orientation) {
            int startRotate = 0;
            int stopRotate = 0;


            if (previousOrientation == 90 || previousOrientation == 270){
                startRotate = previousOrientation - 180;
            } else {
                startRotate = previousOrientation;
            }
            if (orientation == 90 || orientation == 270){
                stopRotate = orientation - 180;
            } else {
                stopRotate = orientation;
            }
            if (orientation == 90 && previousOrientation == 180) {
                stopRotate = stopRotate + 360;
            }
            if (orientation == 180 && previousOrientation == 90) {
                startRotate = startRotate + 360;
            }

            rotateAnimation = new RotateAnimation(startRotate, stopRotate, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(duration);
            rotateAnimation.setFillAfter(true);
            startAndStopButtonImageView.startAnimation(rotateAnimation);
            previousOrientation = orientation;
        }
    }


    private void rotateButton(){
        orientationThread = new Thread(new Runnable() {


            @Override
            public void run() {
                orientationEventListener = new OrientationEventListener(ScannerActivity.this) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        Log.d(TAG, "orientation changed: " + orientation);

                        if (orientation < 5 || orientation > 355){
                            mOrientation = 0;
                        } else if (orientation > 85 && orientation < 95) {
                            mOrientation = 90;
                        } else if (orientation > 175 && orientation < 185) {
                            mOrientation = 180;
                        } else if (orientation > 265 && orientation < 275) {
                            mOrientation = 270;
                        }
                        switch (mOrientation) {
                            case 0:
                                setRotateAnimation(mOrientation, lastOrientation);
                                break;
                            case 90:
                                setRotateAnimation(mOrientation, lastOrientation);
                                break;
                            case 180:
                                setRotateAnimation(mOrientation, lastOrientation);
                                break;
                            case 270:
                                setRotateAnimation(mOrientation, lastOrientation);
                                break;
                            default:
                                break;
                        }
                        lastOrientation = orientation;
                    }
                };
                orientationEventListener.enable();
            }
        });
        orientationThread.start();
    }



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);


        rotateButton();



        isTheOnCreateInvoked = true;

        previewFrameLayout = (FrameLayout) findViewById(R.id.preview_scanner_camera);


        scannerCamera = getCameraInstance();

        safetySitterSurfaceView = new SafetySitterSurfaceView(this, scannerCamera);
        safetySitterSurfaceView.setVideoEventListener(videoEventListener);

        audioRecorderScanner = new AudioRecorderScanner();
        audioRecorderScanner.setAudioEventListener(audioEventListener);

        startAndStopButtonImageView = (ImageView) findViewById(R.id.start_and_stop_button_image_view);
        startAndStopButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!audioIsScanning && !videoIsScanning){
                    startAndStopButtonImageView.setEnabled(true);




                    //start audio scanning();
                    if (!audioRecorderScanner.getWaitingForEvent()){
                        audioRecorderScanner.setWaitingForEvent(true);
                    }
                    audioRecorderScanner.startWaitingForEvent();
                    audioIsScanning = true;


                    //start video scanning();
                    scannerCamera.startPreview();
                    if (!safetySitterSurfaceView.getWaitingForEvent()) {
                        safetySitterSurfaceView.setWaitingForEvent(true);
                    }
                    scannerCamera.setOneShotPreviewCallback(safetySitterSurfaceView);
                    videoIsScanning = true;


                    startAndStopButtonImageView.setImageResource(R.drawable.security_camera_for_button_stop_scan);
                    startAndStopButtonImageView.setEnabled(true);










                } else if (audioIsScanning && videoIsScanning){
                    startAndStopButtonImageView.setEnabled(false);

                    //stop audio scanning();
                    audioRecorderScanner.stopWaitingForEventThread();
                    audioIsScanning = false;


                    //stop video scanning();
                    if (safetySitterSurfaceView.getWaitingForEvent()){
                        safetySitterSurfaceView.setWaitingForEvent(false);
                    }
                    //scannerCamera.stopPreview();
                    videoIsScanning = false;


                    startAndStopButtonImageView.setImageResource(R.drawable.security_camera_for_button_start_scan);
                    startAndStopButtonImageView.setEnabled(true);


                }
            }
        });


        //title bar and preferences
        titleBar = (TextView) findViewById(R.id.title_bar);

        Bundle preferencesBundle = getIntent().getExtras();

        nameToConnect = preferencesBundle.getString(NAME_KEY);
        phoneNumberToConnect = preferencesBundle.getString(PHONE_TO_CONNECT_KEY);
        Log.d(TAG, "" + phoneNumberToConnect);
        smsBoxIsChecked = preferencesBundle.getBoolean  (SMS_BOX_KEY);
        audioBoxIsChecked = preferencesBundle.getBoolean(AUDIO_BOX_KEY);
        videoBoxIsChecked = preferencesBundle.getBoolean(VIDEO_BOX_KEY);
        doSirenIsChecked = preferencesBundle.getBoolean (SIREN_BOX_KEY);
        isHaveToken = preferencesBundle.getBoolean (HAS_TOKEN_KEY);

        if (nameToConnect != null){
            titleBar.setText(nameToConnect);
        } else {
            titleBar.setText(phoneNumberToConnect);
        }




        //send sms
        sentPI = PendingIntent.getBroadcast(ScannerActivity.this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(ScannerActivity.this, 0, new Intent(DELIVERED), 0);
        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String resultForSendSMS = "";
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        resultForSendSMS = "sms sent";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        resultForSendSMS = "generic failure";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        resultForSendSMS = "no service";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        resultForSendSMS = "Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        resultForSendSMS = "radio off";
                        break;
                }
                Toast.makeText(ScannerActivity.this, resultForSendSMS, Toast.LENGTH_SHORT).show();
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String resultForSendSMS = "";
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        resultForSendSMS = "sms delivered";
                        break;
                    case Activity.RESULT_CANCELED:
                        resultForSendSMS = "sms not delivered";
                        break;
                }
                Toast.makeText(ScannerActivity.this, resultForSendSMS, Toast.LENGTH_SHORT).show();
            }
        };









    }





    @Override
    protected void onResume() {
        if (orientationThread == null) {
            rotateButton();
        }
        //orientationEventListener.enable();
        super.onResume();
        startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        orientationEventListener.disable();
        orientationThread.interrupt();
        orientationThread = null;
        stopScanning();


    }










    private Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e){
            Log.d(TAG, "exception: " + e.getMessage());
        }
        return camera;
    }





    //this method call when event happened
    private void executeOnEvent(String whichEvent){
        if (whichEvent.equals(AUDIO_EVENT)){
            //stop video scanner
            safetySitterSurfaceView.setWaitingForEvent(false);
            videoIsScanning = false;
        } else if (whichEvent.equals(VIDEO_EVENT)){
            audioRecorderScanner.stopWaitingForEventThread();
            audioIsScanning = false;
        }

        if (smsBoxIsChecked) {
            ScannerActivity.this.registerReceiver(smsSentReceiver, new IntentFilter(SENT));
            ScannerActivity.this.registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
            SmsManager smsManager = SmsManager.getDefault();
            Log.d(TAG, "onResume: " + phoneNumberToConnect);
            smsManager.sendTextMessage(phoneNumberToConnect, null, getResources().getString(R.string.contant_sms_in_event), sentPI, deliveredPI);
        }
        if (audioBoxIsChecked && isHaveToken){

        }
        if (videoBoxIsChecked && isHaveToken){

        }
        if (doSirenIsChecked){
            MediaPlayer mediaPlayerSiren = MediaPlayer.create(ScannerActivity.this, R.raw.alarm_force);
            mediaPlayerSiren.start();
        }
    }


    private void startScanning(){
        //restart for audio scanner
        if (audioRecorderScanner == null) {
            audioRecorderScanner = new AudioRecorderScanner();
        }
        audioRecorderScanner.startWaitingForEvent();
        audioIsScanning = true;


        //start for video scanner
        if (scannerCamera == null) {
            scannerCamera = getCameraInstance();
            safetySitterSurfaceView = new SafetySitterSurfaceView(this, scannerCamera);
        }
        try {
            safetySitterSurfaceView.getHolder().addCallback(safetySitterSurfaceView);
            scannerCamera.setPreviewDisplay(safetySitterSurfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        previewFrameLayout.addView(safetySitterSurfaceView);
        if (!safetySitterSurfaceView.getWaitingForEvent()){
            safetySitterSurfaceView.setWaitingForEvent(true);
        }
        scannerCamera.startPreview();
        videoIsScanning = true;
        //if i want start the scanning without a button i need to add this lines

        //at the audio recorder
        /*if (!audioRecorderScanner.getWaitingForEvent()){
            audioRecorderScanner.setWaitingForEvent(true);
        }
        audioRecorderScanner.startWaitingForEvent();*/

        //at the video scanner
        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                scannerCamera.setOneShotPreviewCallback(safetysitterSurfaceView);
            }
        }).start();*/

        startAndStopButtonImageView.setImageResource(R.drawable.security_camera_for_button_stop_scan);

        if (isTheOnCreateInvoked){
            isTheOnCreateInvoked = false;
            audioIsScanning = false;
            videoIsScanning = false;
            startAndStopButtonImageView.setImageResource(R.drawable.security_camera_for_button_start_scan);
        }


    }
    private void stopScanning(){
        //stop for audio scanner
        audioRecorderScanner.stopWaitingForEventThread();
        audioRecorderScanner.releaseAudioRocorder();
        audioRecorderScanner = null;
        audioIsScanning = false;


        //stop for video scanner
        if (scannerCamera != null) {
            scannerCamera.setOneShotPreviewCallback(null);
            scannerCamera.stopPreview();
            scannerCamera.release();
            previewFrameLayout.removeView(safetySitterSurfaceView);
            scannerCamera = null;
        }
        if (safetySitterSurfaceView != null && safetySitterSurfaceView.getWaitingForEvent()) {
            safetySitterSurfaceView.setWaitingForEvent(false);
            safetySitterSurfaceView.getHolder().removeCallback(safetySitterSurfaceView);
            safetySitterSurfaceView = null;
        }

        videoIsScanning = false;

        startAndStopButtonImageView.setImageResource(R.drawable.security_camera_for_button_start_scan);

        isTheOnCreateInvoked = true;

    }
}
