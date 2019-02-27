package campus.smartcampus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import utils.Constants;

public class Feed extends Fragment implements Home.FragmentLifeCycle {

    RelativeLayout itemView;
    RecyclerView circularListView, updatesListView;

    public static int circularOffset = 0, updateOffset = 0;
    JSONObject jsonResponse;
    int status, skipCounter = 0;
    String msg = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        itemView = (RelativeLayout) inflater.inflate(R.layout.feed, container, false);

        circularListView = itemView.findViewById(R.id.circularsListView);
        updatesListView = itemView.findViewById(R.id.updatesListView);

        LinearLayoutManager linearLayoutManagerCircular = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        circularListView.setLayoutManager(linearLayoutManagerCircular);
        circularListView.setItemAnimator(new DefaultItemAnimator());
        //circularListView.setAdapter();

        LinearLayoutManager linearLayoutManagerUpdate = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        updatesListView.setLayoutManager(linearLayoutManagerUpdate);
        updatesListView.setItemAnimator(new DefaultItemAnimator());
        //updatesListView.setAdapter();

        return itemView;
    }

    @Override
    public void onResumeFragment() {

    }

    // Get Circulars and Updates
/*
    private class GetFeed extends AsyncTask<String, Void, Void> {

        private String Content = "";
        private String Error = null;
        String data = "";

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... urls) {

            */
/************ Make Post Call To Web Server ***********//*

            BufferedReader reader = null;

            // Send data
            try {

                //collegeId = urls[1];

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.KEY, "UTF-8") + "=" + Constants.key
                        + "&" + URLEncoder.encode(Constants.offset, "UTF-8") + "=" + circularOffset;

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

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //Toast.makeText(AuthenticateUser.this, "Please try again", Toast.LENGTH_SHORT).show();

                        // update the UI

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
                */
/****************** Start Parse Response JSON Data *************//*



                try {

                    */
/****** Creates a new JSONObject with name/value mappings from the JSON string. ********//*

                    jsonResponse = new JSONObject(Content);


                    */
/***** Returns the value mapped by name if it exists and is a JSONArray. ***//*

                    status = jsonResponse.getInt(Constants.status);
                    msg = jsonResponse.getString(Constants.msg);

                    getActivity().runOnUiThread(new Runnable() {
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
                                        */
/*Intent homeIntent = new Intent(getApplicationContext(), GlobalLoginOTP.class);
                                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        homeIntent.putExtra(Constants.userObjectId, str_collegeId);
                                        homeIntent.putExtra(Constants.collegeId, str_collegeId);
                                        homeIntent.putExtra(Constants.phoneNumber, str_phoneNumber);
                                        homeIntent.putExtra(Constants.otpCode, otpCode);
                                        startActivity(homeIntent);
                                        finish();*//*

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
*/

}
