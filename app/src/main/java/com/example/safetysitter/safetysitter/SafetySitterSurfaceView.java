package com.example.safetysitter.safetysitter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.example.safetysitter.safetysitter.RegistrationActivity.TAG;

/**
 * Created by omeredut on 17/11/2016.
 */

public class SafetySitterSurfaceView  extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public static final int STANDARD_DEVITION = 5000000;
    public static final int TIME_PAUSE_BETWEEN_FRAMES = 500;
    public static final int SAMPLING_PIXELS = 50;
    private SurfaceHolder scannerCameraHolder;
    private Camera scannerCamera;
    private Context context;


    private Bitmap anchorBitmap = null;
    private int[] pixels = null;
    private int[] anchorPixels = null;
    private boolean waitingForEvent = true;



    //interface
    private VideoEventListener videoEventListener;
    public static interface VideoEventListener {void videoEventHappened(boolean isHappenedAudioEvent);}
    public void setVideoEventListener(VideoEventListener videoEventListener){
        this.videoEventListener = videoEventListener;
    }
    private boolean isVideoEvent = false;

    public boolean getWaitingForEvent(){
        return waitingForEvent;
    }
    public void setWaitingForEvent(boolean waitingForEvent){
        this.waitingForEvent = waitingForEvent;
    }




    public SafetySitterSurfaceView(Context context, Camera camera) {
        super(context);
        this.context = context;
        scannerCamera = camera;

        scannerCameraHolder = getHolder();
        scannerCameraHolder.addCallback(this);

    }





    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated");
        surfaceHolder.setKeepScreenOn(true);
        try {
            scannerCamera.setPreviewDisplay(scannerCameraHolder);
            scannerCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged");
        scannerCamera.setDisplayOrientation(90);
        /*switch (getContext().getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
                scannerCamera.setDisplayOrientation(270);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                scannerCamera.setDisplayOrientation(90);
                break;
            case Configuration.ORIENTATION_UNDEFINED:
                scannerCamera.setDisplayOrientation(270);
        }*/

    }




    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed");
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        isEventVideo(data, camera);
    }



    private void isEventVideo(byte[] data, Camera camera){
        Log.d(TAG, "one()");

        // Convert to JPG
        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
        byte[] jdata = baos.toByteArray();

        // Convert to Bitmap
        Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
        Log.d(TAG, "bmp.getByteCount() before: " + bmp.getByteCount());
        bmp = Bitmap.createScaledBitmap(bmp, width/35, height/35, true);
        if (anchorBitmap == null){
            anchorBitmap = bmp.copy(Bitmap.Config.ARGB_8888, false);
            anchorPixels = new int[anchorBitmap.getWidth() * anchorBitmap.getHeight()];
            Log.d(TAG, "bmp.getByteCount() after: " + bmp.getByteCount());

            anchorBitmap.getPixels(anchorPixels, 0, anchorBitmap.getWidth(), 0, 0, anchorBitmap.getWidth(), anchorBitmap.getHeight());
            //
            try {
                Thread.sleep(TIME_PAUSE_BETWEEN_FRAMES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            camera.setOneShotPreviewCallback(SafetySitterSurfaceView.this);
            //
            Log.d(TAG, "pixelsOne: " +anchorPixels[0]);


        } else {
            pixels = new int[bmp.getWidth() * bmp.getHeight()];
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
            new Thread(new Runnable() {
                @Override
                public void run() {

                    int i = 1;
                    while (pixels != null && i < pixels.length-1 && waitingForEvent){
                        if ((anchorPixels != null) && ((pixels[i] < (anchorPixels[i]+STANDARD_DEVITION) && pixels[i] > (anchorPixels[i]-STANDARD_DEVITION))
                                || (pixels[i-1] < (anchorPixels[i]+ STANDARD_DEVITION) && pixels[i] > (anchorPixels[i-1]-STANDARD_DEVITION))
                                || (pixels[i+1] < (anchorPixels[i]+STANDARD_DEVITION) && pixels[i] > (anchorPixels[i+1]-STANDARD_DEVITION)))){
                            Log.d(TAG, "pixels is OK, pixels " + i + ": " + pixels[i] + " pixelsOne" + i + ": " + anchorPixels[i]);
                            i += SAMPLING_PIXELS;
                        } else {
                            waitingForEvent = false;
                            isVideoEvent = true;
                            videoEventListener.videoEventHappened(isVideoEvent);
                            Log.d(TAG, "Scenario Video Detected");
                            break;
                        }
                    }
                }
            }).start();

        }
        if (waitingForEvent) {
            try {
                Thread.sleep(TIME_PAUSE_BETWEEN_FRAMES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            camera.setOneShotPreviewCallback(this);
        } else {
            return;
        }
    }

}
