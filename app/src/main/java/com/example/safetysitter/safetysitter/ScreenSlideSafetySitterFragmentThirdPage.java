package com.example.safetysitter.safetysitter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import static com.example.safetysitter.safetysitter.RegistrationActivity.checkValidityNumberPhone;
import static com.example.safetysitter.safetysitter.RegistrationActivity.deviceId;
import static com.example.safetysitter.safetysitter.RegistrationActivity.editorPreferencesPhoneNumber;
import static com.example.safetysitter.safetysitter.RegistrationActivity.isHebrew;
import static com.example.safetysitter.safetysitter.RegistrationActivity.preferencesPhoneNumber;
import static com.example.safetysitter.safetysitter.RegistrationActivity.safetysitterRegistrationViewPager;
import static com.example.safetysitter.safetysitter.RegistrationActivity.token;
import static com.example.safetysitter.safetysitter.SafetySitterPagerAdapter.NUMBER_OF_PAGES_IN_VIEW_PAGER;
import static com.example.safetysitter.safetysitter.ServerSocketConnection.FOUND_TOKEN_PHONE_TO_SEND;
import static com.example.safetysitter.safetysitter.ServerSocketConnection.NOT_FOUND_TOKEN_PHONE_TO_SEND;

/**
 * Created by omeredut on 17/11/2016.
 */

public class ScreenSlideSafetySitterFragmentThirdPage extends DialogFragment{

    public static final String NAME_KEY = "NAME_TO_CONNECT";
    public static final String PHONE_TO_CONNECT_KEY = "PHONE_TO_CONNECT";
    public static final String SMS_BOX_KEY = "SMS_BOX";
    public static final String AUDIO_BOX_KEY = "AUDIO_BOX";
    public static final String VIDEO_BOX_KEY = "VIDEO_BOX";
    public static final String SIREN_BOX_KEY = "SIREN_BOX";
    public static final String HAS_TOKEN_KEY = "HAS_TOKEN";
    public static final String ALERT_DIALOG_2 = "ALERT_DIALOG_2";
    public static final String ALERT_DIALOG_1 = "ALERT_DIALOG_1";
    public static String phoneToConnect;
    public static String nameToConnect;

    //check boxes
    private CheckBox SMSCheckBox;
    private CheckBox audioScannerCheckBox;
    private CheckBox videoScannerCheckBox;
    private CheckBox doSirenCheckBox;

    //bundle for preferences
    private Bundle preferencesBundle;

    //booleans for check boxes
    private boolean smsBoxIsChecked = false;
    private boolean audioBoxIsChecked = false;
    private boolean videoBoxIsChecked = false;
    private boolean doSirenIsChecked = false;
    private boolean isTokenSavedOnServer = false;

    //buttons
    private ImageView btnStartScanner;
    private ImageView btnBack;


    AlertDialogFragment.OnTryAgainListener onTryAgainListener = new AlertDialogFragment.OnTryAgainListener() {
        @Override
        public void onTryAgain() {
            phoneToConnectWithServer();
        }
    };
    AlertDialogFragment.OnContinueToScannerListener onContinueToScannerListener = new AlertDialogFragment.OnContinueToScannerListener() {
        @Override
        public void onContinueToScanner() {
            goToScannerActivity();
        }
    };


    ServerSocketConnection serverSocketConnectionPhoneToSend;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup thirdRootView;

        thirdRootView = (ViewGroup) inflater.inflate(R.layout.fragment_third_page_screen_slide_safety_sitter, container, false);

        //init check boxes
        SMSCheckBox = (CheckBox) thirdRootView.findViewById(R.id.check_box_send_sms);
        audioScannerCheckBox = (CheckBox) thirdRootView.findViewById(R.id.check_box_audio_scanner);
        videoScannerCheckBox = (CheckBox) thirdRootView.findViewById(R.id.check_box_video_scanner);
        doSirenCheckBox = (CheckBox) thirdRootView.findViewById(R.id.check_box_siren);
        getCheckBoxesPreferences();

        //init button of start scanner
        btnStartScanner = (ImageView) thirdRootView.findViewById(R.id.btn_image_view_start_scanner);
        btnStartScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("omer", "phone to connect is: " + phoneToConnect);
                if (phoneToConnect == null || !checkValidityNumberPhone(phoneToConnect)){
                    return;
                }

                btnStartScanner.setEnabled(false);

                saveCheckBoxesPreferences();

                preferencesBundle = new Bundle();
                preferencesBundle.putString(NAME_KEY, nameToConnect);
                preferencesBundle.putString(PHONE_TO_CONNECT_KEY, phoneToConnect);
                preferencesBundle.putBoolean(SMS_BOX_KEY, smsBoxIsChecked);
                preferencesBundle.putBoolean(AUDIO_BOX_KEY, audioBoxIsChecked);
                preferencesBundle.putBoolean(VIDEO_BOX_KEY, videoBoxIsChecked);
                preferencesBundle.putBoolean(SIREN_BOX_KEY, doSirenIsChecked);
                serverSocketConnectionPhoneToSend = new ServerSocketConnection(deviceId, phoneToConnect, token);

