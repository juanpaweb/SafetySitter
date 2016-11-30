package com.example.safetysitter.safetysitter;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import static com.example.safetysitter.safetysitter.RegistrationActivity.PHONE_KEY;
import static com.example.safetysitter.safetysitter.RegistrationActivity.checkValidityNumberPhone;
import static com.example.safetysitter.safetysitter.RegistrationActivity.deviceId;
import static com.example.safetysitter.safetysitter.RegistrationActivity.editorPreferencesPhoneNumber;
import static com.example.safetysitter.safetysitter.RegistrationActivity.isHebrew;
import static com.example.safetysitter.safetysitter.RegistrationActivity.isUserSaved;
import static com.example.safetysitter.safetysitter.RegistrationActivity.preferencesPhoneNumber;
import static com.example.safetysitter.safetysitter.RegistrationActivity.safetysitterRegistrationViewPager;
import static com.example.safetysitter.safetysitter.RegistrationActivity.sendTokenToServer;
import static com.example.safetysitter.safetysitter.RegistrationService.REGISTRATION_TO_SERVER;

/**
 * Created by omeredut on 17/11/2016.
 */

public class ScreenSlideSafetySitterFragmentFirstPage extends Fragment {

    private EditText myPhoneNumberEditText;
    private String myPhoneNumberString = null;
    private ImageView enterMyPhoneNumberImageView;






    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup rootView;
        if (isHebrew){
            rootView = (ViewGroup) inflater.inflate(R.layout.fragment_first_page_screen_slide_safety_sitter_heb, container, false);
        } else {
            rootView = (ViewGroup) inflater.inflate(R.layout.fragment_first_page_screen_slide_safety_sitter, container, false);
        }

        ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(myPhoneNumberEditText, InputMethodManager.SHOW_FORCED);

        myPhoneNumberEditText = (EditText) rootView.findViewById(R.id.my_number_phone);
        enterMyPhoneNumberImageView = (ImageView) rootView.findViewById(R.id.btn_image_view_registration);
        enterMyPhoneNumberImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                enterMyPhoneNumberImageView.setImageResource(R.drawable.insert_button_on_tauch);
                return false;
            }
        });
        enterMyPhoneNumberImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterMyPhoneNumberImageView.setEnabled(false);
                myPhoneNumberString = String.valueOf(myPhoneNumberEditText.getText());

                if (!checkValidityNumberPhone(myPhoneNumberString)) {
                    Toast.makeText(getContext(), getResources().getText(R.string.toast_phone_number_invalid), Toast.LENGTH_SHORT).show();
                    enterMyPhoneNumberImageView.setImageResource(R.drawable.insert_button);
                    enterMyPhoneNumberImageView.setEnabled(true);
                    return;
                }



                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        editorPreferencesPhoneNumber = preferencesPhoneNumber.edit();
                        editorPreferencesPhoneNumber.putString(PHONE_KEY + deviceId, myPhoneNumberString);
                        editorPreferencesPhoneNumber.commit();

                        if (!isUserSaved){
                            sendTokenToServer(getActivity(), REGISTRATION_TO_SERVER);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        enterMyPhoneNumberImageView.setImageResource(R.drawable.insert_button);
                        enterMyPhoneNumberImageView.setEnabled(true);
                        ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                        safetysitterRegistrationViewPager.setCurrentItem(1, true);
                    }
                }.execute(myPhoneNumberString);
            }
        });
        return rootView;
    }

}
