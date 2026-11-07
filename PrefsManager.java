package com.umko.pengeluaranku;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME = "pengeluaranku_prefs";
    private static final String KEY_DARK = "dark_mode";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_BUDGET = "budget_bulanan";
    private static final String KEY_SORT = "sort_terbesar";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setDarkMode(boolean on) { prefs.edit().putBoolean(KEY_DARK, on).apply(); }
    public boolean isDarkMode() { return prefs.getBoolean(KEY_DARK, false); }

    public void setUserName(String name) { prefs.edit().putString(KEY_NAME, name).apply(); }
    public String getUserName() { return prefs.getString(KEY_NAME, ""); }

    public void setBudget(long budget) { prefs.edit().putLong(KEY_BUDGET, budget).apply(); }
    public long getBudget() { return prefs.getLong(KEY_BUDGET, 1000000L); }

    public void setSortTerbesar(boolean on) { prefs.edit().putBoolean(KEY_SORT, on).apply(); }
    public boolean isSortTerbesar() { return prefs.getBoolean(KEY_SORT, false); }
}