package campus.smartcampus;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import modal.Circular;
import utils.Constants;
import utils.Routes;
import utils.Snippets;

public class Home extends Fragment implements GlobalHome.FragmentLifeCycle {

    CoordinatorLayout itemView, homeLayout;
    RecyclerView circularsListView, allCircularsListView;
    ProgressBar allCircularsProgress;
    AppCompatTextView seeAllCirculars;
    AppCompatImageView closeCirculars;
    Snackbar snackbar;
    AppCompatImageView requestsImageView;
    // bottom sheet for loading more circulars
    BottomSheetBehavior bottomSheetBehavior;
    CoordinatorLayout circularBottomSheetLayout;
    LinearLayout requestsLink;

    // list of circulars
    ArrayList<Circular> circulars = new ArrayList<Circular>();
    ArrayList<Circular> allCirculars = new ArrayList<Circular>();
    CircularAdapter circularAdapter;
    AllCircularsAdapter allCircularAdapter;

    public static int circularOffset = 0, updateOffset = 0;
    JSONObject jsonResponse;
    JSONArray circularData, oldCircularData;
    int status, skipCounter = 0;
    String msg = "";
    boolean circularDataExists = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        itemView = (CoordinatorLayout) inflater.inflate(R.layout.activity_home, container, false);

        circularsListView = (RecyclerView) itemView.findViewById(R.id.circularsListView);
        seeAllCirculars = (AppCompatTextView) itemView.findViewById(R.id.allCirculars);
        homeLayout = (CoordinatorLayout) itemView.findViewById(R.id.homeLayout);
        requestsImageView = (AppCompatImageView) itemView.findViewById(R.id.requestsImageView);

        // get the bottom sheet
        circularBottomSheetLayout = (CoordinatorLayout) itemView.findViewById(R.id.circularBottomSheetLayout);
        bottomSheetBehavior = BottomSheetBehavior.from(circularBottomSheetLayout);
        // hide initially
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        allCircularsListView = (RecyclerView) itemView.findViewById(R.id.allCircularsListView);
        allCircularsProgress = (ProgressBar) itemView.findViewById(R.id.allCircularsProgress);
        closeCirculars = (AppCompatImageView) itemView.findViewById(R.id.closeCirculars);
        requestsLink = (LinearLayout) itemView.findViewById(R.id.requestsLink);

