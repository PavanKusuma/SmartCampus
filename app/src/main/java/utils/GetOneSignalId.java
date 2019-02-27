package utils;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.onesignal.OneSignal;

// register device to send push notifications

public class GetOneSignalId extends AsyncTask<Void, Void, String> {

    String token = Constants.null_indicator;

    @Override
    protected void onPreExecute() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    protected String doInBackground(Void... params) {

        try {

            OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                @Override
                public void idsAvailable(String userId, String registrationId) {

                    Log.v(Constants.appName, "UserId : " + userId + " RegistartionId : " + registrationId);
                    token = userId;
                }
            });


            // Send the token to your backend server via an HTTP GET requests
            //new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();

        }
        catch (Exception exc) {
            // Return exc to onPostExecute
            return exc.getMessage();
        }

        // Success
        return token;
    }
}
