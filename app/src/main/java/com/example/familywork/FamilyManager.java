package com.example.familywork;

import android.content.Context;
import android.content.SharedPreferences;

public class FamilyManager {

    private static final String PREF_NAME = "family_prefs";
    private static final String KEY_FAMILY = "family_code";

    public static void saveFamilyCode(Context context, String familyCode) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_FAMILY, familyCode).apply();
    }

    public static String getFamilyCode(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_FAMILY, null);
    }
}
