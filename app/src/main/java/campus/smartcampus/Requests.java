package campus.smartcampus;

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
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import internal.SmartSessionManager;
import modal.Request;
import utils.Constants;
import utils.Routes;
import utils.Snippets;

public class Requests extends AppCompatActivity {

    RelativeLayout activeRequestLayout;
    AppCompatTextView leave, outing, getOldRequests;
    RecyclerView requestsListView, requestsHistoryListView;
    CoordinatorLayout requestsLayout;
    Snackbar snackbar;
    ProgressBar requestsHistoryProgress;
    // bottom sheet for loading more circulars
    BottomSheetBehavior bottomSheetBehavior;
    CoordinatorLayout requestsHistoryBottomSheetLayout;
    AppCompatImageView closeRequestsHistory;

    NewRequestsAdapter requestsAdapter;
    OldRequestsAdapter oldRequestsAdapter;

    // session
    SmartSessionManager smartSessionManager;

    // offset
    public static int offset = 0, oldItemsOffSet = 0;
    boolean isScrolling = false;
    int currentItems, totalItems, scrollOutItems;

    boolean requestsDataExists = true;

    // request status
    int status = -3;
    String msg = "";
    JSONArray requestsData, oldRequestsData;
    JSONObject jsonResponse;
    ArrayList<Request> requests = new ArrayList<Request>();
    ArrayList<Request> oldRequests = new ArrayList<Request>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requests);
    
        leave = (AppCompatTextView) findViewById(R.id.leave);
        outing = (AppCompatTextView) findViewById(R.id.outing);
        getOldRequests = (AppCompatTextView) findViewById(R.id.getOldRequests);
        activeRequestLayout = (RelativeLayout) findViewById(R.id.activeRequestLayout);
        requestsListView = (RecyclerView) findViewById(R.id.requestsListView);
        requestsLayout = (CoordinatorLayout) findViewById(R.id.requestsLayout);
        // get the bottom sheet
        requestsHistoryBottomSheetLayout = (CoordinatorLayout) findViewById(R.id.requestsHistoryBottomSheetLayout);
        bottomSheetBehavior = BottomSheetBehavior.from(requestsHistoryBottomSheetLayout);
        // hide initially
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        requestsHistoryListView = (RecyclerView) findViewById(R.id.requestsHistoryListView);
        requestsHistoryProgress = (ProgressBar) findViewById(R.id.requestsHistoryProgress);
        closeRequestsHistory = (AppCompatImageView) findViewById(R.id.closeRequestsHistory);

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent leaveIntent = new Intent(Requests.this, LeaveRequest.class);
                startActivity(leaveIntent);
            }
        });

        outing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent outingIntent = new Intent(Requests.this, OutingRequest.class);
                startActivity(outingIntent);
            }
        });

        getOldRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // initially show already loaded circulars
                oldRequests.clear();

                // more data exists
                requestsDataExists = true;

                // put offset to 0
                oldItemsOffSet = 0;

                // show progress
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                requestsHistoryProgress.setVisibility(View.VISIBLE);
            }
        });

        // close all circulars view
        closeRequestsHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestsHistoryProgress.setVisibility(View.GONE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {

                // show if bottomSheet is up
                if(i == BottomSheetBehavior.STATE_EXPANDED){

                    oldRequestsAdapter = new OldRequestsAdapter(oldRequests);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Requests.this, LinearLayoutManager.VERTICAL, false);
                    requestsHistoryListView.setLayoutManager(layoutManager);
                    requestsHistoryListView.setItemAnimator(new DefaultItemAnimator());
                    requestsHistoryListView.setAdapter(oldRequestsAdapter);
                    oldRequestsAdapter.notifyDataSetChanged();

                    // get old requests
                    new GetMyRequestsList().execute(Routes.requestHistory);

                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        // Check if any active requests from current session
        // if any, retrieve the details
        // after fetching details, if request status is CLOSED, then delete request from current session
        smartSessionManager = new SmartSessionManager(this);
        if(smartSessionManager.getRequestCount() > 0){

            // get the pending requests with the userObjectId
            Log.v(Constants.appName, "Requests here");

            requestsAdapter = new NewRequestsAdapter(requests);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            requestsListView.setLayoutManager(layoutManager);
            requestsListView.setItemAnimator(new DefaultItemAnimator());
            requestsListView.setAdapter(requestsAdapter);
            requestsAdapter.notifyDataSetChanged();

            new GetNewRequestsList().execute(Routes.myRequests);

        }


    }

    // New Requests
    private class GetNewRequestsList extends AsyncTask<String, Void, Void> {

        private String response = "";
        private String Error = null;
        String data = "";

        @Override
        protected void onPreExecute() {

            // show progress

        }

        @Override
        protected Void doInBackground(String... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.collegeId, "UTF-8") + "=" + "yeswe02"
                        + "&" + URLEncoder.encode(Constants.offset, "UTF-8") + "=" + offset;



                URL url = new URL(urls[0]+ data);

                // connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            showErrorMessage();
                        }
                    });
                }

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                // read the response
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // get the server response
                response = sb.toString();

                // close connection
                reader.close();
                conn.disconnect();


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

                //Log.i("Connection", response);
                /****************** Start Parse Response JSON Data *************/


                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(response);


                    /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                    status = jsonResponse.getInt(Constants.status);
                    msg = jsonResponse.getString(Constants.msg);
                    requestsData = jsonResponse.getJSONArray(Constants.data);

                            // check the status and proceed with the logic
                            switch (status){

                                // exception occurred
                                case 200:

                                    try {

                                        // populate the list view
                                        for (int i=0; i<requestsData.length(); i++) {

                                            Request request = new Request();
                                             request.setRequestId(requestsData.getJSONObject(i).getString(Constants.requestId));
                                             request.setRequestType(requestsData.getJSONObject(i).getString(Constants.requestType));
                                             request.setDescription(requestsData.getJSONObject(i).getString(Constants.description));
                                             request.setRequestDate(requestsData.getJSONObject(i).getString(Constants.requestDate));
                                             request.setRequestStatus(requestsData.getJSONObject(i).getString(Constants.requestStatus));
                                             request.setRequestFrom(requestsData.getJSONObject(i).getString(Constants.requestFrom));
                                             request.setRequestTo(requestsData.getJSONObject(i).getString(Constants.requestTo));
                                             request.setIsOpen(requestsData.getJSONObject(i).getInt(Constants.isOpen));

                                            requests.add(request);
                                        }

                                        // notify requests
                                        requestsAdapter.notifyDataSetChanged();


                                        break;
                                    }
                                    catch(Exception e){
                                        e.printStackTrace();
                                        showErrorMessage();
                                    }

                                    break;

                                // allow login
                                default:

                                    try {

                                        // show error
                                        showErrorMessage();

                                        break;
                                    }
                                    catch(Exception e){
                                        e.printStackTrace();
                                        showErrorMessage();
                                    }
                            }

                } catch (Exception e) {

                    e.printStackTrace();
                    showErrorMessage();
                }

            }

            //nextButton.setEnabled(true);

        }

    }

    public class NewRequestsAdapter extends RecyclerView.Adapter<NewRequestsAdapter.RequestsViewHolder> {

        ArrayList<Request> myRequestsList;

        // constructor
        public NewRequestsAdapter(ArrayList<Request> requestsList) {

            myRequestsList = requestsList;

        }

        class RequestsViewHolder extends RecyclerView.ViewHolder{

            AppCompatTextView requestType, requestDescription, requestDate, requestStatus, requestStatusMessage, closeRequest;
            ProgressBar closeProgressBar;

            RequestsViewHolder(View view) {
                super(view);

                requestType = (AppCompatTextView) view.findViewById(R.id.requestType);
                requestDescription = (AppCompatTextView) view.findViewById(R.id.requestDescription);
                requestDate = (AppCompatTextView) view.findViewById(R.id.requestDate);
                requestStatus = (AppCompatTextView) view.findViewById(R.id.requestStatus);
                requestStatusMessage = (AppCompatTextView) view.findViewById(R.id.requestStatusMessage);

                closeRequest = (AppCompatTextView) view.findViewById(R.id.closeRequest);
                closeProgressBar = (ProgressBar) view.findViewById(R.id.closeProgressBar);

                //hereText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.link_white_24dp,0,0,0);

                Log.v(Constants.appName, "one");
            }


        }

        @Override
        public RequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_request_singleitem, parent, false);
            Log.v(Constants.appName, "two");
            return new RequestsViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(final RequestsViewHolder holder, int i) {

            final int position = i;
            try {Log.v(Constants.appName, "Three");

                Log.v(Constants.appName, "Ok : "+myRequestsList.get(i).getRequestType());

                // set title and created At
                holder.requestType.setText(myRequestsList.get(i).getRequestType());
                holder.requestDate.setText(Snippets.convertDate(myRequestsList.get(i).getRequestFrom()) + " - " + Snippets.convertDate(myRequestsList.get(i).getRequestTo()));
                holder.requestDescription.setText(myRequestsList.get(i).getDescription());
                holder.requestStatus.setText(myRequestsList.get(i).getRequestStatus());

                // Only show status message if comment is available
                holder.requestStatusMessage.setVisibility(View.GONE);
                holder.requestStatusMessage.setText("");
                holder.closeRequest.setVisibility(View.GONE);
                holder.closeProgressBar.setVisibility(View.GONE);

                // status display
                if(myRequestsList.get(i).getRequestStatus().contentEquals("Rejected")){
                    holder.requestStatus.setTextColor(getResources().getColor(R.color.red));

                    // check the request type
                    if(myRequestsList.get(i).getRequestType().equalsIgnoreCase(Constants.leave) && myRequestsList.get(i).getIsOpen() == 1){
                        holder.closeRequest.setVisibility(View.VISIBLE);
                    }

                }else if(myRequestsList.get(i).getRequestStatus().contentEquals("Approved")){
                    holder.requestStatus.setTextColor(getResources().getColor(R.color.green));

                    // check the request type
                    if(myRequestsList.get(i).getRequestType().equalsIgnoreCase(Constants.leave) && myRequestsList.get(i).getIsOpen() == 1){
                        holder.closeRequest.setVisibility(View.VISIBLE);
                    }

                }else if(myRequestsList.get(i).getRequestStatus().contentEquals("Submitted")){
                    holder.requestStatus.setTextColor(getResources().getColor(R.color.green));

                    holder.requestStatusMessage.setVisibility(View.VISIBLE);
                    holder.requestStatusMessage.setText("Waiting for approval! ");

                }else if(myRequestsList.get(i).getRequestStatus().contentEquals("Issued")){
                    holder.requestStatus.setTextColor(getResources().getColor(R.color.green));
                }

                // Only show status message if comment is available
                if(myRequestsList.get(i).getComment()!=null){
                    holder.requestStatusMessage.setVisibility(View.VISIBLE);
                    holder.requestStatusMessage.append(myRequestsList.get(i).getComment());
                }

                // close leave request
                holder.closeRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // hide the close button
                        holder.closeRequest.setVisibility(View.GONE);
                        holder.closeProgressBar.setVisibility(View.VISIBLE);

                        // close leave request
                        new CloseLeave().execute(Routes.closeRequest, myRequestsList.get(position).getRequestId());

                        // remove item
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                myRequestsList.remove(position);
                                notifyDataSetChanged();

                            }
                        }, Constants.delay);

                    }
                });



                /*if(i == myOldRequestsList.size()-1){

                    Log.v(Constants.appName, "Getting more " + myOldRequestsList.size() + " items");

                    // get the global info feed
                    //myOldRequestsList.addAll(new GetGlobalInfoFeed().execute(Routes.getGlobalInfo, String.valueOf(myOldRequestsList.size())).get());
                    //notifyDataSetChanged();
                    this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                        @Override
                        public void onChanged() {
                            super.onChanged();
                            Log.v(Constants.appName, "Attached yaah");
                        }
                    });


                    Log.v(Constants.appName, "Wow updating");
                }
*/

            }
            catch (Exception e){

                // do nothing
                e.printStackTrace();
            }

        }

        @Override
        public int getItemCount() {
            return myRequestsList.size();
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        // Close leave
        private class CloseLeave extends AsyncTask<String, Void, Void> {

            private String response = "";
            String error = null;
            String data = "";
            JSONObject jsonResponse;

            @Override
            protected Void doInBackground(String... urls) {

                /************ Make Post Call To Web Server ***********/
                BufferedReader reader = null;

                // Send data
                try {

                    // Set Request parameter
                    data += "?&" + URLEncoder.encode(Constants.requestId, "UTF-8") + "=" + urls[1];

                    URL url = new URL(urls[0]+ data);

                    // connection
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() != 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                showErrorMessage();
                            }
                        });
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    // read the response
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        // Append server response in string
                        sb.append(line + " ");
                    }

                    // get the server response
                    response = sb.toString();

                    // close connection
                    br.close();
                    conn.disconnect();


                } catch (Exception ex) {

                    ex.printStackTrace();
                    error = ex.getMessage();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            showErrorMessage();
                        }
                    });

                }

                return null;
            }

            protected void onPostExecute(Void unused) {

                if (error != null) {

                    showErrorMessage();

                } else {

                    /****************** Start Parse Response JSON Data *************/
                    try {

                        /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/

                        if(response.length() > 0) {
                            jsonResponse = new JSONObject(response);

                            /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                            status = jsonResponse.getInt(Constants.status);
                            msg = jsonResponse.getString(Constants.msg);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    // check the status and proceed with the logic
                                    switch (status) {

                                        // exception occurred
                                        case 200:




                                            break;

                                        // allow login
                                        default:

                                            // show error
                                            showErrorMessage();
                                            break;
                                    }
                                }
                            });

                        }

                    } catch (Exception e) {

                        showErrorMessage();
                    }

                }

                //nextButton.setEnabled(true);

            }

        }

    }

    // Old Requests
    private class GetMyRequestsList extends AsyncTask<String, Void, Void> {

        private String response = "";
        private String Error = null;
        String data = "";

        @Override
        protected Void doInBackground(String... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.collegeId, "UTF-8") + "=" + "yeswe02"
                        + "&" + URLEncoder.encode(Constants.offset, "UTF-8") + "=" + oldItemsOffSet;

                URL url = new URL(urls[0]+ data);

                // connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            requestsHistoryProgress.setVisibility(View.GONE);
                            showErrorMessage();
                        }
                    });
                }

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                // read the response
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // get the server response
                response = sb.toString();

                // close connection
                reader.close();
                conn.disconnect();


            } catch (Exception ex) {

                ex.printStackTrace();
                Error = ex.getMessage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        requestsHistoryProgress.setVisibility(View.GONE);
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
                requestsHistoryProgress.setVisibility(View.GONE);
                showErrorMessage();

            } else {

                //Log.i("Connection", response);
                /****************** Start Parse Response JSON Data *************/
                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(response);


                    /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                    status = jsonResponse.getInt(Constants.status);
                    msg = jsonResponse.getString(Constants.msg);
                    oldRequestsData = jsonResponse.getJSONArray(Constants.data);


                    // check the status and proceed with the logic
                    switch (status){

                        // exception occurred
                        case 200:

                            try {

                                if(oldRequestsData.length() > 0) {
                                    Log.v(Constants.appName, "New items : " + oldRequestsData.length());

                                    // populate the list view
                                    for (int i = 0; i < oldRequestsData.length(); i++) {

                                        Request request = new Request();
                                        request.setRequestId(oldRequestsData.getJSONObject(i).getString(Constants.requestId));
                                        request.setRequestType(oldRequestsData.getJSONObject(i).getString(Constants.requestType));
                                        request.setDescription(oldRequestsData.getJSONObject(i).getString(Constants.description));
                                        request.setRequestDate(oldRequestsData.getJSONObject(i).getString(Constants.requestDate));
                                        request.setRequestStatus(oldRequestsData.getJSONObject(i).getString(Constants.requestStatus));
                                        request.setRequestFrom(oldRequestsData.getJSONObject(i).getString(Constants.requestFrom));
                                        request.setRequestTo(oldRequestsData.getJSONObject(i).getString(Constants.requestTo));

                                        oldRequests.add(request);

                                    }

                                    // notify adapter
                                    oldRequestsAdapter.notifyDataSetChanged();
                                }
                                else {

                                    // more data exists
                                    requestsDataExists = false;
                                }

                                // hide the progress
                                requestsHistoryProgress.setVisibility(View.GONE);

                                break;
                            }
                            catch(Exception e){
                                e.printStackTrace();
                                showErrorMessage();
                            }

                            break;

                        // allow login
                        default:

                            try {

                                requestsHistoryProgress.setVisibility(View.GONE);

                                // show error
                                showErrorMessage();

                                break;
                            }
                            catch(Exception e){
                                e.printStackTrace();
                                showErrorMessage();
                            }


                    }



                } catch (Exception e) {

                    e.printStackTrace();
                    showErrorMessage();
                }

            }

            //nextButton.setEnabled(true);

        }

    }

    public class OldRequestsAdapter extends RecyclerView.Adapter<OldRequestsAdapter.OldRequestsViewHolder> {

        List<Request> myOldRequestsList;

        // constructor
        public OldRequestsAdapter(List<Request> requestsList) {

            this.myOldRequestsList = requestsList;

        }

        public class OldRequestsViewHolder extends RecyclerView.ViewHolder{

            AppCompatTextView requestType, requestDescription, requestDate, requestStatus, requestStatusMessage, closeRequest;
            ProgressBar closeProgressBar;

            public OldRequestsViewHolder(View view) {
                super(view);

                requestType = (AppCompatTextView) view.findViewById(R.id.requestType);
                requestDescription = (AppCompatTextView) view.findViewById(R.id.requestDescription);
                requestDate = (AppCompatTextView) view.findViewById(R.id.requestDate);
                requestStatus = (AppCompatTextView) view.findViewById(R.id.requestStatus);
                requestStatusMessage = (AppCompatTextView) view.findViewById(R.id.requestStatusMessage);

                closeRequest = (AppCompatTextView) view.findViewById(R.id.closeRequest);
                closeProgressBar = (ProgressBar) view.findViewById(R.id.closeProgressBar);

               // Log.v(Constants.appName, "one");
            }


        }


        @Override
        public OldRequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_request_singleitem, parent, false);
            return new OldRequestsViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull final OldRequestsViewHolder oldRequestsViewHolder, int i) {

            final int position = i;
            Log.v(Constants.appName, "Bind : "+position);

                Request request = myOldRequestsList.get(i);

                // set title and created At
                oldRequestsViewHolder.requestType.setText(request.getRequestType());
                oldRequestsViewHolder.requestDate.setText(Snippets.convertDate(request.getRequestFrom()) + " - " + Snippets.convertDate(request.getRequestTo()));
                oldRequestsViewHolder.requestDescription.setText(request.getDescription());
                oldRequestsViewHolder.requestStatus.setText(request.getRequestStatus());

                // Only show status message if comment is available
                oldRequestsViewHolder.requestStatusMessage.setVisibility(View.GONE);
                oldRequestsViewHolder.requestStatusMessage.setText("");
                oldRequestsViewHolder.closeRequest.setVisibility(View.GONE);
                oldRequestsViewHolder.closeProgressBar.setVisibility(View.GONE);

                // status display
                if(request.getRequestStatus().contentEquals("Rejected")){
                    oldRequestsViewHolder.requestStatus.setTextColor(getResources().getColor(R.color.red));

                    // check the request type
                    if(request.getRequestType().equalsIgnoreCase(Constants.leave) && request.getIsOpen() == 1){
                        oldRequestsViewHolder.closeRequest.setVisibility(View.VISIBLE);
                    }

                }else if(request.getRequestStatus().contentEquals("Approved")){
                    oldRequestsViewHolder.requestStatus.setTextColor(getResources().getColor(R.color.green));

                    // check the request type
                    if(request.getRequestType().equalsIgnoreCase(Constants.leave) && request.getIsOpen() == 1){
                        oldRequestsViewHolder.closeRequest.setVisibility(View.VISIBLE);
                    }

                }else if(request.getRequestStatus().contentEquals("Submitted")){
                    oldRequestsViewHolder.requestStatus.setTextColor(getResources().getColor(R.color.green));

                    oldRequestsViewHolder.requestStatusMessage.setVisibility(View.VISIBLE);
                    oldRequestsViewHolder.requestStatusMessage.setText("Waiting for approval! ");

                }else if(request.getRequestStatus().contentEquals("Issued")){
                    oldRequestsViewHolder.requestStatus.setTextColor(getResources().getColor(R.color.green));
                }

                // Only show status message if comment is available
                if(request.getComment()!=null){
                    oldRequestsViewHolder.requestStatusMessage.setVisibility(View.VISIBLE);
                    oldRequestsViewHolder.requestStatusMessage.append(request.getComment());
                }

                // close leave request
                oldRequestsViewHolder.closeRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // hide the close button
                        oldRequestsViewHolder.closeRequest.setVisibility(View.GONE);
                        oldRequestsViewHolder.closeProgressBar.setVisibility(View.VISIBLE);

                        // close leave request
                        new CloseLeave().execute(Routes.closeRequest, myOldRequestsList.get(position).getRequestId());

                        // remove item
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                myOldRequestsList.remove(position);
                                notifyDataSetChanged();

                            }
                        }, Constants.delay);

                    }
                });

                // check the position to load more data
                if(i == myOldRequestsList.size()-1) {
                    //loadMore
                    loadMoreRequests(i);
                }

        }

        @Override
        public int getItemCount() {
            return myOldRequestsList.size();
        }

        // Close leave
        private class CloseLeave extends AsyncTask<String, Void, Void> {

            private String response = "";
            String error = null;
            String data = "";
            JSONObject jsonResponse;

            @Override
            protected Void doInBackground(String... urls) {

                /************ Make Post Call To Web Server ***********/
                BufferedReader reader = null;

                // Send data
                try {

                    // Set Request parameter
                    data += "?&" + URLEncoder.encode(Constants.requestId, "UTF-8") + "=" + urls[1];

                    URL url = new URL(urls[0]+ data);

                    // connection
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() != 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                showErrorMessage();
                            }
                        });
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    // read the response
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        // Append server response in string
                        sb.append(line + " ");
                    }

                    // get the server response
                    response = sb.toString();

                    // close connection
                    br.close();
                    conn.disconnect();


                } catch (Exception ex) {

                    ex.printStackTrace();
                    error = ex.getMessage();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            showErrorMessage();
                        }
                    });

                }

                return null;
            }

            protected void onPostExecute(Void unused) {

                if (error != null) {

                    showErrorMessage();

                } else {

                    /****************** Start Parse Response JSON Data *************/
                    try {

                        /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/

                        if(response.length() > 0) {
                            jsonResponse = new JSONObject(response);

                            /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                            status = jsonResponse.getInt(Constants.status);
                            msg = jsonResponse.getString(Constants.msg);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    // check the status and proceed with the logic
                                    switch (status) {

                                        // exception occurred
                                        case 200:




                                            break;

                                        // allow login
                                        default:

                                            // show error
                                            showErrorMessage();
                                            break;
                                    }
                                }
                            });

                        }

                    } catch (Exception e) {

                        showErrorMessage();
                    }

                }

                //nextButton.setEnabled(true);

            }

        }

    }

    public void loadMoreRequests(int position) {

        if(position == oldRequests.size()-1){

            // check if data exists
            if(requestsDataExists) {

                // increment the offset
                oldItemsOffSet = position + 1;

                // get more requests
                requestsHistoryProgress.setVisibility(View.VISIBLE);
                new GetMyRequestsList().execute(Routes.requestHistory);
            }
            else {

                // no more data

            }
        }

    }

    public void showErrorMessage(){

        // show error message
        snackbar = Snackbar.make(requestsLayout, "Something went wrong. Please try again!", Snackbar.LENGTH_LONG);
        designSnackBar();
        snackbar.show();

    }

    public void designSnackBar() {

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.palette_red));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setTextSize(16);
    }
}
