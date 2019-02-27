package utils;

import java.util.Calendar;
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
}
