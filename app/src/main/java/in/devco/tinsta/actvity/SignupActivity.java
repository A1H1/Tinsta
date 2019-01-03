package in.devco.tinsta.actvity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import in.devco.tinsta.lib.Util;

//TODO: Override back button

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        findViewById(R.id.signup_bt_login).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.signup_bt_login:
                signup();
                break;
        }
    }

    private void signup() {
        EditText etUserName = findViewById(R.id.signup_et_username);
        EditText etEmail    = findViewById(R.id.signup_et_email);
        EditText etPassword = findViewById(R.id.signup_et_password);
        EditText etReference= findViewById(R.id.signup_et_reference);
        RadioGroup rgGender = findViewById(R.id.signup_gender);

        String userName     = etUserName.getText().toString();
        String email        = etEmail.getText().toString();
        String password     = etPassword.getText().toString();
        String reference    = etReference.getText().toString();
        RadioButton gender  = findViewById(rgGender.getCheckedRadioButtonId());

        if(CheckNetwork.isInternetAvailable(getApplicationContext())) {
            if(userName.equals("") || email.equals("") || password.equals("") || gender == null)
                Toast.makeText(SignupActivity.this, getResources().getString(R.string.required), Toast.LENGTH_LONG).show();
            else {
                if(!Util.isValidEmail(email))
                    etEmail.setError(getResources().getString(R.string.valid_email));
                else if(!Util.isValidUsername(userName))
                    etUserName.setError(getResources().getString(R.string.valid_username));
                else
                    new SignupTask(userName, email, password, reference, gender.getText().toString().toLowerCase(), SignupActivity.this).execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_login));
            }
        } else
            Toast.makeText(SignupActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
    }

    private static class SignupTask extends AsyncTask<String, String, String> {
        private String userName;
        private String email;
        private String password;
        private String reference;
        private String gender;

        private WeakReference<SignupActivity> activityReference;

        private ProgressDialog progress;

        SignupTask(String userName, String email, String password, String reference, String gender, SignupActivity context) {
            this.userName   = userName;
            this.email      = email;
            this.password   = password;
            this.reference  = reference;
            this.gender     = gender;

            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            SignupActivity activity = activityReference.get();
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
                hm.put("action", "signup");
                hm.put("username", userName);
                hm.put("email", email);
                hm.put("password", password);
                hm.put("reference", reference);
                hm.put("gender", gender);

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

            SignupActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            try {
                JSONObject result = new JSONObject(s);

                Integer status  = result.getInt("status");
                String error    = result.getString("error");
                Boolean success = result.getBoolean("success");

                progress.dismiss();

                if (status != 200)
                    Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                else {
                    if (!success) {
                        switch (error) {
                            case "username":
                                Toast.makeText(activity, activity.getResources().getString(R.string.exists_username), Toast.LENGTH_LONG).show();
                                break;
                            case "email":
                                Toast.makeText(activity, activity.getResources().getString(R.string.exists_email), Toast.LENGTH_LONG).show();
                                break;
                            case "reference":
                                Toast.makeText(activity, activity.getResources().getString(R.string.invalid_reference), Toast.LENGTH_LONG).show();
                                break;
                        }
                    } else
                        Toast.makeText(activity, activity.getResources().getString(R.string.signup_complete), Toast.LENGTH_LONG).show();
                }
            }
            catch (Exception e) {
                progress.dismiss();
                Toast.makeText(activity, activity.getResources().getString(R.string.something_wrong), Toast.LENGTH_LONG).show();
                Log.e("Url", e.toString());
            }
        }
    }
}
