package campus.smartcampus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import internal.SmartSessionManager;
import utils.Constants;
import utils.Routes;
import utils.Snippets;

public class LeaveRequest extends AppCompatActivity {

    AppCompatTextView fromDateText, fromDayText, toDateText, toDayText, durationText, submitLeave;
    AppCompatImageView cancelLeave;
    AppCompatEditText leaveText;
    RelativeLayout fromDateLayout, toDateLayout, bottomSheetProgress;
    Snackbar snackbar;
    CoordinatorLayout leaveLayout;

    // bottom sheet
    BottomSheetBehavior bottomSheetBehavior;
    AppCompatTextView loaderText, resultText;
    AppCompatImageView resultImage;
    RelativeLayout loaderView, resultView;

    // calendar
    Calendar fromCalendar, toCalendar, extraToCalendar;
    String fromDate, toDate;
    static int number_of_days = 1;

    // request status
    int status = -3;
    String msg = "";
    JSONObject jsonResponse;

    // session
    SmartSessionManager smartSessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leave_request);

        fromDateText = (AppCompatTextView) findViewById(R.id.fromDateText);
        fromDayText = (AppCompatTextView) findViewById(R.id.fromDayText);
        toDateText = (AppCompatTextView) findViewById(R.id.toDateText);
        toDayText = (AppCompatTextView) findViewById(R.id.toDayText);
        durationText = (AppCompatTextView) findViewById(R.id.durationText);
        leaveText = (AppCompatEditText) findViewById(R.id.leaveText);
        submitLeave = (AppCompatTextView) findViewById(R.id.submitLeave);
        cancelLeave = (AppCompatImageView) findViewById(R.id.cancelLeave);

        fromDateLayout = (RelativeLayout) findViewById(R.id.fromDateLayout);
        toDateLayout = (RelativeLayout) findViewById(R.id.toDateLayout);
        leaveLayout = (CoordinatorLayout) findViewById(R.id.leaveLayout);

        // bottom sheet
        bottomSheetProgress = (RelativeLayout) findViewById(R.id.bottomSheetProgress);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetProgress);
        loaderText = (AppCompatTextView) findViewById(R.id.loaderText);
        resultText = (AppCompatTextView) findViewById(R.id.resultText);
        resultImage = (AppCompatImageView) findViewById(R.id.resultImage);
        loaderView = (RelativeLayout) findViewById(R.id.loaderView);
        resultView = (RelativeLayout) findViewById(R.id.resultView);

        fromCalendar = Calendar.getInstance();
        // update
        updateCheckInAndCheckOut(fromCalendar);

        smartSessionManager = new SmartSessionManager(this);

        fromDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //fromCalendar = Calendar.getInstance();
                int mYear = fromCalendar.get(Calendar.YEAR);
                int mMonth = fromCalendar.get(Calendar.MONTH);
                int mDay = fromCalendar.get(Calendar.DAY_OF_MONTH);

                // this will open up with the selected date
                DatePickerDialog dpDialog = new DatePickerDialog(LeaveRequest.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                        // set the checkIn calendar instance for getting day of the selected date
                        fromCalendar.set(year,monthOfYear,dayOfMonth);
                        Log.v(Constants.appName, "Here is : " + year + ","+monthOfYear+","+dayOfMonth);

                        // update check in and check out
                        updateCheckInAndCheckOut(fromCalendar);

                    }
                }
                        , mYear, mMonth, mDay);

                // set the min and max date of check in calendar
                Calendar calendar = Calendar.getInstance();
                long startOfMonth = calendar.getTimeInMillis();
                calendar.set(Calendar.DATE, mDay + 365);
                long endOfMonth = calendar.getTimeInMillis();

                dpDialog.getDatePicker().setMinDate(startOfMonth);
                dpDialog.getDatePicker().setMaxDate(endOfMonth);
                dpDialog.show();

                Date startdate = new Date(startOfMonth);
                Log.v(Constants.appName, "Here is start : " + startdate);
                Date enddate = new Date(endOfMonth);
                Log.v(Constants.appName, "Here is end : " + enddate);
            }
        });

        toDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // calendar instance
                //checkOutCalendar = Calendar.getInstance();
                int mYear = toCalendar.get(Calendar.YEAR);
                int mMonth = toCalendar.get(Calendar.MONTH);
                int mDay = toCalendar.get(Calendar.DAY_OF_MONTH);
                int mDaySet = fromCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpDialog = new DatePickerDialog(LeaveRequest.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                        // get the selected date
                        toCalendar.set(year,monthOfYear,dayOfMonth);

                        // update only check out calendar
                        updateOnlyCheckOut(toCalendar);

                    }


                }
                        , mYear, mMonth, mDay);


                // check min and max date of check out calendar
                Calendar calendar = Calendar.getInstance();
                //calendar.set(outCalendar.get(Calendar.YEAR), outCalendar.get(Calendar.MONTH), outCalendar.get(Calendar.DAY_OF_MONTH + 1));
                calendar.set(fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH) );
                long startOfMonth = calendar.getTimeInMillis();

                GregorianCalendar cal = new GregorianCalendar();
                Date startdate = new Date(startOfMonth);
                cal.setTime(startdate);
                cal.add(Calendar.DATE, 15);
                long endOfMonth = cal.getTimeInMillis();

                dpDialog.getDatePicker().setMinDate(startOfMonth);
                dpDialog.getDatePicker().setMaxDate(endOfMonth);
                dpDialog.show();
            }
        });

        submitLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // close the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(leaveText.getWindowToken(), 0);

                // check for leave reason
                if(leaveText.getText().length() > 3) {

                    // disable leave controls
                    leaveText.setEnabled(false);
                    submitLeave.setEnabled(false);


                    // send request
                    new NewLeave().execute(Routes.newRequest);


                }
                else {

                    snackbar = Snackbar.make(leaveLayout, "Leave reason is missing!", Snackbar.LENGTH_LONG);
                    designSnackBar();
                    snackbar.show();
                }
                
            }
        });

        cancelLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        // hide initially
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    // update from and to date
    public void updateCheckInAndCheckOut(Calendar calendar){

        int iYear = fromCalendar.get(Calendar.YEAR);
        int iMonth = fromCalendar.get(Calendar.MONTH);
        int iDay = fromCalendar.get(Calendar.DAY_OF_MONTH);
        int iDate = fromCalendar.get(Calendar.DAY_OF_WEEK);

        // check in
        fromDateText.setText(String.valueOf(iDay) + " " + Constants.getMonth(iMonth)); // set the check in date
        fromDayText.setText(Constants.getDay(iDate)); // set the check in day
        fromDate = String.valueOf(iYear) + "-" + String.valueOf(iMonth+1) + "-" + String.valueOf(iDay);
        //session.saveCheckIn(checkInDate);
        Log.v(Constants.appName, "Here is from date : " + fromDate);

        try {

            // get the check in date to set toCalendar
            String checkIn = String.valueOf(iYear) + "-" + String.valueOf(iMonth+1) + "-" + String.valueOf(iDay);;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = format.parse(checkIn);
            System.out.println(date);

            toCalendar = Calendar.getInstance();
            toCalendar.setTime(date);
//            toCalendar.add(Calendar.DATE, 1);
            updateOnlyCheckOut(toCalendar);
            //toCalendar.set(Calendar.DATE, iDay + 15);

        }
        catch (Exception e){

        }

    }

    // set only from date based on from date
    public void updateOnlyCheckOut(Calendar calendar){

        //toCalendar = calendar;
        //toCalendar.add(Calendar.DATE, 1);
        int oYear = toCalendar.get(Calendar.YEAR);
        int oMonth = toCalendar.get(Calendar.MONTH);
        int oDay = toCalendar.get(Calendar.DAY_OF_MONTH);
        int oDate = toCalendar.get(Calendar.DAY_OF_WEEK);

        // check out
        toDateText.setText(String.valueOf(oDay) + " " + Constants.getMonth(oMonth)); // set the check out date
        toDayText.setText(Constants.getDay(oDate)); // set the check out day
        toDate = String.valueOf(oYear) + "-" +  String.valueOf(oMonth+1) + "-" +  String.valueOf(oDay);
        //session.saveCheckOut(toDate);

        //Log.v(Constants.appName, "Here is check out date : " + toDate);

        // set the out calendar date as + 1 of the selected check in date
        extraToCalendar = toCalendar;

        // update the number of nights based on the selected dates
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // get the dates diff
            long diff = format.parse(toDate).getTime() - format.parse(fromDate).getTime();

            number_of_days = (int) diff / (24 * 60 * 60 * 1000);
            number_of_days = Math.abs(number_of_days);

            //Log.v(Constants.appName, "Number of nights : " + number_of_days);
            durationText.setText(String.valueOf(number_of_days+1) + " Day(s)");

            //session.saveNightsCount(number_of_days);
        }
        catch (Exception e){

            // check
        }

    }

    public void designSnackBar() {

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.palette_red));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setTextSize(16);
    }

    // Apply leave
    private class NewLeave extends AsyncTask<String, Void, Void> {

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
                data += "?&" + URLEncoder.encode(Constants.requestId, "UTF-8") + "=" + Snippets.getRequestId()
                        + "&" + URLEncoder.encode(Constants.requestType, "UTF-8") + "=" + "Leave"
                        + "&" + URLEncoder.encode(Constants.collegeId, "UTF-8") + "=" + "yeswe02"
                        + "&" + URLEncoder.encode(Constants.username, "UTF-8") + "=" + "PavanKusuma"
                        + "&" + URLEncoder.encode(Constants.branch, "UTF-8") + "=" + "IT"
                        + "&" + URLEncoder.encode(Constants.year, "UTF-8") + "=" + 0
                        + "&" + URLEncoder.encode(Constants.description, "UTF-8") + "=" +  URLEncoder.encode((String) leaveText.getText().toString(), "UTF-8")
                        + "&" + URLEncoder.encode(Constants.requestFrom, "UTF-8") + "=" + fromDate
                        + "&" + URLEncoder.encode(Constants.requestTo, "UTF-8") + "=" + toDate
                        + "&" + URLEncoder.encode(Constants.duration, "UTF-8") + "=" + number_of_days;


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

                                    // increment the request count in session
                                    smartSessionManager.updateOpenRequestsCount();

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
        leaveText.setEnabled(true);
        submitLeave.setEnabled(true);

        // show error message
        snackbar = Snackbar.make(leaveLayout, "Something went wrong. Please try again!", Snackbar.LENGTH_LONG);
        designSnackBar();
        snackbar.show();

    }
}
