package com.example.safetysitter.safetysitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;


import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

import static com.example.safetysitter.safetysitter.RegistrationService.REFRESH_TOKEN;
import static com.example.safetysitter.safetysitter.RegistrationService.REGISTRATION_TO_SERVER;
import static com.example.safetysitter.safetysitter.RegistrationService.TASK_TO_DO_KEY;
import static com.example.safetysitter.safetysitter.SafetySitterPagerAdapter.NUMBER_OF_PAGES_IN_VIEW_PAGER;

public class RegistrationActivity extends FragmentActivity {

    public static final String TAG = "SAFETYSITTER_TAG";
    public static final String TOKEN_KEY = "TOKEN_";
    public static final String PHONE_KEY = "PHONE_NUMBER_";
    public static final String APP_NAME = "SafetySitter";
    public static final String PHONE_NUMBER_SAVED_KEY = "PHONE_NUMBER_SAVED";
    public static String token;
    public static String deviceId;
    public static boolean isHebrew = false;
    public static boolean isUserSaved = false;
    private String languageIsHebrew = null;

    public static final String PREFERENCES_PHONE_NUMBER = "PREFERENCES_PHONE_NUMBER";
    public static final String DEVICE_ID_KEY = "DEVICE_ID_KEY";
    public static final String NULL = "NULL";

    public static SharedPreferences preferencesPhoneNumber;
    public static SharedPreferences.Editor editorPreferencesPhoneNumber;

    public static ViewPager safetysitterRegistrationViewPager;
    private PagerAdapter safetysitterRegistrationPagerAdapter;


    public static void sendTokenToServer(Context context, int registrationOrRefresh){
        String myPhoneNumberString = preferencesPhoneNumber.getString(PHONE_KEY + deviceId, NULL);
        if (myPhoneNumberString != NULL) {
            String newToken = preferencesPhoneNumber.getString(TOKEN_KEY + deviceId, NULL);
            if (newToken != NULL) {
                if (myPhoneNumberString != NULL && newToken != NULL) {
                    Intent serviceRefreshTokenIntent = new Intent(context, RegistrationService.class);
                    Bundle bundleRefreshToken = new Bundle();
                    bundleRefreshToken.putInt(TASK_TO_DO_KEY, registrationOrRefresh);
                    bundleRefreshToken.putString(DEVICE_ID_KEY, deviceId);
                    bundleRefreshToken.putString(PHONE_KEY, myPhoneNumberString);
                    bundleRefreshToken.putString(TOKEN_KEY, newToken);
                    serviceRefreshTokenIntent.putExtras(bundleRefreshToken);
                    Log.d(TAG, "serviceReceived: deviceId: " + deviceId + ", numberPhone: " + myPhoneNumberString + ", token: " + newToken);
                    context.startService(serviceRefreshTokenIntent);
                }
            }
        }
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checking if the device is in hebrew...
        isHebrew = languageDeviceIsHebrew();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_registration);

        //init sharedPreferences
        preferencesPhoneNumber = getSharedPreferences(PREFERENCES_PHONE_NUMBER, MODE_PRIVATE);
        editorPreferencesPhoneNumber = preferencesPhoneNumber.edit();

        //get deviceId
        deviceId = getIdDevice();
        token = preferencesPhoneNumber.getString(TOKEN_KEY+deviceId, NULL);



        //updating the deviceId in sharedPreferences
        if (preferencesPhoneNumber.getString(DEVICE_ID_KEY, NULL) == NULL || preferencesPhoneNumber.getString(DEVICE_ID_KEY, NULL) != deviceId){
            editorPreferencesPhoneNumber.putString(DEVICE_ID_KEY, deviceId);
            editorPreferencesPhoneNumber.commit();
            Log.d(TAG, "deviceId saved: " + preferencesPhoneNumber.getString(DEVICE_ID_KEY, NULL));
        }

        //get message from the firebase
        FirebaseMessaging.getInstance().subscribeToTopic(APP_NAME);


        //if the phoneNumber for this device saved in sharedPreferences, the registration fragment not shown.
        if ((preferencesPhoneNumber.getString(PHONE_KEY+deviceId, NULL)).equals(NULL)){
            NUMBER_OF_PAGES_IN_VIEW_PAGER = 3;} else {NUMBER_OF_PAGES_IN_VIEW_PAGER = 2;}


        //init viewPager
        safetysitterRegistrationViewPager = (ViewPager) findViewById(R.id.view_pager_registration);
        safetysitterRegistrationViewPager.beginFakeDrag();
        safetysitterRegistrationPagerAdapter = new SafetySitterPagerAdapter(getSupportFragmentManager());
        safetysitterRegistrationViewPager.setAdapter(safetysitterRegistrationPagerAdapter);


        //registrationService
        isUserSaved = preferencesPhoneNumber.getBoolean(PHONE_NUMBER_SAVED_KEY, false);
        if (!isUserSaved && (preferencesPhoneNumber.getString(PHONE_KEY + deviceId, NULL) != NULL)){
            sendTokenToServer(getApplicationContext(), REGISTRATION_TO_SERVER);
        }
    }


    private boolean languageDeviceIsHebrew(){
        languageIsHebrew = getResources().getString(R.string.check_if_language_is_hebrew);
        if (Locale.getDefault().getDisplayLanguage().equals(languageIsHebrew)){
            Log.d(TAG, "language is hebrew");
            return true;
        } else {
            return false;
        }
    }


    private String getIdDevice(){
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        Log.d(TAG, "deviceId: " + deviceId);
        return deviceId;
    }

    private boolean userSaveOnServer(){
        if (preferencesPhoneNumber.getBoolean(PHONE_NUMBER_SAVED_KEY, false)){
            return true;
        } else {
            return false;
        }
    }


    public static boolean checkValidityNumberPhone(String numberPhone) {
        if ((numberPhone.length() == 10) && (numberPhone.charAt(0) == '0')) {
            return true;
        } else {
            return false;
        }
    }

}
