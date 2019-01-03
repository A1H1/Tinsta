package in.devco.tinsta.actvity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import in.devco.tinsta.R;
import in.devco.tinsta.lib.CheckNetwork;
import in.devco.tinsta.lib.ServerConnection;
import in.devco.tinsta.lib.User;

//TODO: Add loading to login check
//TODO: Add Google signup
//TODO: Add Facebook signup

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String EMAIL = "email";
    private static final String TAG = "LoginActivity";
    private static final Integer RC_SIGN_IN = 301;

    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Check for Login
        isLoggedIn();

        //Facebook Setup
        LoginButton facebookButton = findViewById(R.id.login_facebook_button);
        facebookButton.setReadPermissions(Collections.singletonList(EMAIL));
        callbackManager = CallbackManager.Factory.create();

        //Google Setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //OnClick Listeners
        findViewById(R.id.login_rl_google).setOnClickListener(this);
        findViewById(R.id.login_rl_facebook).setOnClickListener(this);
        findViewById(R.id.login_signup).setOnClickListener(this);
        findViewById(R.id.login_bt_login).setOnClickListener(this);

        //Facebook Callback registration
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String email = object.getString("email");
                                    new LogInTask(LoginActivity.this, email, "facebook").execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_login));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            googleLogin(task);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.login_rl_google:
                startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                break;
            case R.id.login_rl_facebook:
                findViewById(R.id.login_facebook_button).performClick();
                break;
            case R.id.login_signup:
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
                break;
            case R.id.login_bt_login:
                login();
                break;
        }
    }

    private void isLoggedIn() {
        if (User.isLoggedIn(this)) {
            Object[] temp = User.sessionDetails(this);

            new LoginCheckTask((Integer) temp[0], (String) temp[1], this).execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_login));
        }
    }

    private void login() {
        EditText etUserName = findViewById(R.id.login_et_username);
        EditText etPassword = findViewById(R.id.login_et_password);

        String userName = etUserName.getText().toString();
        String password = etPassword.getText().toString();

        if(CheckNetwork.isInternetAvailable(getApplicationContext())) {
            if (userName.equals(""))
                etUserName.setError(getResources().getString(R.string.empty_username));
            else if (password.equals(""))
                etPassword.setError(getResources().getString(R.string.empty_password));
            else
                new LogInTask(userName, password,LoginActivity.this).execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_login));
        } else
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
    }

    private void googleLogin(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = Objects.requireNonNull(account).getEmail();

            new LogInTask(LoginActivity.this, email, "google").execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_login));
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    //Login Async Task class
    private static class LogInTask extends AsyncTask<String, String, String> {
        private String userName;
        private String password;
        private String src = "app";

        private WeakReference<LoginActivity> activityReference;

        private ProgressDialog progress;

        LogInTask(String userName, String password, LoginActivity context) {
            this.userName = userName;
            this.password = password;

            activityReference = new WeakReference<>(context);
        }

        LogInTask(LoginActivity context, String email, String src) {
            userName = email;
            this.src = src;

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
                if (src.equals("facebook"))
                    hm.put("src", src);
                else
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
                String src          = result.getString("src");
                Boolean success     = result.getBoolean("success");

                if (status != 200) {
                    progress.dismiss();
                    Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                } else {
                    if (!success) {
                        progress.dismiss();
                        Toast.makeText(activity, activity.getResources().getString(R.string.wrong_username_password), Toast.LENGTH_LONG).show();
                    } else {
                        User.logIn(activity, userId, sessionId, src);
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
                } else if (success) {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                }
            }
            catch (Exception e) {
                Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                Log.e("Url", e.toString());
            }
        }
    }
}
