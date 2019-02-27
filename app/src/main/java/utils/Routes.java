package utils;

public class Routes {

    // check user login and get data
    public static final String authenticateUser = "http://112.133.223.214/campus/public/index.php/authenticateUser/";

    // leave and outing requests
    public static final String newRequest = "http://10.0.2.2:5000/api/new_request/";
    public static final String closeRequest = "http://10.0.2.2:5000/api/close_request/";
    public static final String myRequests = "http://10.0.2.2:5000/api/my_new_requests/";
    public static final String requestHistory = "http://10.0.2.2:5000/api/my_requests/";

    public static final String newFeedback = "http://10.0.2.2:5000/api/new_feedback/";

    // create message
    public static final String sendOTP = "https://control.msg91.com/api/sendotp.php";

}
