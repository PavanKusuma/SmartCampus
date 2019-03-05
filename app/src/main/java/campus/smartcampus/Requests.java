package campus.smartcampus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
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
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import internal.SmartSessionManager;
import modal.Request;
import utils.Constants;
import utils.Routes;
import utils.Snippets;

public class Requests extends Fragment implements Home.FragmentLifeCycle {

    RelativeLayout activeRequestLayout;
    AppCompatTextView leave, outing, oldRequestsHeading, getOldRequests;
    RecyclerView requestsListView, oldRequestsListview;
    CoordinatorLayout itemView, requestsLayout;
    Snackbar snackbar;
    ProgressBar oldProgressBar;

    // session
    SmartSessionManager smartSessionManager;

    // offset
    int offset = 0;

    // request status
    int status = -3;
    String msg = "";
    JSONArray requestsData;
    JSONObject jsonResponse;
    ArrayList<Request> requests = new ArrayList<Request>();
    ArrayList<Request> oldRequests = new ArrayList<Request>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        itemView = (CoordinatorLayout) inflater.inflate(R.layout.requests, container, false);

        leave = (AppCompatTextView) itemView.findViewById(R.id.leave);
        outing = (AppCompatTextView) itemView.findViewById(R.id.outing);
        oldRequestsHeading = (AppCompatTextView) itemView.findViewById(R.id.oldRequestsHeading);
        getOldRequests = (AppCompatTextView) itemView.findViewById(R.id.getOldRequests);
        activeRequestLayout = (RelativeLayout) itemView.findViewById(R.id.activeRequestLayout);
        requestsListView = (RecyclerView) itemView.findViewById(R.id.requestsListView);
        oldRequestsListview = (RecyclerView) itemView.findViewById(R.id.oldRequestsListView);
        requestsLayout = (CoordinatorLayout) itemView.findViewById(R.id.requestsLayout);
        oldProgressBar = (ProgressBar) itemView.findViewById(R.id.oldProgressBar);

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent leaveIntent = new Intent(getActivity(), LeaveRequest.class);
                startActivity(leaveIntent);
            }
        });

        outing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent outingIntent = new Intent(getActivity(), OutingRequest.class);
                startActivity(outingIntent);
            }
        });

        getOldRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // hide button
                getOldRequests.setVisibility(View.GONE);
                oldRequestsHeading.setVisibility(View.GONE);
                oldProgressBar.setVisibility(View.VISIBLE);
                oldRequestsListview.setVisibility(View.VISIBLE);

                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                oldRequestsListview.setLayoutManager(mLayoutManager);

                new GetMyRequestsList().execute(Routes.requestHistory);
            }
        });

        // Check if any active requests from current session
        // if any, retrieve the details
        // after fetching details, if request status is CLOSED, then delete request from current session
        smartSessionManager = new SmartSessionManager(getActivity());
        if(smartSessionManager.getRequestCount() > 0){

            // get the pending requests with the userObjectId
            Log.v(Constants.appName, "Requests here");

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            requestsListView.setLayoutManager(mLayoutManager);

            new GetNewRequestsList().execute(Routes.myRequests);

        }

        return itemView;

    }

    @Override
    public void onResumeFragment() {

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
                    getActivity().runOnUiThread(new Runnable() {
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

                getActivity().runOnUiThread(new Runnable() {
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

                                        RequestsAdapter requestsAdapter = new RequestsAdapter(getActivity().getApplicationContext(), requests);
                                        requestsListView.setAdapter(requestsAdapter);
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

    public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder> {

        // context of present class
        Context context;
        ArrayList<Request> myRequestsList;
        LayoutInflater layoutInflater;

        // constructor
        public RequestsAdapter(Context context, ArrayList<Request> requestsList) {

            this.context = context;
            myRequestsList = requestsList;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        class RequestsViewHolder extends RecyclerView.ViewHolder{

            AppCompatTextView requestType, requestDescription, requestDate, requestStatus, requestStatusMessage, closeRequest;
            ProgressBar closeProgressBar;

            RequestsViewHolder(View itemView) {
                super(itemView);

                requestType = (AppCompatTextView) itemView.findViewById(R.id.requestType);
                requestDescription = (AppCompatTextView) itemView.findViewById(R.id.requestDescription);
                requestDate = (AppCompatTextView) itemView.findViewById(R.id.requestDate);
                requestStatus = (AppCompatTextView) itemView.findViewById(R.id.requestStatus);
                requestStatusMessage = (AppCompatTextView) itemView.findViewById(R.id.requestStatusMessage);

                closeRequest = (AppCompatTextView) itemView.findViewById(R.id.closeRequest);
                closeProgressBar = (ProgressBar) itemView.findViewById(R.id.closeProgressBar);

                //hereText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.link_white_24dp,0,0,0);


            }


        }

        @Override
        public RequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView;

            itemView = (RelativeLayout) layoutInflater.inflate(R.layout.my_request_singleitem, parent, false);

            return new RequestsViewHolder(itemView);
        }


        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);

            Log.v(Constants.appName, "Attached");
        }


        @Override
        public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);

            Log.v(Constants.appName, "Planted bomb here");
            //this.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void onBindViewHolder(final RequestsViewHolder holder, int i) {

            final int position = i;
            try {

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



                if(i == myRequestsList.size()-1){

                    Log.v(Constants.appName, "Getting more " + myRequestsList.size() + " items");

                    // get the global info feed
                    //myRequestsList.addAll(new GetGlobalInfoFeed().execute(Routes.getGlobalInfo, String.valueOf(myRequestsList.size())).get());
                    notifyDataSetChanged();
                    this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                        @Override
                        public void onChanged() {
                            super.onChanged();
                            Log.v(Constants.appName, "Attached yaah");
                        }
                    });


                    Log.v(Constants.appName, "Wow updating");
                }


            }
            catch (Exception e){

                // do nothing
            }

        }

        @Override
        public int getItemCount() {
            return myRequestsList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
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
                        getActivity().runOnUiThread(new Runnable() {
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

                    getActivity().runOnUiThread(new Runnable() {
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

                            getActivity().runOnUiThread(new Runnable() {
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
                        + "&" + URLEncoder.encode(Constants.offset, "UTF-8") + "=" + offset;


                URL url = new URL(urls[0]+ data);

                // connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            oldProgressBar.setVisibility(View.GONE);
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

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        oldProgressBar.setVisibility(View.GONE);
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
                oldProgressBar.setVisibility(View.GONE);
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

                                    oldRequests.add(request);

                                }

                                oldProgressBar.setVisibility(View.GONE);

                                RequestsAdapter requestsAdapter = new RequestsAdapter(getActivity().getApplicationContext(), requests);
                                oldRequestsListview.setAdapter(requestsAdapter);
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

                                oldProgressBar.setVisibility(View.GONE);

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
