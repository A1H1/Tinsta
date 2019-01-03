package in.devco.tinsta.lib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import in.devco.tinsta.actvity.LoginActivity;
import in.devco.tinsta.actvity.MainActivity;

public class User {
    private static final String LOGIN_SHARED_PREFERENCES = "login";

    private static final String LOGGED_IN = "loggedIn";
    private static final String USER_ID = "userId";
    private static final String SESSION_ID = "sessionId";
    private static final String SRC = "src";

    public static void logIn(Context context, Integer userId, String sessionId, String src) {
        SharedPreferences sp = context.getSharedPreferences(LOGIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();

        editor.putBoolean(LOGGED_IN, true);
        editor.putInt(USER_ID, userId);
        editor.putString(SESSION_ID, sessionId);
        editor.putString(SRC, src);
        editor.apply();

        context.startActivity(new Intent(context, MainActivity.class));
    }

    public static Boolean isLoggedIn(Context context) {
        SharedPreferences sp = context.getSharedPreferences(LOGIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        return sp.getBoolean(LOGGED_IN, false);
    }

    public static Object[] sessionDetails(Context context) {
        SharedPreferences sp = context.getSharedPreferences(LOGIN_SHARED_PREFERENCES, Context.MODE_PRIVATE);

        return new Object[]{sp.getInt(USER_ID, 0), sp.getString(SESSION_ID, "")};
    }
}