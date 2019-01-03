package in.devco.tinsta.lib;

import android.text.TextUtils;

import java.util.regex.Pattern;

public class Util {
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{4,15}$";

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidUsername(String username) {
        return Pattern.compile(USERNAME_PATTERN).matcher(username).matches();
    }

    public static String plural(int count, String text) {
        if (count ==  1)
            return "1 " + text;
        else if (count > 1)
            return count + " " + text + "s";
        else
            return "0 " + text;

    }
}
