package com.example.safetysitter.safetysitter;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import static com.example.safetysitter.safetysitter.RegistrationActivity.TAG;

/**
 * Created by omeredut on 17/11/2016.
 */

public class AudioRecorderScanner {

    //audio constants
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE = 8000;// 8khz
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, AUDIO_FORMAT);

    //audio fields
    private AudioRecord audioRecordListening;
    private short[] receivedRecordBuffer;

    //audio variables
    private boolean waitingForEvent = false;
    private boolean isRecording;
    private boolean isPlaying;


    //interface
    private AudioEventListener audioEventListener;

    public static interface AudioEventListener {
        void audioEventHappened(boolean isHappenedAudioEvent);
    }

    public void setAudioEventListener(AudioEventListener audioEventListener) {
        this.audioEventListener = audioEventListener;
    }


    Thread waitingForEventThread = null;

    private boolean isHappenedEvent = false;



    //1 - waiting for audio event...
    public void startWaitingForEvent() {
        //initialized AudioRecord
        if (audioRecordListening == null || audioRecordListening.getState() == AudioRecord.STATE_UNINITIALIZED) {
            audioRecordListening = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL, AUDIO_FORMAT, BUFFER_SIZE);
        }
        //initialized buffer for recorder
        if (receivedRecordBuffer == null) {
            receivedRecordBuffer = new short[BUFFER_SIZE / 2];
        }

        if (audioRecordListening.getState() == AudioRecord.STATE_INITIALIZED) {
            isRecording = true;
        }

        waitingForEventThread = new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecordListening.startRecording();
                int responseCode;
                byte[] recordAudioBufferBytes = new byte[BUFFER_SIZE];

                while (waitingForEvent) {
                    Log.d(TAG, "waiting for event...");
                    responseCode = audioRecordListening.read(receivedRecordBuffer, 0, BUFFER_SIZE / 2);
                    if (responseCode == AudioRecord.ERROR_BAD_VALUE || responseCode == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.d(TAG, "error recording");
                    } else {
                        for (int i = 0; i < receivedRecordBuffer.length; i++) {
                            recordAudioBufferBytes[i * 2] = (byte) (receivedRecordBuffer[i] & 0x00FF);
                            recordAudioBufferBytes[i * 2 + 1] = (byte) (receivedRecordBuffer[i] >> 8);
                            if (recordAudioBufferBytes[i * 2 + 1] > 100 || recordAudioBufferBytes[i * 2 + 1] < -100) {
                                Log.d("omer", " " + recordAudioBufferBytes[i * 2 + 1]);
                                isHappenedEvent = true;
                                waitingForEvent = false;
                                //listener
                                if (audioEventListener != null) {
                                    audioEventListener.audioEventHappened(isHappenedEvent);
                                }
                                break;
                            }
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
                if (!waitingForEvent) {
                    stopWaitingForEventThread();
                }
            }
        });
        waitingForEventThread.start();
    }


    public void stopWaitingForEventThread() {
        if (waitingForEventThread != null) {
            waitingForEventThread.interrupt();
            waitingForEventThread = null;
        }
        if (waitingForEvent) {
            waitingForEvent = false;
        }
    }


    public void releaseAudioRocorder() {
        audioRecordListening.release();
        audioRecordListening = null;
    }



    public void stopRecording() {
        isRecording = false;
    }



    public void setWaitingForEvent(boolean waitingForEvent) {
        this.waitingForEvent = waitingForEvent;
    }

    public boolean getWaitingForEvent() {
        return waitingForEvent;
    }
}