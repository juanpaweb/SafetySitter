package com.example.safetysitter.safetysitter;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.example.safetysitter.safetysitter.RegistrationActivity.TAG;

/**
 * Created by omeredut on 17/11/2016.
 */

public class ServerSocketConnection {

    public static final String HOSTNAME = "192.168.43.7";
    public static final int PORT = 5999;
    public static final int TIMEOUT = 2000;

    public static final int CHECK_EXIST_USER = 101;
    public static final int SIGN_IN_SUCCESS = 102;
    public static final int USER_EXIST = 103;
    public static final int TOKEN_REFRESHED = 104;
    public static final int REGISTRATION = 105;
    public static final int CHECK_PHONE_TO_SEND = 106;
    public static final int FOUND_TOKEN_PHONE_TO_SEND = 107;
    public static final int NOT_FOUND_TOKEN_PHONE_TO_SEND = 108;
    public static final int ERROR = 10;
    public static final int AUDIO_REPORT = 150;
    public static final int NOT_AUDIO_REPORT = 151;
    public static final int VIDEO_REPORT = 152;
    public static final int NOT_VIDEO_REPORT = 153;
    public static final int BROADCAST_EVENT = 110;
    public static final int REFRESH_TOKEN = 123321;


    private String deviceId;
    private String userPhoneNumber;
    private String token;





    public ServerSocketConnection(String deviceId, String myPhone, String token) {
        this.deviceId = deviceId;
        this.userPhoneNumber = myPhone;
        //this.phoneToSend = phoneToSend;
        this.token = token;
        if (token == null){
            this.token = "null";
        }
    }





