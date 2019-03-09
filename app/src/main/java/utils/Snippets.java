package utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Snippets {


    public static final String getOTP() {

        // get the universal unique identifier
        //userObjectId = UUID.randomUUID().toString();

        String AB = "0123456789";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder( 5 );
        for( int i = 0; i < 4; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );

        return sb.toString();

    }

    public static final String getRequestId(){ return "R" + getUniqueObjectId(); }

    public static final String getFeedbackId(){ return "G" + getUniqueObjectId(); }

    public static final String getUniqueObjectId() {

        // get the universal unique identifier
        //userObjectId = UUID.randomUUID().toString();

        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder( 9 );
        for( int i = 0; i < 9; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );

        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        return String.valueOf(mDay) + String.valueOf(mMonth) + String.valueOf(mYear) + sb.toString();
    }

    // this method will convert normal string to string which includes hex characters
    public static String escapeURIPathParam(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else{
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%'".indexOf(ch) >= 0;
    }

    public static String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < ba.length; i++)
            str.append(String.format("%x", ba[i]));
        return str.toString();
    }

    public static String fromHexString(String hex) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            str.append((char) Integer.parseInt(hex.substring(i, i + 2), 16));
        }
        return str.toString();
    }

    public static String getDuration(String date){

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy");

        Date convertedDate = new Date();
        Date today = new Date();

/*
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date1 = new Date();
        System.out.println(dateFormat.format(date1));
*/

        try {
            convertedDate = inputFormat.parse(date);


            long secs = (today.getTime() - convertedDate.getTime())/1000;
            long hours = 0;

            if(secs > 3600) {
                hours = secs / 3600;

                // get the hours
                if(hours > 24){

                    // get the days
                    long days = (hours/24);

                    // get months
                    if(days > 31){

                        long months = days/30;

                        if(months > 1) {
                            return months + " months ago";
                        }
                        else {
                            return "a month ago";
                        }

                    }
                    else {

                        if(days > 1) {
                            return days + " days ago";
                        }
                        else {
                            return "a day ago";
                        }

                    }
                }
                else {

                    if(hours > 1) {
                        return hours + " hrs ago";
                    }
                    else {
                        return "an hr ago";
                    }
                }

            }
            else {
                // less than an hour
                long mins = 0;
                if(secs > 60){
                    mins = secs/60;

                    if(mins > 1) {
                        return mins + " mins ago";
                    }
                    else {
                        return "a min ago";
                    }
                }
                else {
                    return secs+" secs ago";
                }
            }


    }
        catch (Exception e){

        }

        return "";

    }


    public static String convertDate(String date){

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy");

        Date convertedDate = new Date();

        try {
            convertedDate = inputFormat.parse(date);
        }
        catch (Exception e){

        }
        return outputFormat.format(convertedDate);
    }
}
