package io.github.x0b.safdav;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Base64;
import io.github.x0b.safdav.file.SafConstants;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * Saf Emulation Server
 */
public class SafAccessProvider {

    private static final String PREF_KEY_SAF_USER = "io.github.x0b.safdav.safDavUser";
    private static final String PREF_KEY_SAF_PASS = "io.github.x0b.safdav.safDavPass";

    private static SafDAVServer davServer;
    private static SafDirectServer directServer;
    private static String user;
    private static String password;

    public static SafDAVServer getServer(Context context) throws IOException {
        if(null == davServer) {
            initAuth(context);
            davServer = new SafDAVServer(SafConstants.SAF_REMOTE_PORT, user, password, context);
            davServer.start();
        }
        return davServer;
    }

    @SuppressLint("ApplySharedPref")
    private static void initAuth(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(preferences.contains(PREF_KEY_SAF_PASS) && preferences.contains((PREF_KEY_SAF_USER))){
            password = preferences.getString(PREF_KEY_SAF_PASS, "");
            user = preferences.getString(PREF_KEY_SAF_USER, "dav");
        } else {
            SecureRandom random = new SecureRandom();
            byte[] values = new byte[16];
            random.nextBytes(values);
            password = Base64.encodeToString(values, Base64.NO_WRAP);
            user = "dav";
            preferences.edit()
                    .putString(PREF_KEY_SAF_PASS, password)
                    .putString(PREF_KEY_SAF_USER, user)
                    .commit();
        }
    }

    public static SafDirectServer getDirectServer(Context context) {
        if(null == directServer) {
            directServer = new SafDirectServer(context);
        }
        return directServer;
    }

    public static String getUser(Context context) {
        initAuth(context);
        return user;
    }

    public static String getPassword(Context context) {
        initAuth(context);
        return password;
    }
}