    public int registration(){
        //this method checked with the server if the number phone is exist.
        //if is not, the server sign in the number phone.
        //the server returns int in respectively of the result.
        //this method have to run in another Thread.

        Socket registrationClientSocket = null;
        int responseRegistration = -1;
        InputStream registrationInputStream = null;
        OutputStream registrationOutputStream = null;


        JSONObject registrationJsonObject = new JSONObject();
        try {
            registrationJsonObject.put("PHONE_NUMBER", userPhoneNumber);
            registrationJsonObject.put("DEVICE_ID_" + userPhoneNumber, deviceId);
            Log.d("omer", "userPhone: "+ userPhoneNumber);
            registrationJsonObject.put("TOKEN_" + userPhoneNumber, token);
            String userPropertiesForRegistration = registrationJsonObject.toString();
            int lengthUserProperties = userPropertiesForRegistration.getBytes().length;
            Log.d("omer", "length user properties is: " + lengthUserProperties);

            registrationClientSocket = new Socket(HOSTNAME, PORT);

            registrationInputStream = registrationClientSocket.getInputStream();
            registrationOutputStream = registrationClientSocket.getOutputStream();

            registrationOutputStream.write(REGISTRATION);
            registrationOutputStream.write(lengthUserProperties);
            registrationOutputStream.write(userPropertiesForRegistration.getBytes());

            responseRegistration = registrationInputStream.read();
            Log.d(TAG, "print response: " + responseRegistration);

        } catch (JSONException e) {
            Log.d(TAG, "json object failed");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d(TAG, "connection to server failed");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (registrationInputStream != null){
                try {
                    registrationInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (registrationOutputStream != null){
                try {
                    registrationOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (registrationClientSocket != null){
                try {
                    registrationClientSocket.close();
                    registrationClientSocket = null;
                    Log.d(TAG, "registrationClientSocket: " + registrationClientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return responseRegistration;
    }





    public int checkPhoneToSend(String phoneToSend){
        //this method checked with the server if the number phone to connect is exist
        // and if it had a token.
        //if is not, returns TOKEN_NOT_FOUND.
        //the server returns int in respectively of the result.
        //this method have to run in another Thread.


        int responseCheckUserExist = -1;
        InputStream checkExistUserInputStream = null;
        OutputStream checkExistUserOutputStream = null;


        JSONObject checkPhoneToSendJsonObject = new JSONObject();
        try {
            checkPhoneToSendJsonObject.put("PHONE_TO_SEND", phoneToSend);
            String phoneToSendProperties = checkPhoneToSendJsonObject.toString();
            int lengthPhoneToSendProperties = phoneToSendProperties.getBytes().length;
            Log.d("omer", "length user properties is: " + lengthPhoneToSendProperties);


            Socket checkExistUserClientSocket = new Socket();
            checkExistUserClientSocket.connect(new InetSocketAddress(HOSTNAME, PORT), TIMEOUT);

            checkExistUserInputStream = checkExistUserClientSocket.getInputStream();
            checkExistUserOutputStream = checkExistUserClientSocket.getOutputStream();

            checkExistUserOutputStream.write(CHECK_PHONE_TO_SEND);
            checkExistUserOutputStream.write(lengthPhoneToSendProperties);
            checkExistUserOutputStream.write(phoneToSendProperties.getBytes());

            responseCheckUserExist = checkExistUserInputStream.read();
            Log.d("omer", "print response: " + responseCheckUserExist);

        } catch (JSONException e) {
            Log.d("omer", "the json object failed");
            System.out.println("the json object failed");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d("omer", "the connection to server failed");
            System.out.println("the connection to server failed");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("omer", e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            if (checkExistUserInputStream != null){
                try {
                    checkExistUserInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (checkExistUserOutputStream != null){
                try {
                    checkExistUserOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseCheckUserExist;
    }



    public int eventReporting(boolean isAudioReport, boolean isVideoReport){
        int responseEventReporting = 0;

        Socket reportingSocket = null;
        InputStream reportingInputStream = null;
        OutputStream reportingOutputStream = null;


        try {
            reportingOutputStream.write(BROADCAST_EVENT);
            if (isAudioReport) {reportingOutputStream.write(AUDIO_REPORT);} else {reportingOutputStream.write(NOT_AUDIO_REPORT);}
            if (isVideoReport) {reportingOutputStream.write(VIDEO_REPORT);} else {reportingOutputStream.write(NOT_VIDEO_REPORT);}




        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseEventReporting;
    }




    public int refreshToken(){
        //this method updates the token of exist user.
        //the server returns int in respectively of the result.
        //this method have to run in another Thread.

        Socket refreshTokenClientSocket = null;
        int refreshTokenRegistration = -1;
        InputStream refreshTokenInputStream = null;
        OutputStream refreshTokenOutputStream = null;


        JSONObject refreshTokenJsonObject = new JSONObject();
        try {
            refreshTokenJsonObject.put("PHONE_NUMBER", userPhoneNumber);
            refreshTokenJsonObject.put("DEVICE_ID_" + userPhoneNumber, deviceId);
            refreshTokenJsonObject.put("TOKEN_" + userPhoneNumber, token);
            String userPropertiesForRefreshToken = refreshTokenJsonObject.toString();
            int lengthUserProperties = userPropertiesForRefreshToken.getBytes().length;

            refreshTokenClientSocket = new Socket();
            refreshTokenClientSocket.connect(new InetSocketAddress(HOSTNAME, PORT), TIMEOUT);

            refreshTokenInputStream = refreshTokenClientSocket.getInputStream();
            refreshTokenOutputStream = refreshTokenClientSocket.getOutputStream();

            refreshTokenOutputStream.write(REFRESH_TOKEN);
            refreshTokenOutputStream.write(lengthUserProperties);
            refreshTokenOutputStream.write(userPropertiesForRefreshToken.getBytes());

            refreshTokenRegistration = refreshTokenInputStream.read();
            Log.d(TAG, "refresh token response: " + refreshTokenRegistration);

        } catch (JSONException e) {
            Log.d(TAG, "json object failed");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d(TAG, "connection to server failed");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (refreshTokenInputStream != null){
                try {
                    refreshTokenInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (refreshTokenOutputStream != null){
                try {
                    refreshTokenOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (refreshTokenClientSocket != null){
                try {
                    refreshTokenClientSocket.close();
                    refreshTokenClientSocket = null;
                    Log.d(TAG, "registrationClientSocket: " + refreshTokenClientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return refreshTokenRegistration;
    }

}
