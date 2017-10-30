package com.badeeb.greenbook.shared;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by meldeeb on 9/30/17.
 */

public class Utils {
    private final static String TAG = Utils.class.getName();

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
        String expression = "^[\\w\\.-|\\+]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
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

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        Log.d(TAG, " distance : lat1: "+lat1+" lng1: "+lon1+" - lat2: "+lat2+" lon2: "+lon2);
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c ; // in km

        return Math.round(distance);
    }

    public static int compareTimes(Date d1, Date d2)
    {
        int     t1;
        int     t2;

        t1 = (int) (d1.getTime() % (24*60*60*1000L));
        t2 = (int) (d2.getTime() % (24*60*60*1000L));
        return (t1 - t2);
    }

    public static String getFormattedDate(Date date) {
        Calendar now = Calendar.getInstance();

        Calendar dateTime = Calendar.getInstance();
        dateTime.setTime(date);

        String timeFormatString = "h:mmaa";

        int daysDifference = now.get(Calendar.DATE) - dateTime.get(Calendar.DATE);
        if (daysDifference == 0) {
            int hoursDifference = now.get(Calendar.HOUR) - dateTime.get(Calendar.HOUR);
            if (hoursDifference > 0) {
                return hoursDifference + " hrs";
            } else {
                int minutesDifference = now.get(Calendar.MINUTE) - dateTime.get(Calendar.MINUTE);
                if (minutesDifference > 0) {
                    return minutesDifference + " mins";
                } else {
                    return "Just now";
                }
            }
        } else if (daysDifference == 1) {
            return "Yesterday at " + DateFormat.format(timeFormatString, dateTime);
        } else {
            return DateFormat.format("MMMM dd 'at' h:mmaa", dateTime).toString();
        }
    }
}
