package internal;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import utils.Constants;

public class SmartSessionManager {

    public SharedPreferences sharedPreferences;

    // shared preference editor
    SharedPreferences.Editor editor;

    // context
    Context context;

    // shared preference name
    private static final String Shared_Preference_name = Constants.app_SharedPreferences;

    public SmartSessionManager(Context context){

        this.context = context;

        sharedPreferences = context.getSharedPreferences(Shared_Preference_name, 0);
        editor = sharedPreferences.edit();

        // by default requests count is 0
        editor.putInt(Constants.requests, 0);
        editor.apply();
    }


    ////////////////////////////// REQUESTS ////////////////////////////////
    // get open request count
    public int getRequestCount(){
        return sharedPreferences.getInt(Constants.requests, 0);
    }

    // increment request count when user creates a request
    public void updateOpenRequestsCount(){

        if(sharedPreferences.contains(Constants.requests)){
            editor.putInt(Constants.requests, sharedPreferences.getInt(Constants.requests, 0)+1);
        }
        else {
            editor.putInt(Constants.requests, 1);
        }
    }

    // delete the open request count
    public void deleteOpenRequestCount(){

        if(sharedPreferences.getInt(Constants.requests, 0) > 0){

            editor.putInt(Constants.requests, sharedPreferences.getInt(Constants.requests,0)-1);
        }
    }












    // save requests
    public void saveRequests(String requestId){

        Set<String> updatedRequests = new HashSet<String>();

        //  check if requests exist
        if(sharedPreferences.contains(Constants.requests)) {

            updatedRequests = sharedPreferences.getStringSet(Constants.requests, null);

            Set<String> requests = new HashSet<String>();
            requests = updatedRequests;
            requests.add(requestId);

            editor.putStringSet(Constants.requests, requests);
            editor.apply();
        }
        else {

            updatedRequests.add(requestId);
            editor.putStringSet(Constants.requests, updatedRequests);
            editor.apply();
        }
    }


}
