package in.devco.tinsta.actvity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import in.devco.tinsta.R;
import in.devco.tinsta.lib.CheckNetwork;
import in.devco.tinsta.lib.ServerConnection;
import in.devco.tinsta.lib.User;

//TODO: Add loading to login check
//TODO: Add Google and Facebook login
//TODO: Add intent to sign up

public class LoginActivity extends AppCompatActivity{

    String userName, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (User.isLoggedIn(this)) {
            Object[] temp = User.sessionDetails(this);

            new LoginCheckTask((Integer) temp[0], (String) temp[1], this).execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_login));
        }


        Button btSignIn = findViewById(R.id.login_bt_login);
        final EditText etUserName = findViewById(R.id.login_et_username);
        final EditText etPassword = findViewById(R.id.login_et_password);

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = etUserName.getText().toString();
                password = etPassword.getText().toString();

                if(CheckNetwork.isInternetAvailable(getApplicationContext())) {
                    if (userName.equals(""))
                        etUserName.setError(getResources().getString(R.string.empty_username));
                    else if (password.equals(""))
                        etPassword.setError(getResources().getString(R.string.empty_password));
                    else
                        new LogInTask(userName, password,LoginActivity.this).execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_login));
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Login Async Task class
    private static class LogInTask extends AsyncTask<String, String, String> {
        private String userName;
        private String password;

        private WeakReference<LoginActivity> activityReference;

        private ProgressDialog progress;

        LogInTask(String userName, String password, LoginActivity context) {
            this.userName = userName;
            this.password = password;

            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            progress = new ProgressDialog(activity);
            progress.setTitle(activity.getResources().getString(R.string.loading));
            progress.setMessage(activity.getResources().getString(R.string.loading_des));
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                HashMap<String, String> hm = new HashMap<>();
                hm.put("action", "loginUser");
                hm.put("username", userName);
                hm.put("password", password);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(ServerConnection.getPostDataString(hm));

                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();

                InputStream stream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                return buffer.toString();
            } catch (Exception e) {
                progress.dismiss();
                Log.e("Url", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            try {
                JSONObject result = new JSONObject(s);

                Integer status      = result.getInt("status");
                Integer userId      = result.getInt("user_id");
                String sessionId    = result.getString("session_id");
                Boolean success     = result.getBoolean("success");

                if (status != 200) {
                    progress.dismiss();
                    Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                } else {
                    if (!success) {
                        progress.dismiss();
                        Toast.makeText(activity, activity.getResources().getString(R.string.wrong_username_password), Toast.LENGTH_LONG).show();
                    } else {
                        User.logIn(activity, userId, sessionId);
                        progress.dismiss();
                        activity.finish();
                    }
                }
            }
            catch (Exception e) {
                progress.dismiss();
                Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                Log.e("Url", e.toString());
            }
        }
    }

    //Login Check Async Task class
    private static class LoginCheckTask extends AsyncTask<String, String, String> {
        private Integer userId;
        private String sessionId;

        private WeakReference<LoginActivity> activityReference;

        LoginCheckTask(Integer userId, String sessionId, LoginActivity context) {
            this.userId = userId;
            this.sessionId = sessionId;

            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                HashMap<String, String> hm = new HashMap<>();
                hm.put("action", "isLoggedIn");
                hm.put("userId", String.valueOf(userId));
                hm.put("sessionId", sessionId);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(ServerConnection.getPostDataString(hm));

                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();

                InputStream stream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                return buffer.toString();
            } catch (Exception e) {
                Log.e("Url", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            try {
                JSONObject result = new JSONObject(s);

                Integer status = result.getInt("status");
                Boolean success = result.getBoolean("success");

                if (status != 200) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                } else if (success)
                    activity.startActivity(new Intent(activity, MainActivity.class));
            }
            catch (Exception e) {
                Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                Log.e("Url", e.toString());
            }
        }
    }
}
