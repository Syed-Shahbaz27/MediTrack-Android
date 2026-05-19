package com.meditrack.app.utils;
import android.content.Context;
import android.content.SharedPreferences;
/**
 * SessionManager — handles user login state using SharedPreferences.
 *
 * WHY SharedPreferences:
 * SQLite stores data. SharedPreferences stores app STATE — like whether
 * someone is currently logged in. It persists between app restarts.
 *
 * HOW IT WORKS:
 * On login → saveSession() stores userId + userName in SharedPreferences.
 * Every Activity → reads getUserId() to know whose data to load from SQLite.
 * On logout → clearSession() wipes the saved state.
 *
 * This is the "thread" that connects all 10 screens — every screen that
 * shows user data calls session.getUserId() to know which user is logged in.
 */
public class SessionManager {

    private static final String PREF_NAME       = "MediTrackSession";
    private static final String KEY_LOGGED_IN   = "isLoggedIn";
    private static final String KEY_USER_ID     = "userId";
    private static final String KEY_USER_NAME   = "userName";
    private static final String KEY_USER_EMAIL  = "userEmail";
    private static final String KEY_LAST_LOGIN  = "lastLogin";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Call this immediately after successful login.
     * Stores the user's details so any activity can access them.
     */
    public void saveSession(int userId, String userName, String userEmail, String lastLogin) {
        editor.putBoolean(KEY_LOGGED_IN,  true);
        editor.putInt    (KEY_USER_ID,    userId);
        editor.putString (KEY_USER_NAME,  userName);
        editor.putString (KEY_USER_EMAIL, userEmail);
        editor.putString (KEY_LAST_LOGIN, lastLogin);
        editor.apply(); // apply() is async and safer than commit()
    }

    /** Returns true if a user is currently logged in */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    /** Returns the logged-in user's database ID — used by every data-loading activity */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /** Returns the logged-in user's name — used for "Hello Shahbaz" on dashboard */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    /** Returns the logged-in user's email */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Returns when user last logged in.
     * Shown on dashboard = "Last login: 10 May 2026, 3:45 PM"
     */
    public String getLastLogin() {
        return prefs.getString(KEY_LAST_LOGIN, "First login");
    }

    /** Save Remember Me checkbox state */
    public void setRememberMe(boolean rememberMe) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    /** Returns whether user checked "Remember Me" on last login */
    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Call this on logout. Clears all stored session data.
     * User will be sent back to SplashActivity.
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
