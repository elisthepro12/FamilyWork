package com.example.familywork;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    // מפתח יום בפורמט yyyy-MM-dd לשימוש ב-Firebase (קל למיון ותצוגה)
    public static String dayKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // מחרוזת קריאה נחמדה ליום להצגה
    public static String dayTitle(String dayKey) {
        try {
            SimpleDateFormat src = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dst = new SimpleDateFormat("EEEE - dd/MM/yyyy", Locale.getDefault());
            Date d = src.parse(dayKey);
            return dst.format(d);
        } catch (Exception e) {
            return dayKey;
        }
    }
}
