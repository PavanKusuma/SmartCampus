package campus.smartcampus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import utils.Constants;
import utils.GetOneSignalId;
import utils.Routes;
import utils.Snippets;

public class Login extends AppCompatActivity {

    AppCompatButton login, submit;
    AppCompatEditText mobileNumber, collegeId, otp;
    AppCompatTextView errorText, info;
    ProgressBar progressBarLogin, progressBarOTP;
    RelativeLayout layout_one, layout_two;

    // token for sending notifications
    public String token = Constants.null_indicator;

    // request status
    int status = -3;
    String msg = "";
    JSONObject jsonResponse;
    public static String otpCode = "1111";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // get OneSignal Id of the user
        // register device to send push notifications
        invokeOneSignal();

        login = (AppCompatButton) findViewById(R.id.loginBtn);
        submit = (AppCompatButton) findViewById(R.id.submitBtn);
        mobileNumber = (AppCompatEditText) findViewById(R.id.mobileNumber);
        collegeId = (AppCompatEditText) findViewById(R.id.collegeId);
        otp = (AppCompatEditText) findViewById(R.id.otp);
        errorText = (AppCompatTextView) findViewById(R.id.errorText);
        info = (AppCompatTextView) findViewById(R.id.infoText);
        progressBarOTP = (ProgressBar) findViewById(R.id.progressBarOTP);
        progressBarLogin = (ProgressBar) findViewById(R.id.progressBarLogin);
        layout_one = (RelativeLayout) findViewById(R.id.layout_one);
        layout_two = (RelativeLayout) findViewById(R.id.layout_two);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // check if required inputs are present
                if (collegeId.length() > 0 && mobileNumber.length() >= 10) {

                    progressBarLogin.setVisibility(View.VISIBLE);

                    // check if pushy token is fetched
                    if(token.contentEquals(Constants.null_indicator)) {
                        new Authentication().execute(Routes.authenticateUser, collegeId.getText().toString());
                    }
                    else {
                        progressBarLogin.setVisibility(View.GONE);
                        // disable the errorText view
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Try again");
                        //nextButton.setEnabled(true);
                    }

                } else {

                    // enable go button to re submit the login credentials
                    //nextButton.setEnabled(true);

                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("No required data");
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // length
                if(otp.getText().toString().length() == 4){

                    progressBarOTP.setVisibility(View.VISIBLE);

                    // verify
                    if(otp.getText().toString() == otpCode){

                        progressBarOTP.setVisibility(View.GONE);

                        Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                        startActivity(homeIntent);
                        finish();
                    }
                    else {

                        progressBarOTP.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("OTP is incorrect");
                    }
                }
                else {

                    progressBarOTP.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Enter Valid OTP");
                }
            }
        });
    }

    // toggles between login & OTP UI
    public void closeOne(){

        progressBarLogin.setVisibility(View.GONE);
        layout_one.setVisibility(View.GONE);

        progressBarOTP.setVisibility(View.GONE);
        layout_two.setVisibility(View.VISIBLE);

        errorText.setText("");
    }
    public void closeTwo(){

        progressBarLogin.setVisibility(View.GONE);
        layout_one.setVisibility(View.GONE);

        progressBarOTP.setVisibility(View.GONE);
        layout_two.setVisibility(View.GONE);

        errorText.setText("");
    }
    public void closeTwoAndOpenOne(){

        progressBarLogin.setVisibility(View.GONE);
        layout_one.setVisibility(View.VISIBLE);

        progressBarOTP.setVisibility(View.GONE);
        layout_two.setVisibility(View.GONE);

        errorText.setText("");
    }


    private void invokeOneSignal() {

        try {

            token = new GetOneSignalId().execute().get();

        }
        catch (Exception e){

        }
    }


    // Authenticate User
    private class Authentication extends AsyncTask<String, Void, Void> {

        private String Content = "";
        private String Error = null;
        String data = "";
        //String collegeId = "";

        @Override
        protected void onPreExecute() {

            //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            //StrictMode.setThreadPolicy(policy);



        }

        @Override
        protected Void doInBackground(String... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                //collegeId = urls[1];

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.KEY, "UTF-8") + "=" + Constants.key
                        + "&" + URLEncoder.encode(Constants.collegeId, "UTF-8") + "=" + (collegeId.getText().toString())
                        + "&" + URLEncoder.encode(Constants.pushToken, "UTF-8") + "=" + (token)
                        + "&" + URLEncoder.encode(Constants.mobileNumber, "UTF-8") + "=" + (mobileNumber.getText().toString());

                Log.v(Constants.appName, urls[0]+data);

                // Defined URL  where to send data
                java.net.URL url = new URL(urls[0]+data);

                // Send POST data request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                //conn.setDoInput(true);
                //OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                //wr.write(data);
                //wr.flush();

                // Get the server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // Append Server Response To Content String
                Content = sb.toString();

                // close the reader
                //reader.close();

            } catch (Exception ex) {

                ex.printStackTrace();
                Error = ex.getMessage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //Toast.makeText(AuthenticateUser.this, "Please try again", Toast.LENGTH_SHORT).show();

                        progressBarLogin.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Error, Try again");
                    }
                });


            } finally {

                try {

                    reader.close();

                } catch (Exception ex) {
                    Error = ex.getMessage();
                }
            }

            return null;
        }

        protected void onPostExecute(Void unused) {

            if (Error != null) {

                Log.i("Connection", Error);

            } else {

                //Log.i("Connection", Content);
                /****************** Start Parse Response JSON Data *************/


                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(Content);


                    /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                    status = jsonResponse.getInt(Constants.status);
                    msg = jsonResponse.getString(Constants.msg);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // check the status and proceed with the logic
                            switch (status){

                                // exception occurred
                                case -4:

                                    progressBarLogin.setVisibility(View.GONE);
                                    errorText.setVisibility(View.VISIBLE);
                                    errorText.setText(msg);
                                    break;

                                // phone number exists
                                case -3:

                                    progressBarLogin.setVisibility(View.GONE);
                                    errorText.setVisibility(View.VISIBLE);
                                    errorText.setText(msg);
                                    break;

                                // exception occurred
                                case -2:

                                    progressBarLogin.setVisibility(View.GONE);
                                    errorText.setVisibility(View.VISIBLE);
                                    errorText.setText(msg);
                                    break;

                                // invalid college Id, user not present in db
                                case -1:

                                    progressBarLogin.setVisibility(View.GONE);
                                    errorText.setVisibility(View.VISIBLE);
                                    errorText.setText(msg);
                                    break;

                                // allow login
                                case 0:

                                    progressBarLogin.setVisibility(View.GONE);
                                    errorText.setVisibility(View.VISIBLE);
                                    errorText.setText(msg);

                                    try {

                                        // send the otp message to user's phone number
                                        //otpCode = Snippets.getOTP();
                                        //sendOTP(otpCode);

                                        // navigate to home activity
                                        Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                                        startActivity(homeIntent);
                                        finish();
                                        /*Intent homeIntent = new Intent(getApplicationContext(), GlobalLoginOTP.class);
                                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        homeIntent.putExtra(Constants.userObjectId, str_collegeId);
                                        homeIntent.putExtra(Constants.collegeId, str_collegeId);
                                        homeIntent.putExtra(Constants.phoneNumber, str_phoneNumber);
                                        homeIntent.putExtra(Constants.otpCode, otpCode);
                                        startActivity(homeIntent);
                                        finish();*/
                                        break;
                                    }
                                    catch(Exception e){

                                        Log.e(Constants.error, e.getMessage());
                                        progressBarLogin.setVisibility(View.GONE);
                                        errorText.setVisibility(View.VISIBLE);
                                        errorText.setText("Please try again!");
                                    }


                            }
                        }
                    });





                } catch (JSONException e) {

                    e.printStackTrace();
                    progressBarLogin.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Please try again!");
                }

            }

            //nextButton.setEnabled(true);

        }

    }

    public void sendOTP(String otpCode){

            /*//Initialize soap request + add parameters
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            //Use this to add parameters
            request.addProperty("strID", "dvnraju@svecw.edu.in");
            request.addProperty("strPwd", "SVECW123");
            request.addProperty("strPhNo", str_secretCode);
            //request.addProperty("strPhNo","7799813519");
            request.addProperty("strText", otpCode + " - Your OTP for SmartCampus App login ");
            request.addProperty("strSchedule", "");
            request.addProperty("intRetryMin", "100");
            request.addProperty("strSenderID", "SVECWB");
            request.addProperty("strSenderNo", "9542918778");

            //Declare the version of the SOAP request
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            //Needed to make the internet call
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.debug = true;
            try {
                //this is the actual part that will call the webservice
                androidHttpTransport.call(SOAP_ACTION, envelope);

                String result = envelope.getResponse().toString();

            } catch (Exception e) {
                e.printStackTrace();

                Log.e(Constants.appName, e.getMessage());
            }*/


        new SendOTPToUser().execute(Routes.sendOTP, otpCode);

    }

    // send otp to the user
    private class SendOTPToUser extends AsyncTask<String, Void, Void>{

        private String Content = "";
        private String Error = null;
        String data = "";
        String collegeId = "";

        @Override
        protected void onPreExecute() {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

        }

        @Override
        protected Void doInBackground(String... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                collegeId = urls[1];

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.authkey, "UTF-8") + "=" + Constants.authkeyValue
                        + "&" + URLEncoder.encode(Constants.mobile, "UTF-8") + "=" + (mobileNumber.getText().toString())
                        + "&" + URLEncoder.encode(Constants.message, "UTF-8") + "=" + Snippets.escapeURIPathParam(urls[1] + " - Your OTP for SmartCampus login ")
                        + "&" + URLEncoder.encode(Constants.otp, "UTF-8") + "=" + (urls[1])
                        + "&" + URLEncoder.encode(Constants.sender, "UTF-8") + "=" + ("CAMPUS");

                Log.v(Constants.appName, urls[0]+data);

                // Defined URL  where to send data
                URL url = new URL(urls[0]+data);

                // Send POST data request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                //conn.setDoInput(true);
                //OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                //wr.write(data);
                //wr.flush();

                // Get the server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // Append Server Response To Content String
                Content = sb.toString();

                // close the reader
                //reader.close();

            } catch (Exception ex) {

                ex.printStackTrace();
                Error = ex.getMessage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //Toast.makeText(AuthenticateUser.this, "Please try again", Toast.LENGTH_SHORT).show();

                        progressBarLogin.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Please try again!");
                    }
                });


            } finally {

                try {

                    reader.close();

                } catch (Exception ex) {
                    Error = ex.getMessage();
                }
            }

            return null;
        }

        protected void onPostExecute(Void unused) {

            if (Error != null) {

                Log.i("Connection", Error);

            } else {

                Log.i("Connection", Content);
                /****************** Start Parse Response JSON Data *************/


                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(Content);


                    /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                    final String type = jsonResponse.getString(Constants.type);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //*//* check the status and proceed with the logic
                            switch (type){

                                // OTP send successfully, navigate to OTP screen
                                case Constants.success:

                                    try {
                                        // navigate to home activity by fetching the previously saved data
                                        /*Intent homeIntent = new Intent(getApplicationContext(), GlobalLoginOTP.class);
                                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        homeIntent.putExtra(Constants.userObjectId, str_collegeId);
                                        homeIntent.putExtra(Constants.collegeId, str_collegeId);
                                        homeIntent.putExtra(Constants.phoneNumber, str_phoneNumber);
                                        //homeIntent.putExtra(Constants.email, (String) smartCampusDB.getUser().get(Constants.email));
                                        homeIntent.putExtra(Constants.otpCode, otpCode);
                                        startActivity(homeIntent);


                                        finish();*/


                                        // show up the OTP input here
                                        closeOne();

                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }

                                    break;

                                // Failure, try again
                                case Constants.failure:
                                    progressBarLogin.setVisibility(View.GONE);
                                    //Toast.makeText(getApplicationContext(), R.string.errorMsg, Toast.LENGTH_SHORT).show();
                                    errorText.setVisibility(View.VISIBLE);
                                    errorText.setText("Please try again!");
                                    break;




                            }
                        }
                    });





                } catch (Exception e) {

                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext(), "Oops! something went wrong, try again later", Toast.LENGTH_SHORT).show();

                    progressBarLogin.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Please try again!");
                }

            }

            //nextButton.setEnabled(true);

        }

    }
}