        GradientDrawable gD = new GradientDrawable();
        gD.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);gD.setColors(new int[]{getResources().getColor(R.color.appPrimary), getResources().getColor(R.color.appPrimaryDark)});
        gD.setShape(GradientDrawable.OVAL);
        requestsImageView.setBackgroundDrawable(gD);

        circularAdapter = new CircularAdapter(circulars, R.layout.circular_single_item);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        circularsListView.setLayoutManager(layoutManager);
        circularsListView.setItemAnimator(new DefaultItemAnimator());
        circularsListView.setAdapter(circularAdapter);

        // get circulars
        new GetCirculars().execute(Routes.getCirculars, circularAdapter);

        // open all circulars view
        seeAllCirculars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // initially show already loaded circulars
                allCirculars.clear();
                allCirculars.addAll(circulars);

                // more data exists
                circularDataExists = true;

                // put offset to 0
                circularOffset = 0;

                // show progress
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                allCircularsProgress.setVisibility(View.VISIBLE);


            }
        });

        // close all circulars view
        closeCirculars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                allCircularsProgress.setVisibility(View.GONE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {

                // show if bottomSheet is up
                if(i == BottomSheetBehavior.STATE_EXPANDED || i == BottomSheetBehavior.STATE_COLLAPSED){

                    allCircularAdapter = new AllCircularsAdapter(allCirculars, R.layout.all_circulars_single_item);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                    allCircularsListView.setLayoutManager(layoutManager);
                    allCircularsListView.setItemAnimator(new DefaultItemAnimator());
                    allCircularsListView.setAdapter(allCircularAdapter);
                    allCircularAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        // open requests
        requestsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent requestsIntent = new Intent(getActivity(), Requests.class);
                startActivity(requestsIntent);
            }
        });

        return itemView;
    }

    @Override
    public void onResumeFragment() {

    }

    // Get Circulars
    private class GetCirculars extends AsyncTask<Object, Void, Void> {

        private String response = "";
        private String Error = null;
        String data = "";

        @Override
        protected Void doInBackground(Object... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.collegeId, "UTF-8") + "=" + "yeswe02"
                        + "&" + URLEncoder.encode(Constants.offset, "UTF-8") + "=" + 0;

                URL url = new URL((String)urls[0]+ data);
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
                    msg = jsonResponse.getString(Constants.msg);
                    circularData = jsonResponse.getJSONArray(Constants.data);

                    // check the status and proceed with the logic
                    switch (status){

                        // exception occurred
                        case 200:

                            try {

                                // clear previous requests
                                //circulars.clear();

                                // populate the list view
                                for (int i=0; i<circularData.length(); i++) {

                                    Circular circular = new Circular();
                                    circular.setCircularId(circularData.getJSONObject(i).getString(Constants.circularId));
                                    circular.setUserObjectId(circularData.getJSONObject(i).getString(Constants.userObjectId));
                                    circular.setSubject(circularData.getJSONObject(i).getString(Constants.subject));
                                    circular.setDescription(circularData.getJSONObject(i).getString(Constants.description));
                                    circular.setIsActive(circularData.getJSONObject(i).getInt(Constants.isActive));
                                    circular.setLinkUrl(circularData.getJSONObject(i).getString(Constants.linkUrl));
                                    circular.setLinkTitle(circularData.getJSONObject(i).getString(Constants.linkTitle));
                                    circular.setModifiedDate(circularData.getJSONObject(i).getString(Constants.modifiedDate));
                                    circular.setMediaCount(circularData.getJSONObject(i).getInt(Constants.mediaCount));
                                    circular.setMedia(circularData.getJSONObject(i).getString(Constants.media));

                                    circulars.add(circular);
                                }

                                //circularsListView.removeAllViews();
                                circularAdapter.notifyDataSetChanged();
                                //RequestsAdapter requestsAdapter = new RequestsAdapter(getActivity().getApplicationContext(), requests);
                                //requestsListView.setAdapter(requestsAdapter);
                                //requestsAdapter.notifyDataSetChanged();


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

    // Get All Circulars
    private class GetAllCirculars extends AsyncTask<Object, Void, Void> {

        private String response = "";
        private String Error = null;
        String data = "";

        @Override
        protected Void doInBackground(Object... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                // Set Request parameter
                data += "?&" + URLEncoder.encode(Constants.collegeId, "UTF-8") + "=" + "yeswe02"
                        + "&" + URLEncoder.encode(Constants.offset, "UTF-8") + "=" + circularOffset;

                URL url = new URL((String)urls[0]+ data);

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
                    msg = jsonResponse.getString(Constants.msg);
                    oldCircularData = jsonResponse.getJSONArray(Constants.data);

                    // check the status and proceed with the logic
                    switch (status){

                        // exception occurred
                        case 200:

                            try {

                                // clear previous requests
                                //circulars.clear();

                                if(oldCircularData.length() > 0) {

                                    // populate the list view
                                    for (int i = 0; i < oldCircularData.length(); i++) {

                                        Circular circular = new Circular();
                                        circular.setCircularId(oldCircularData.getJSONObject(i).getString(Constants.circularId));
                                        circular.setUserObjectId(oldCircularData.getJSONObject(i).getString(Constants.userObjectId));
                                        circular.setSubject(oldCircularData.getJSONObject(i).getString(Constants.subject));
                                        circular.setDescription(oldCircularData.getJSONObject(i).getString(Constants.description));
                                        circular.setIsActive(oldCircularData.getJSONObject(i).getInt(Constants.isActive));
                                        circular.setLinkUrl(oldCircularData.getJSONObject(i).getString(Constants.linkUrl));
                                        circular.setLinkTitle(oldCircularData.getJSONObject(i).getString(Constants.linkTitle));
                                        circular.setModifiedDate(oldCircularData.getJSONObject(i).getString(Constants.modifiedDate));
                                        circular.setMediaCount(oldCircularData.getJSONObject(i).getInt(Constants.mediaCount));
                                        circular.setMedia(oldCircularData.getJSONObject(i).getString(Constants.media));

                                        allCirculars.add(circular);
                                    }

                                    allCircularAdapter.notifyDataSetChanged();

                                }
                                else {

                                    // stop the lazy loading
                                    circularDataExists = false;
                                }

                                //circularsListView.removeAllViews();

                                allCircularsProgress.setVisibility(View.GONE);
                                //RequestsAdapter requestsAdapter = new RequestsAdapter(getActivity().getApplicationContext(), requests);
                                //requestsListView.setAdapter(requestsAdapter);
                                //requestsAdapter.notifyDataSetChanged();


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

    // circulars adapter
    public class CircularAdapter extends RecyclerView.Adapter<CircularAdapter.MyViewHolder> {

        private List<Circular> circularList;
        private int resource;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public AppCompatTextView circularHeadLine, circularText, durationText;

            public MyViewHolder(View view){
                super(view);
                circularHeadLine = (AppCompatTextView) view.findViewById(R.id.circularHeadLine);
                circularText = (AppCompatTextView) view.findViewById(R.id.circularText);
                durationText = (AppCompatTextView) view.findViewById(R.id.durationText);

            }
        }

        public CircularAdapter(List<Circular> circularList, int resource){

            this.circularList = circularList;
            this.resource = resource;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {

            Circular circular = circularList.get(position);
            myViewHolder.circularHeadLine.setText(circular.getSubject());
            myViewHolder.circularText.setText(circular.getDescription());
            myViewHolder.durationText.setText(Snippets.getDuration(circular.getModifiedDate()));

        }

        @Override
        public int getItemCount() {
            return circularList.size();
        }

    }

    // all circulars adapter
    public class AllCircularsAdapter extends RecyclerView.Adapter<AllCircularsAdapter.MyAllViewHolder> {

        private List<Circular> circularList;
        private int resource;

        public class MyAllViewHolder extends RecyclerView.ViewHolder {

            public AppCompatTextView circularHeadLine, circularText, durationText;

            public MyAllViewHolder(View view){
                super(view);
                circularHeadLine = (AppCompatTextView) view.findViewById(R.id.circularHeadLine);
                circularText = (AppCompatTextView) view.findViewById(R.id.circularText);
                durationText = (AppCompatTextView) view.findViewById(R.id.durationText);

            }
        }

        public AllCircularsAdapter(List<Circular> circularList, int resource){

            this.circularList = circularList;
            this.resource = resource;
        }

        @Override
        public MyAllViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            return new MyAllViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAllViewHolder myViewHolder, int position) {

            Circular circular = circularList.get(position);
            myViewHolder.circularHeadLine.setText(circular.getSubject());
            myViewHolder.circularText.setText(circular.getDescription());
            myViewHolder.durationText.setText(Snippets.getDuration(circular.getModifiedDate()));

            // check the position to load more data
            if(position == circularList.size()-1) {
                //loadMore
                loadMoreCirculars(position);
            }

        }

        @Override
        public int getItemCount() {
            return circularList.size();
        }

    }


    public void showErrorMessage(){

        // show error message
        snackbar = Snackbar.make(homeLayout, "Something went wrong. Please try again!", Snackbar.LENGTH_LONG);
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


    public void loadMoreCirculars(int position) {

        if(position == allCirculars.size()-1){

            // check if data exists
            if(circularDataExists) {

                // increment the offset
                circularOffset = position + 1;

                // get more circulars
                new GetAllCirculars().execute(Routes.getCirculars, allCircularAdapter);
            }
            else {

                // no more data

            }
        }

    }
}