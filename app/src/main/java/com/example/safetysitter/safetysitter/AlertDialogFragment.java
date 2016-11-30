package com.example.safetysitter.safetysitter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * Created by omeredut on 18/11/2016.
 */

public class AlertDialogFragment extends DialogFragment {

    public static final String TITLE_ALERT = "titleAlert";
    public static final String MESSAGE_ALERT = "messageAlert";
    public static final String TEXT_POSITIVE_BUTTON = "textPositiveButton";
    public static final String TEXT_NEGATIVE_BUTTON = "textNegativeButton";
    private String titleAlert;
    private String messageAlert;
    private String textPositiveButton;
    private String textNegativeButton;

    /*public AlertDialogFragment(String titleAlert, String textNegativeButton, String textPositiveButton, String messageAlert){
        this.titleAlert = titleAlert;
        this.messageAlert = messageAlert;
        this.textPositiveButton = textPositiveButton;
        this.textNegativeButton = textNegativeButton;
    }*/

    public AlertDialogFragment(){

    }

    public static final AlertDialogFragment newInstance(String titleAlert, String textNegativeButton, String textPositiveButton, String messageAlert){
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_ALERT, titleAlert);
        args.putString(MESSAGE_ALERT, messageAlert);
        args.putString(TEXT_NEGATIVE_BUTTON, textNegativeButton);
        args.putString(TEXT_POSITIVE_BUTTON, textPositiveButton);
        alertDialogFragment.setArguments(args);
        return alertDialogFragment;
    }

    OnTryAgainListener onTryAgainListener;
    OnContinueToScannerListener onContinueToScannerListener;

    interface OnTryAgainListener{
        void onTryAgain();
        }
    public void setOnTryAgainListener(OnTryAgainListener onTryAgainListener){
        this.onTryAgainListener = onTryAgainListener;
    }

    interface OnContinueToScannerListener{
        void onContinueToScanner();
    }
    public void setOnContinueToScannerListener(OnContinueToScannerListener onContinueToScannerListener){
        this.onContinueToScannerListener = onContinueToScannerListener;
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        titleAlert = getArguments().getString(TITLE_ALERT);
        messageAlert = getArguments().getString(MESSAGE_ALERT);
        textPositiveButton = getArguments().getString(TEXT_POSITIVE_BUTTON);
        textNegativeButton = getArguments().getString(TEXT_NEGATIVE_BUTTON);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_alert, null);
        builder.setCancelable(false)
                .setMessage(messageAlert).setTitle(titleAlert)
                .setPositiveButton(textPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onTryAgainListener != null){
                            onTryAgainListener.onTryAgain();
                        }
                    }
                })
                .setNegativeButton(textNegativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onContinueToScannerListener != null){
                            onContinueToScannerListener.onContinueToScanner();
                        }
                    }
                });
        return builder.create();
    }
}
