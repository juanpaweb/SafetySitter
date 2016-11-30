package com.example.safetysitter.safetysitter;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.safetysitter.safetysitter.RegistrationActivity.DEVICE_ID_KEY;
import static com.example.safetysitter.safetysitter.RegistrationActivity.PHONE_KEY;
import static com.example.safetysitter.safetysitter.RegistrationActivity.PHONE_NUMBER_SAVED_KEY;
import static com.example.safetysitter.safetysitter.RegistrationActivity.TAG;
import static com.example.safetysitter.safetysitter.RegistrationActivity.TOKEN_KEY;
import static com.example.safetysitter.safetysitter.RegistrationActivity.editorPreferencesPhoneNumber;
import static com.example.safetysitter.safetysitter.ServerSocketConnection.SIGN_IN_SUCCESS;
import static com.example.safetysitter.safetysitter.ServerSocketConnection.TOKEN_REFRESHED;
import static com.example.safetysitter.safetysitter.ServerSocketConnection.USER_EXIST;

/**
 * Created by omeredut on 17/11/2016.
 */

public class RegistrationService extends IntentService {

    public static final int TRY_CONECCTION_TO_SRVER = 1000;
    public static final int REGISTRATION_TO_SERVER = 555;
    public static final int REFRESH_TOKEN = 666;
    public static final String TASK_TO_DO_KEY = "TASK_TO_DO";
    public static final String REGISTRATION_SERVICE = "RegistrationService";
    private boolean registrationSuccess = false;
    private boolean refreshTokenSuccess = false;
    private String deviceId;
    private String myPhoneNumber;
    private String token;


    public RegistrationService() {
        super(REGISTRATION_SERVICE);
    }


    private void intentsExtraForRegistrationToServer(Intent intent){
        int whichTaskToDo = intent.getIntExtra(TASK_TO_DO_KEY, 0);
        deviceId = intent.getStringExtra(DEVICE_ID_KEY);
        myPhoneNumber = intent.getStringExtra(PHONE_KEY);
        token = intent.getStringExtra(TOKEN_KEY);
        switch (whichTaskToDo) {
            case REGISTRATION_TO_SERVER:
                Log.d(TAG, "REGISTRATION_TO_SERVER");
                registrationToServer(deviceId, myPhoneNumber, token);
                break;
            case REFRESH_TOKEN:
                Log.d(TAG, "REFRESH_TOKEN");
                refreshToken(deviceId, myPhoneNumber, token);
                break;
            default:
                break;
        }
    }

    private void registrationToServer(final String deviceId, final String myPhoneNumber, final String token){

        final String myDeviceId = deviceId;
        final String phoneNumber = myPhoneNumber;
        final String myToken = token;
        final ServerSocketConnection serverSocketConnectionForRegistration = new ServerSocketConnection(myDeviceId, phoneNumber, myToken);

        while (!registrationSuccess){
            int responseRegistration = serverSocketConnectionForRegistration.registration();
            if (responseRegistration == SIGN_IN_SUCCESS || responseRegistration == USER_EXIST){
                editorPreferencesPhoneNumber.putBoolean(PHONE_NUMBER_SAVED_KEY, true);
                registrationSuccess = true;
                Log.d(TAG, "serviceRegistration success");
            }
            try {
                Thread.sleep(TRY_CONECCTION_TO_SRVER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "try to registration, the result is: " + registrationSuccess);
            Log.d(TAG, "service deviceId: " + myDeviceId);
            Log.d(TAG, "service myPhoneNumber: " + phoneNumber);
            Log.d(TAG, "service token: " + myToken);
        }
    }

    private void refreshToken(String deviceId, String myPhoneNumber, String newToken){
        final String myDeviceId = deviceId;
        final String phoneNumber = myPhoneNumber;
        final String myToken = newToken;
        final ServerSocketConnection serverSocketConnectionForRefreshToken = new ServerSocketConnection(myDeviceId, phoneNumber, myToken);

        while (!refreshTokenSuccess){
            Log.d(TAG, "refreshTokenService try connection to server");
            int responseRefreshToken = serverSocketConnectionForRefreshToken.refreshToken();
            if (responseRefreshToken == TOKEN_REFRESHED){
                refreshTokenSuccess = true;
                Log.d(TAG, "serviceRefreshToken success");
            }
            try {
                Thread.sleep(TRY_CONECCTION_TO_SRVER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandelIntent()");
        intentsExtraForRegistrationToServer(intent);
    }

}
