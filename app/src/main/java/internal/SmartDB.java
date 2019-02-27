package internal;

import android.content.Context;
import android.content.SharedPreferences;

import utils.Constants;

public class SmartDB {

    // shared preference instance
    public SharedPreferences sharedPreferences;

    // shared preference editor
    SharedPreferences.Editor editor;

    // context
    Context context;

    // shared preference name
    private static final String Shared_Preference_name = Constants.app_SharedPreferences;

    // Global count of wall posts
    int global_Count = 0;

    public SmartDB(Context context){

        this.context = context;

        sharedPreferences = context.getSharedPreferences(Shared_Preference_name, 0);
        editor = sharedPreferences.edit();
    }

    // save notification
    public void saveNotificationNumber(int number){

        editor.putInt(Constants.notificationNumber, number);
        editor.commit();
    }
    public int getNotificationNumber(){

        return sharedPreferences.getInt(Constants.notificationNumber, 0);
    }
    public void clearNotificationNumber(){

        if(sharedPreferences.contains(Constants.notificationNumber)) {
            editor.putInt(Constants.notificationNumber, 0);
            editor.commit();
        }
    }

    // save loggedIn
    public void setLoggedIn(boolean i){

        editor.putBoolean(Constants.loginUsername, i);
        editor.commit();
    }

    // get the saved login status
    public Boolean getLoginUsername(){

        return sharedPreferences.getBoolean(Constants.loginUsername, false);
    }

    // save profile photo
    public void alreadyDidRating(boolean i){

        editor.putBoolean(Constants.rating, i);
        editor.commit();
    }

    // get the saved profile photo
    public Boolean getRating(){

        return sharedPreferences.getBoolean(Constants.rating, false);
    }



    // save profile photo
    public void saveProfilePhoto(String base64String){

        editor.putString(Constants.userImage, base64String);
        editor.commit();
    }

    // remove profile photo
    public void removeProfilePhoto(){

        editor.putString(Constants.userImage, Constants.null_indicator);
        editor.commit();
    }

    // get the saved profile photo
    public String getProfilePhoto(){

        return sharedPreferences.getString(Constants.userImage, Constants.null_indicator);
    }


    // save departments and sub_departments
    public void saveDepartments(String departments, String sub_departments){

        editor.putString(Constants.departments, departments);
        editor.putString(Constants.sub_departments, sub_departments);
        editor.commit();
    }

    // remove departments and sub_departments
    public void removeDepartments(){

        editor.putString(Constants.departments, Constants.null_indicator);
        editor.putString(Constants.sub_departments, Constants.null_indicator);
        editor.commit();
    }

    // get the saved departments
    public String getDepartments(){

        return sharedPreferences.getString(Constants.departments, Constants.null_indicator);
    }
    // get the saved sub_departments
    public String getSubDepartments(){

        return sharedPreferences.getString(Constants.sub_departments, Constants.null_indicator);
    }

    // this
    public boolean isSubscribed(){
        return sharedPreferences.getBoolean(Constants.isSubscribed, false);
    }

    // get isLocal to the remote as we updated it to server
    // this
    public void setSubscribed(){
        editor.putBoolean(Constants.isSubscribed, true);
        editor.commit();
    }

}
