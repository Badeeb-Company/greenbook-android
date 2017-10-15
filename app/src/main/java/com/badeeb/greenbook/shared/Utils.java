package com.badeeb.greenbook.shared;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by meldeeb on 9/30/17.
 */

public class Utils {

    private static final int MAX_PHOTO_FILE_SIZE = 4 * 1024 * 1024; // 15 MB

    public static boolean isAllowedFileSize(Context context, Uri fileUri){
        File file = FileUtils.getFile(context, fileUri);
        if(file == null){
            Toast.makeText(context, "File not found", Toast.LENGTH_LONG).show();
            return false;
        }
        boolean fileSizePermitted = file.length() <= MAX_PHOTO_FILE_SIZE;
        if(!fileSizePermitted){
            Toast.makeText(context, "Maximum file size to upload is 3 MB", Toast.LENGTH_LONG).show();
        }
        return  fileSizePermitted;
    }

    public static byte[] getBytes(InputStream inputStream) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteBuffer.toByteArray();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public static boolean isPhoneNumberValid(String email) {
        String expression = "^[+]?[0-9]{8,25}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
