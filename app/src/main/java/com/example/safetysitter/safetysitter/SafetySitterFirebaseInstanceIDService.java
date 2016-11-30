package com.example.safetysitter.safetysitter;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.example.safetysitter.safetysitter.RegistrationActivity.TAG;
import static com.example.safetysitter.safetysitter.RegistrationActivity.TOKEN_KEY;
import static com.example.safetysitter.safetysitter.RegistrationActivity.deviceId;
import static com.example.safetysitter.safetysitter.RegistrationActivity.editorPreferencesPhoneNumber;
import static com.example.safetysitter.safetysitter.RegistrationActivity.sendTokenToServer;
import static com.example.safetysitter.safetysitter.RegistrationActivity.token;
import static com.example.safetysitter.safetysitter.RegistrationService.REFRESH_TOKEN;

/**
 * Created by omeredut on 17/11/2016.
 */

public class SafetySitterFirebaseInstanceIDService extends FirebaseInstanceIdService{

    private String newToken;


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        newToken = FirebaseInstanceId.getInstance().getToken();
        token = newToken;
        Log.d(TAG, "token: " + newToken);
        editorPreferencesPhoneNumber.putString(TOKEN_KEY + deviceId, token);
        editorPreferencesPhoneNumber.commit();
        sendTokenToServer(getApplicationContext(), REFRESH_TOKEN);

    }

}
