package campus.smartcampus;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import utils.Constants;
import utils.Routes;
import utils.Snippets;

public class GrievanceRequest extends AppCompatActivity {

    AppCompatEditText grievanceText;
    AppCompatTextView submitGrievance;
    CoordinatorLayout grievanceLayout;
    RelativeLayout bottomSheetProgress;

    Snackbar snackbar;

    // bottom sheet
    BottomSheetBehavior bottomSheetBehavior;
    AppCompatTextView loaderText, resultText;
    AppCompatImageView resultImage;
    RelativeLayout loaderView, resultView;

    // request status
    int status = -3;
    String msg = "";
    JSONObject jsonResponse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grievance_request);

        grievanceText = (AppCompatEditText) findViewById(R.id.grievanceText);
        submitGrievance = (AppCompatTextView) findViewById(R.id.submitGrievance);
        grievanceLayout = (CoordinatorLayout) findViewById(R.id.grievanceLayout);

        // bottom sheet
        bottomSheetProgress = (RelativeLayout) findViewById(R.id.bottomSheetProgress);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetProgress);
        loaderText = (AppCompatTextView) findViewById(R.id.loaderText);
        resultText = (AppCompatTextView) findViewById(R.id.resultText);
        resultImage = (AppCompatImageView) findViewById(R.id.resultImage);
        loaderView = (RelativeLayout) findViewById(R.id.loaderView);
        resultView = (RelativeLayout) findViewById(R.id.resultView);

        // based on the user role, we show the actions

        submitGrievance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(submitGrievance.getWindowToken(), 0);

                // check if text is entered
                if(grievanceText.getText().length() > 3) {

                    // disable leave controls
                    grievanceText.setEnabled(false);
                    submitGrievance.setEnabled(false);

                    // send request
                    new NewFeedback().execute(Routes.newFeedback);


                }
                else {

                    snackbar = Snackbar.make(grievanceLayout, "Grievance description is missing!", Snackbar.LENGTH_LONG);
                    designSnackBar();
                    snackbar.show();
                }
            }
        });
    }


        public void designSnackBar() {

            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(getResources().getColor(R.color.palette_red));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setTextSize(16);
        }



    // Apply leave
    private class NewFeedback extends AsyncTask<String, Void, Void> {

        private String Content = "";
        private String Error = null;
        String data = "";

        @Override
        protected void onPreExecute() {

            // show progress
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetBehavior.setHideable(false);

        }

        @Override
        protected Void doInBackground(String... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                //collegeId = urls[1];

                // Set Request parameter
/*                data += "/" + URLEncoder.encode(Snippets.getLeaveId(), "UTF-8")
                        + "/" + URLEncoder.encode("yeswe02", "UTF-8")
                        + "/" + URLEncoder.encode("PavanKusuma", "UTF-8")
                        + "/" + URLEncoder.encode("IT", "UTF-8")
                        + "/" + URLEncoder.encode("0", "UTF-8")
                        + "/" + URLEncoder.encode((String) leaveText.getText().toString(), "UTF-8")
                        + "/" + URLEncoder.encode(fromDate, "UTF-8")
                        + "/" + URLEncoder.encode(toDate, "UTF-8")
                        + "/" + URLEncoder.encode(String.valueOf(number_of_days), "UTF-8");*/

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.feedbackId, "UTF-8") + "=" + Snippets.getFeedbackId()
                        + "&" + URLEncoder.encode(Constants.userObjectId, "UTF-8") + "=" + "yeswe02"
                        + "&" + URLEncoder.encode(Constants.username, "UTF-8") + "=" + "PavanKusuma"
                        + "&" + URLEncoder.encode(Constants.branch, "UTF-8") + "=" + "IT"
                        + "&" + URLEncoder.encode(Constants.year, "UTF-8") + "=" + 0
                        + "&" + URLEncoder.encode(Constants.description, "UTF-8") + "=" +  URLEncoder.encode((String) grievanceText.getText().toString(), "UTF-8");


                Log.v(Constants.appName, urls[0]+data);

                // Defined URL  where to send data
                java.net.URL url = new URL(urls[0]+data);

                // Send POST data request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                //conn.setDoInput(true);

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
                Log.v(Constants.appName, Content);
                // close the reader
                //reader.close();

            } catch (Exception ex) {

                ex.printStackTrace();
                Error = ex.getMessage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        showErrorMessage();
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
                showErrorMessage();

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
                                case 200:

                                    // show result
                                    loaderView.setVisibility(View.GONE);
                                    resultView.setVisibility(View.VISIBLE);
                                    resultText.setText(msg);

                                    // finish
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    }, Constants.delay);

                                    break;

                                // allow login
                                default:

                                    try {

                                        // show error
                                        showErrorMessage();

                                        break;
                                    }
                                    catch(Exception e){
                                        showErrorMessage();
                                    }


                            }
                        }
                    });





                } catch (JSONException e) {

                    e.printStackTrace();
                    showErrorMessage();
                }

            }

            //nextButton.setEnabled(true);

        }

    }

    public void closeBottomSheet(){

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void showErrorMessage(){
        // close the bottomBehavior and show error message
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // enable views
        grievanceText.setEnabled(true);
        submitGrievance.setEnabled(true);

        // show error message
        snackbar = Snackbar.make(grievanceLayout, "Something went wrong. Please try again!", Snackbar.LENGTH_LONG);
        designSnackBar();
        snackbar.show();

    }
}
