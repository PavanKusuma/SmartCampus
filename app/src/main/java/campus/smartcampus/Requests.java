package campus.smartcampus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
    AppCompatTextView leave, outing, grievance;
    RecyclerView requestsListView;
    CoordinatorLayout itemView, requestsLayout;
    Snackbar snackbar;

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
        grievance = (AppCompatTextView) itemView.findViewById(R.id.grievance);
        activeRequestLayout = (RelativeLayout) itemView.findViewById(R.id.activeRequestLayout);
        requestsListView = (RecyclerView) itemView.findViewById(R.id.requestsListView);
        requestsLayout = (CoordinatorLayout) itemView.findViewById(R.id.requestsLayout);

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

        grievance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent grievanceIntent = new Intent(getActivity(), GrievanceRequest.class);
                startActivity(grievanceIntent);
            }
        });

        // Check if any active requests from current session
        // if any, retrieve the details
        // after fetching details, if request status is CLOSED, then delete request from current session
        smartSessionManager = new SmartSessionManager(getActivity());
        if(smartSessionManager.getRequestCount() > 0){

            // get the pending requests with the userObjectId
            Log.v(Constants.appName, "Requests here");

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            requestsListView.setLayoutManager(mLayoutManager);

            new GetNewRequestsList().execute(Routes.myRequests);

        }

        return itemView;

    }

    @Override
    public void onResumeFragment() {

    }

    // Apply leave
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
Log.v(Constants.appName, response);
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
                    requestsData = jsonResponse.getJSONArray(Constants.data);


                    Log.v(Constants.appName, "OK"+requestsData.length());
                            // check the status and proceed with the logic
                            switch (status){

                                // exception occurred
                                case 200:

                                    try {

                                        Log.v(Constants.appName, "OK"+requestsData.length());
                                        // populate the list view
                                        for (int i=0; i<requestsData.length(); i++) {

                                            Request request = new Request();
                                             request.setRequestId(requestsData.getJSONObject(i).getString(Constants.requestId));
                                             request.setRequestType(requestsData.getJSONObject(i).getString(Constants.requestType));
                                             request.setDescription(requestsData.getJSONObject(i).getString(Constants.description));
                                             request.setRequestDate(requestsData.getJSONObject(i).getString(Constants.requestDate));

                                            requests.add(request);
                                            Log.v(Constants.appName, request.getRequestId());
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

            // date format for displaying created date
            DateFormat simpleDateFormat;
            AppCompatTextView requestType;
            AppCompatTextView requestDescription;
            AppCompatTextView requestDate;
            AppCompatTextView requestStatusPending;
            AppCompatTextView requestStatusApproved;
            AppCompatTextView requestStatusApprovedWaiting;
            AppCompatTextView requestStatusIssued;
            AppCompatTextView closeRequest;

            RequestsViewHolder(View itemView) {
                super(itemView);

                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                requestType = (AppCompatTextView) itemView.findViewById(R.id.requestType);
                requestDescription = (AppCompatTextView) itemView.findViewById(R.id.requestDescription);
                requestDate = (AppCompatTextView) itemView.findViewById(R.id.requestDate);
                requestStatusPending = (AppCompatTextView) itemView.findViewById(R.id.requestStatusPending);
                requestStatusApproved = (AppCompatTextView) itemView.findViewById(R.id.requestStatusApproved);
                requestStatusApprovedWaiting = (AppCompatTextView) itemView.findViewById(R.id.requestStatusApprovedWaiting);
                requestStatusIssued = (AppCompatTextView) itemView.findViewById(R.id.requestStatusIssued);
                closeRequest = (AppCompatTextView) itemView.findViewById(R.id.closeRequest);

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
        public void onBindViewHolder(RequestsViewHolder holder, int i) {

            try {


                // get the date format and convert it into required format to display
                //java.util.Date date = holder.simpleDateFormat.parse(myRequestsList.get(i).getRequestDate());
                //simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm aa");
                //holder.simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");


               // Log.v(Constants.appName, Snippets.convertDate(myRequestsList.get(i).getRequestDate()));
String date = Snippets.convertDate(myRequestsList.get(i).getRequestDate());
                Log.v(Constants.appName, date);
                // set title and created At
                holder.requestType.setText(myRequestsList.get(i).getRequestType());
                holder.requestDate.setText(date);
                holder.requestDescription.setText(myRequestsList.get(i).getDescription());


                holder.closeRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                final int position = i;

                Log.v(Constants.appName, "Inserted here" + i);

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



                // navigate to link page only if link is available
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                    }
                });


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



        private class UpdateHitGlobalInfo extends AsyncTask<String, Void, Void> {

            private String Content = "";
            private String Error = null;
            String data = "";

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Void doInBackground(String... urls) {

                /************ Make Post Call To Web Server ***********/
                BufferedReader reader = null;

                // Send data
                try {

                    // Set Request parameter
                    data += "?&" + URLEncoder.encode(Constants.KEY, "UTF-8") + "=" + Constants.key
                            + "&" + URLEncoder.encode(Constants.userObjectId, "UTF-8") + "=" + (urls[2]);


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

                    // Append Server Response To response String
                    Content = sb.toString();

                    // close the reader
                    //reader.close();

                } catch (Exception ex) {

                    ex.printStackTrace();
                    Error = ex.getMessage();


                } finally {

                    try {

                        reader.close();

                    } catch (Exception ex) {
                        Error = ex.getMessage();
                    }
                }

                return null;
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
