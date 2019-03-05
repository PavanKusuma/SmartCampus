package utils;

public class Constants {

    public static final String key = "smartcampus_key";
    public static final String appName = "SVECW";
    public static final String app_version = "app_version";
    public static final String app_SharedPreferences = "Smart_SharedPreference";

    public static final String role = "role";
    public static final String student = "Student";
    public static final String faculty = "Faculty";
    public static final String admin = "Admin";

    // otp parameters
    public static final String authkey = "authkey";
    public static final String authkeyValue = "150012AzYaFN8svVjW58fd661a";
    public static final String mobile = "mobile";
    public static final String route = "route";
    public static final String otp = "otp";
    public static final String sender = "sender";

    public static final String charset = "UTF-8";
    public static final String null_indicator = "-";
    public static final String status = "status";
    public static final String data = "data";
    public static final String msg = "msg";
    public static final String type = "type";

    public static final String error = "error";
    public static final String success = "success";
    public static final String failure = "failure";

    public static final String KEY = "key";
    public static final String offset = "offset";
    public static final String collegeId = "collegeId";
    public static final String pushToken = "pushToken";
    public static final String mobileNumber = "mobileNumber";
    public static final String message = "message";

    public static final String notificationNumber = "notificationNumber";
    public static final String id = "id";
    public static final String userImage = "userImage";
    public static final String loginUsername = "username";
    public static final String rating = "rating";
    public static final String departments = "departments";
    public static final String sub_departments = "sub_departments";
    public static final String isSubscribed = "isSubscribed";

    public static final String userObjectId = "userObjectId";
    public static final String username = "username";
    public static final String description = "description";
    public static final String branch = "branch";
    public static final String year = "year";
    public static final String requestFrom = "requestFrom";
    public static final String requestTo = "requestTo";
    public static final String isOpen = "isOpen";
    public static final String duration = "duration";

    public static final String requestId = "requestId";
    public static final String feedbackId = "feedbackId";
    public static final String requests = "requests";
    public static final String requestType = "requestType";
    public static final String requestDate = "requestDate";
    public static final String requestStatus = "requestStatus";
    public static final String leave = "leave";
    public static final String outing = "outing";

    // delay in closing screen
    public static final long delay = 2000;

    // get day
    public static String getDay(int i){

        String val = "SUNDAY";

        switch (i){

            case 2:
                val = "MONDAY";
                break;
            case 3:
                val = "TUESDAY";
                break;
            case 4:
                val = "WEDNESDAY";
                break;
            case 5:
                val = "THURSDAY";
                break;
            case 6:
                val = "FRIDAY";
                break;
            case 7:
                val = "SATURDAY";
                break;
            case 1:
            case 8:
                val = "SUNDAY";
                break;
        }

        return val;

    }

    // get month
    public static String getMonth(int i){

        String val = "JAN";

        switch (i){

            case 1:
                val = "FEB";
                break;
            case 2:
                val = "MAR";
                break;
            case 3:
                val = "APR";
                break;
            case 4:
                val = "MAY";
                break;
            case 5:
                val = "JUN";
                break;
            case 6:
                val = "JUL";
                break;
            case 7:
                val = "AUG";
                break;
            case 8:
                val = "SEP";
                break;
            case 9:
                val = "OCT";
                break;
            case 10:
                val = "NOV";
                break;
            case 11:
                val = "DEC";
                break;
            case 0:
            case 12:
                val = "JAN";
                break;
        }

        return val;
    }

}