                phoneToConnectWithServer();
            }
        });

        btnBack = (ImageView) thirdRootView.findViewById(R.id.btn_image_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backPreviousPage();
            }
        });




        return thirdRootView;
    }

    private void backPreviousPage(){
        phoneToConnect = null;
        if (NUMBER_OF_PAGES_IN_VIEW_PAGER == 2) {
            safetysitterRegistrationViewPager.setCurrentItem(0, true);
        } else {
            safetysitterRegistrationViewPager.setCurrentItem(1, true);
        }
    }


    private void getCheckBoxesPreferences(){
        if (preferencesPhoneNumber.contains(SMS_BOX_KEY)){
            smsBoxIsChecked = preferencesPhoneNumber.getBoolean(SMS_BOX_KEY, false);
            SMSCheckBox.setChecked(smsBoxIsChecked);
        }
        if (preferencesPhoneNumber.contains(AUDIO_BOX_KEY)){
            audioBoxIsChecked = preferencesPhoneNumber.getBoolean(AUDIO_BOX_KEY, false);
            audioScannerCheckBox.setChecked(audioBoxIsChecked);
        }
        if (preferencesPhoneNumber.contains(VIDEO_BOX_KEY)){
            videoBoxIsChecked = preferencesPhoneNumber.getBoolean(VIDEO_BOX_KEY, false);
            videoScannerCheckBox.setChecked(videoBoxIsChecked);
        }
        if (preferencesPhoneNumber.contains(SIREN_BOX_KEY)){
            doSirenIsChecked = preferencesPhoneNumber.getBoolean(SIREN_BOX_KEY, false);
            doSirenCheckBox.setChecked(doSirenIsChecked);
        }
    }

    private void saveCheckBoxesPreferences(){
        editorPreferencesPhoneNumber = preferencesPhoneNumber.edit();
        smsBoxIsChecked = SMSCheckBox.isChecked();
        editorPreferencesPhoneNumber.putBoolean(SMS_BOX_KEY, smsBoxIsChecked);
        editorPreferencesPhoneNumber.commit();
        audioBoxIsChecked = audioScannerCheckBox.isChecked();
        editorPreferencesPhoneNumber.putBoolean(AUDIO_BOX_KEY, audioBoxIsChecked);
        editorPreferencesPhoneNumber.commit();
        videoBoxIsChecked = videoScannerCheckBox.isChecked();
        editorPreferencesPhoneNumber.putBoolean(VIDEO_BOX_KEY, videoBoxIsChecked);
        editorPreferencesPhoneNumber.commit();
        doSirenIsChecked = doSirenCheckBox.isChecked();
        editorPreferencesPhoneNumber.putBoolean(SIREN_BOX_KEY, doSirenIsChecked);
        editorPreferencesPhoneNumber.commit();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_alert, null);
        builder.setMessage("phone to connact cannot found").setTitle("Error")
                .setPositiveButton("continue to scanner", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferencesBundle.putBoolean(HAS_TOKEN_KEY, isTokenSavedOnServer);
                        getActivity().finish();
                        Intent scannerIntent = new Intent(getContext(), ScannerActivity.class);
                        scannerIntent.putExtras(preferencesBundle);
                        startActivity(scannerIntent);
                    }
                })
                .setNegativeButton("try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        phoneToConnectWithServer();
                    }
                });
        //builder.setView(view);
        return builder.create();
    }


    private void phoneToConnectWithServer(){

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int responsePhoneToConnect = serverSocketConnectionPhoneToSend.checkPhoneToSend(phoneToConnect);
                switch (responsePhoneToConnect) {
                    case FOUND_TOKEN_PHONE_TO_SEND:
                        isTokenSavedOnServer = true;
                        return 1;
                    case NOT_FOUND_TOKEN_PHONE_TO_SEND:
                        return 2;
                    case -1:
                        return 3;
                    default:return 3;
                }
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                btnStartScanner.setEnabled(true);
                if (integer == 1){
                    goToScannerActivity();
                } else if (integer == 2){
                    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                            getResources().getString(R.string.title_alert_token_not_found),
                            getResources().getString(R.string.text_alert_positive_button),
                            getResources().getString(R.string.text_alert_negative_button),
                            getResources().getString(R.string.message_alert_token_not_found));
                    alertDialogFragment.setOnTryAgainListener(onTryAgainListener);
                    alertDialogFragment.setOnContinueToScannerListener(onContinueToScannerListener);
                    alertDialogFragment.show(getActivity().getSupportFragmentManager(), ALERT_DIALOG_1);
                } else if (integer == 3){
                    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                            getResources().getString(R.string.title_alert_error_connect_to_server),
                            getResources().getString(R.string.text_alert_positive_button),
                            getResources().getString(R.string.text_alert_negative_button),
                            getResources().getString(R.string.message_alert_error_connect_to_server));
                    alertDialogFragment.setOnTryAgainListener(onTryAgainListener);
                    alertDialogFragment.setOnContinueToScannerListener(onContinueToScannerListener);
                    alertDialogFragment.show(getActivity().getSupportFragmentManager(), ALERT_DIALOG_2);
                }
            }
        }.execute();
    }


    private void goToScannerActivity(){
        preferencesBundle.putBoolean(HAS_TOKEN_KEY, isTokenSavedOnServer);
        getActivity().finish();
        Intent scannerIntent = new Intent(getContext(), ScannerActivity.class);
        scannerIntent.putExtras(preferencesBundle);
        startActivity(scannerIntent);
    }


    /*private void setLayoutByLanguage(){
        if (isHebrew){
            thirdRootView = (ViewGroup) inflater.inflate(R.layout.fragment_third_page_screen_slide_safety_sitter_heb, container, false);
        } else {
        thirdRootView = (ViewGroup) inflater.inflate(R.layout.fragment_third_page_screen_slide_safety_sitter, container, false);
        }
    }*/



}




